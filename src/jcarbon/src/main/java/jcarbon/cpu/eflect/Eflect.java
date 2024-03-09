package jcarbon.cpu.eflect;

import static jcarbon.cpu.eflect.EflectAccounting.accountTaskEnergy;
import static jcarbon.cpu.jiffies.JiffiesAccounting.accountTaskActivity;
import static jcarbon.data.DataOperations.forwardApply;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jcarbon.cpu.jiffies.ProcStat;
import jcarbon.cpu.jiffies.ProcTask;
import jcarbon.cpu.jiffies.ProcessSample;
import jcarbon.cpu.jiffies.SystemSample;
import jcarbon.cpu.rapl.Powercap;
import jcarbon.cpu.rapl.RaplSample;
import jcarbon.util.SamplingFuture;

/** Simple wrapper to provide an Eflect footprint. */
public final class Eflect {
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-eflect");
            t.setDaemon(false);
            return t;
          });

  private boolean isRunning = false;
  private SamplingFuture<ProcessSample> taskFuture;
  private SamplingFuture<SystemSample> sysFuture;
  private SamplingFuture<RaplSample> raplFuture;

  public Eflect() {}

  /** Starts the sampling futures is we aren't already running. */
  public void start() {
    synchronized (this) {
      if (!isRunning) {
        taskFuture = SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, 10, executor);
        sysFuture = SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, 10, executor);
        raplFuture = SamplingFuture.fixedPeriodMillis(Powercap::sample, 10, executor);
        isRunning = true;
      }
    }
  }

  /** Stops the sampling futures and merges the data they collected. */
  // TODO: this can throw if any of the futures are empty. i don't know how to handle this yet
  public List<ProcessEnergy> stop() {
    synchronized (this) {
      if (isRunning) {
        isRunning = false;
        return accountTaskEnergy(
        accountTaskActivity(taskFuture.get(), sysFuture.get()),
        forwardApply(raplFuture.get(), Powercap::difference));
      } else {
        return List.of();
      }
    }
  }
}
