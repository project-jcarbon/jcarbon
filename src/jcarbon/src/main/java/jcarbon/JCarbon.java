package jcarbon;

import static java.util.stream.Collectors.toList;
import static jcarbon.data.DataOperations.forwardApply;
import static jcarbon.data.DataOperations.forwardPartialAlign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
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
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.cpu.rapl.RaplSample;
import jcarbon.cpu.rapl.RaplSource;
import jcarbon.emissions.EmissionsConverter;
import jcarbon.emissions.EmissionsConverters;
import jcarbon.emissions.EmissionsInterval;
import jcarbon.util.LoggerUtil;
import jcarbon.util.SamplingFuture;

/** A class to collect and provide jcarbon signals. */
public final class JCarbon {
  private static final Logger logger = LoggerUtil.getLogger();

  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-sampling-thread");
            t.setDaemon(true);
            return t;
          });
  private final HashMap<Class<?>, List<?>> dataSignals = new HashMap<>();
  private final RaplSource source = RaplSource.getRaplSource();
  private final EmissionsConverter converter = EmissionsConverters.getDefault();

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
        raplFuture = SamplingFuture.fixedPeriodMillis(source::sample, 10, executor);
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
        addSignal(ProcessJiffies.class, forwardApply(processFuture.get(), ProcessJiffies::between));
        addSignal(SystemJiffies.class, forwardApply(systemFuture.get(), SystemJiffies::between));
        List<RaplSample> raplSamples =
            raplFuture.get().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        if (raplSamples.size() > 1) {
          addSignal(RaplInterval.class, forwardApply(raplSamples, source::difference));
        }
        processFuture = null;
        systemFuture = null;
        raplFuture = null;

        // virtual signals
        addSignal(
            ProcessActivity.class,
            forwardPartialAlign(
                getSignal(ProcessJiffies.class),
                getSignal(SystemJiffies.class),
                JiffiesAccounting::computeTaskActivity));
        if (hasSignal(RaplInterval.class)) {
          addSignal(
              ProcessEnergy.class,
              forwardPartialAlign(
                  getSignal(ProcessActivity.class),
                  getSignal(RaplInterval.class),
                  EflectAccounting::computeTaskEnergy));
          addSignal(
              EmissionsInterval.class,
              getSignal(ProcessEnergy.class).stream()
                  .map(nrg -> converter.convert(nrg))
                  .collect(toList()));
        }
      }
    }
  }

  /** Checks if there is a signal for the class. */
  public boolean hasSignal(Class<?> cls) {
    return dataSignals.keySet().stream().anyMatch(cls::equals);
  }

  /** Shallow copy of the signal list. */
  public <T> List<T> getSignal(Class<T> cls) {
    if (hasSignal(cls)) {
      return new ArrayList<>((List<T>) dataSignals.get(cls));
    }
    return List.of();
  }

  /** Shallow copy of the signals added. */
  public Set<Class<?>> getSignalTypes() {
    return new HashSet<>(dataSignals.keySet());
  }

  /** Deep copy of the storage. */
  public Map<Class<?>, List<?>> getSignals() {
    HashMap<Class<?>, List<?>> signalsCopy = new HashMap<>();
    dataSignals.forEach((k, v) -> signalsCopy.put(k, new ArrayList<>(v)));
    return signalsCopy;
  }

  private <T> void addSignal(Class<T> cls, List<T> data) {
    dataSignals.put(cls, data);
    logger.info(String.format("added signal for %s", cls.getSimpleName()));
  }
}
