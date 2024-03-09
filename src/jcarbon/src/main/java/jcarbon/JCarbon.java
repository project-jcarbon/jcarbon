package jcarbon;

import static jcarbon.cpu.eflect.EflectAccounting.accountTaskEnergy;
import static jcarbon.cpu.jiffies.JiffiesAccounting.accountTaskActivity;
import static jcarbon.data.DataOperations.forwardApply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.cpu.jiffies.ProcStat;
import jcarbon.cpu.jiffies.ProcTask;
import jcarbon.cpu.jiffies.ProcessActivity;
import jcarbon.cpu.jiffies.ProcessSample;
import jcarbon.cpu.jiffies.SystemSample;
import jcarbon.cpu.rapl.Powercap;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.util.SamplingFuture;

public class JCarbon {
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-eflect");
            t.setDaemon(false);
            return t;
          });

  private final HashMap<String, SamplingFuture<?>> dataFutures = new HashMap<>();
  private final HashMap<Class<?>, List<?>> dataSignals = new HashMap<>();

  private boolean isRunning = false;

  //   private SamplingFuture<ProcessSample> taskFuture;
  //   private SamplingFuture<SystemSample> sysFuture;
  //   private SamplingFuture<RaplSample> raplFuture;

  public JCarbon() {}

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
        // taskFuture = SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, 10, executor);
        // sysFuture = SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, 10, executor);
        // raplFuture = SamplingFuture.fixedPeriodMillis(Powercap::sample, 10, executor);
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
        dataSignals.put(
            RaplInterval.class,
            forwardApply(dataFutures.get("powercap_energy").get(), Powercap::difference));
        dataSignals.put(
            ProcessActivity.class,
            accountTaskActivity(
                (List<ProcessSample>) dataFutures.get("process_jiffies").get(),
                (List<SystemSample>) dataFutures.get("cpu_jiffies").get()));
        dataSignals.put(
            ProcessEnergy.class,
            accountTaskEnergy(
                (List<ProcessActivity>) dataSignals.get("process_activity"),
                (List<RaplInterval>) dataSignals.get("powercap_energy")));
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
