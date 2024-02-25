package jcarbon.dacapo;

import static jcarbon.cpu.jiffies.JiffiesAccounting.accountTasks;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jcarbon.cpu.jiffies.ProcStat;
import jcarbon.cpu.jiffies.ProcTask;
import jcarbon.cpu.jiffies.ProcessSample;
import jcarbon.cpu.jiffies.SystemSample;
import jcarbon.cpu.jiffies.TaskActivityInterval;
import jcarbon.util.SamplingFuture;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public class JCarbonCallback extends Callback {
  private static final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor();

  private final HashMap<Integer, List<TaskActivityInterval>> activity = new HashMap<>();

  private int iteration = 0;
  private SamplingFuture<ProcessSample> taskFuture;
  private SamplingFuture<SystemSample> sysFuture;

  public JCarbonCallback(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    taskFuture = SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, 10, executor);
    sysFuture = SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, 10, executor);
    super.start(benchmark);
  }

  @Override
  public void stop(long w) {
    super.stop(w);
    activity.put(iteration, accountTasks(taskFuture.get(), sysFuture.get()));
    System.out.println(
        activity.get(iteration).stream()
            .mapToDouble(
                at ->
                    at.data().stream().mapToDouble(a -> a.activity).sum()
                        / Runtime.getRuntime().availableProcessors())
            .summaryStatistics());
    iteration++;
  }

  @Override
  public boolean runAgain() {
    // if we have run every iteration, dump the data and terminate
    if (!super.runAgain()) {
      System.out.println("dumping data");
      return false;
    } else {
      return true;
    }
  }
}
