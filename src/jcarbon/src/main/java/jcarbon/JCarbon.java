package jcarbon;

import static java.util.stream.Collectors.toList;
import static jcarbon.data.DataOperations.forwardApply;
import static jcarbon.data.DataOperations.forwardPartialAlign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jcarbon.cpu.eflect.EflectAccounting;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.cpu.jiffies.JiffiesAccounting;
import jcarbon.cpu.jiffies.ProcStat;
import jcarbon.cpu.jiffies.ProcTask;
import jcarbon.cpu.jiffies.ProcessActivity;
import jcarbon.cpu.jiffies.ProcessJiffies;
import jcarbon.cpu.jiffies.ProcessSample;
import jcarbon.cpu.jiffies.SystemJiffies;
import jcarbon.cpu.jiffies.SystemSample;
import jcarbon.cpu.rapl.Powercap;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.cpu.rapl.RaplSample;
import jcarbon.util.SamplingFuture;

/** A class to collect and provide jcarbon signals. */
public final class JCarbon {
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-eflect");
            t.setDaemon(false);
            return t;
          });

  private final HashMap<Class<?>, List<?>> dataSignals = new HashMap<>();

  private boolean isRunning = false;
  private SamplingFuture<ProcessSample> processFuture;
  private SamplingFuture<SystemSample> systemFuture;
  private SamplingFuture<Optional<RaplSample>> raplFuture;

  /** Starts the sampling futures is we aren't already running. */
  public void start() {
    synchronized (this) {
      if (!isRunning) {
        processFuture = SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, 10, executor);
        systemFuture = SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, 10, executor);
        raplFuture = SamplingFuture.fixedPeriodMillis(Powercap::sample, 10, executor);
        isRunning = true;
      }
    }
  }

  /** Stops the sampling futures and merges the data they collected. */
  // TODO: this can throw if any of the futures are empty. i don't know how to handle this yet
  public void stop() {
    synchronized (this) {
      if (isRunning) {
        isRunning = false;

        // physical signals
        dataSignals.put(
            ProcessJiffies.class, forwardApply(processFuture.get(), ProcessJiffies::between));
        dataSignals.put(
            SystemJiffies.class, forwardApply(systemFuture.get(), SystemJiffies::between));
        List<RaplSample> raplSamples =
            raplFuture.get().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        if (!raplSamples.isEmpty()) {
          dataSignals.put(RaplInterval.class, forwardApply(raplSamples, Powercap::difference));
        }
        processFuture = null;
        systemFuture = null;
        raplFuture = null;

        // virtual signals
        dataSignals.put(
            ProcessActivity.class,
            forwardPartialAlign(
                getSignal(ProcessJiffies.class),
                getSignal(SystemJiffies.class),
                JiffiesAccounting::computeTaskActivity));
        if (hasSignal(RaplInterval.class)) {
          dataSignals.put(
              ProcessEnergy.class,
              forwardPartialAlign(
                  getSignal(ProcessActivity.class),
                  getSignal(RaplInterval.class),
                  EflectAccounting::computeTaskEnergy));
        }
      }
    }
  }

  public boolean hasSignal(Class<?> cls) {
    return dataSignals.keySet().stream().anyMatch(cls::equals);
  }

  public <T> List<T> getSignal(Class<T> cls) {
    if (hasSignal(cls)) {
      return new ArrayList<>((List<T>) dataSignals.get(cls));
    }
    return List.of();
  }

  /** Deep copy of the storage. */
  public Map<Class<?>, List<?>> getSignals() {
    HashMap<Class<?>, List<?>> signalsCopy = new HashMap<>();
    dataSignals.forEach((k, v) -> signalsCopy.put(k, new ArrayList<>(v)));
    return signalsCopy;
  }
}
