package jcarbon;

import static jcarbon.data.DataOperations.forwardApply;
import static jcarbon.data.DataOperations.forwardPartialAlign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import jcarbon.data.Interval;
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

  private final HashMap<String, SamplingFuture<?>> dataFutures = new HashMap<>();
  private final HashMap<Class<?>, List<? extends Interval<?>>> dataSignals = new HashMap<>();

  private boolean isRunning = false;

  /** Starts the sampling futures is we aren't already running. */
  public void start() {
    synchronized (this) {
      if (!isRunning) {
        dataFutures.put(
            "process_jiffies",
            SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, 10, executor));
        dataFutures.put(
            "cpu_jiffies", SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, 10, executor));
        dataFutures.put(
            "powercap_energy", SamplingFuture.fixedPeriodMillis(Powercap::sample, 10, executor));
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
            ProcessJiffies.class,
            forwardApply(
                (List<ProcessSample>) dataFutures.get("process_jiffies").get(),
                ProcessJiffies::between));
        dataSignals.put(
            SystemJiffies.class,
            forwardApply(
                (List<SystemSample>) dataFutures.get("cpu_jiffies").get(),
                SystemJiffies::between));
        dataSignals.put(
            RaplInterval.class,
            forwardApply(
                (List<RaplSample>) dataFutures.get("powercap_energy").get(), Powercap::difference));

        // virtual signals
        dataSignals.put(
            ProcessActivity.class,
            forwardPartialAlign(
                getSignal(ProcessJiffies.class),
                getSignal(SystemJiffies.class),
                JiffiesAccounting::accountInterval));
        dataSignals.put(
            ProcessEnergy.class,
            forwardPartialAlign(
                getSignal(ProcessActivity.class),
                getSignal(RaplInterval.class),
                EflectAccounting::accountInterval));
        dataFutures.clear();
      }
    }
  }

  public <T> List<T> getSignal(Class<T> cls) {
    if (dataSignals.keySet().stream().anyMatch(cls::equals)) {
      return new ArrayList<>((List<T>) dataSignals.get(cls));
    }
    return List.of();
  }
}
