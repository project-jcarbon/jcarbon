package jcarbon.dacapo;

import static jcarbon.data.DataOperations.forwardApply;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jcarbon.cpu.eflect.EflectAccounting;
import jcarbon.cpu.eflect.EnergyFootprint;
import jcarbon.cpu.jiffies.JiffiesAccounting;
import jcarbon.cpu.jiffies.ProcStat;
import jcarbon.cpu.jiffies.ProcTask;
import jcarbon.cpu.jiffies.ProcessSample;
import jcarbon.cpu.jiffies.SystemSample;
import jcarbon.cpu.rapl.Powercap;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.cpu.rapl.RaplSample;
import jcarbon.util.SamplingFuture;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public class JCarbonCallback extends Callback {
  private static final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-sampling");
            t.setDaemon(false);
            return t;
          });

  private final HashMap<Integer, List<EnergyFootprint>> energy = new HashMap<>();

  private int iteration = 0;
  private SamplingFuture<ProcessSample> taskFuture;
  private SamplingFuture<SystemSample> sysFuture;
  private SamplingFuture<RaplSample> raplFuture;

  public JCarbonCallback(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    taskFuture = SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, 10, executor);
    sysFuture = SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, 10, executor);
    raplFuture = SamplingFuture.fixedPeriodMillis(Powercap::sample, 10, executor);

    super.start(benchmark);
  }

  @Override
  public void stop(long w) {
    super.stop(w);

    List<RaplInterval> rapl = forwardApply(raplFuture.get(), Powercap::difference);
    List<EnergyFootprint> footprints =
        EflectAccounting.accountTasks(
            JiffiesAccounting.accountTasks(taskFuture.get(), sysFuture.get()), rapl);
    System.out.println(
        rapl.stream()
            .mapToDouble(nrg -> Arrays.stream(nrg.data()).mapToDouble(e -> e.total).sum())
            .summaryStatistics());
    System.out.println(
        footprints.stream()
            .mapToDouble(nrg -> nrg.data().stream().mapToDouble(e -> e.energy).sum())
            .summaryStatistics());
    energy.put(iteration, footprints);

    iteration++;
  }

  @Override
  public boolean runAgain() {
    // if we have run every iteration, dump the data and terminate
    if (!super.runAgain()) {
      // System.out.println("dumping data");
      return false;
    } else {
      return true;
    }
  }
}
