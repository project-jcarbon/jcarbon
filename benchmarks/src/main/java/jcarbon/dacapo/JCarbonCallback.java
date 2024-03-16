package jcarbon.dacapo;

import java.time.Instant;
import jcarbon.JCarbon;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.cpu.jiffies.ProcessActivity;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

import jcarbon.emissions.EmissionsConverter;
import jcarbon.emissions.EmissionsConverters;
import jcarbon.emissions.EmissionsInterval;

import java.util.List;

public class JCarbonCallback extends Callback {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private final JCarbon jcarbon = new JCarbon();

  private Instant start = Instant.EPOCH;

  public JCarbonCallback(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    start = Instant.now();
    jcarbon.start();
    super.start(benchmark);
  }

  @Override
  public void complete(String benchmark, boolean valid, boolean warmup) {
    super.complete(benchmark, valid, warmup);
    jcarbon.stop();

    System.out.println(
        String.format(
            "Consumed %.2f%% of cycles",
            100
                * jcarbon.getSignal(ProcessActivity.class).stream()
                    .mapToDouble(
                        activity ->
                            activity.data().stream().mapToDouble(a -> a.activity).sum() / CPU_COUNT)
                    .average()
                    .getAsDouble()));
    System.out.println(
        String.format(
            "Consumed %.6f joules",
            jcarbon.getSignal(ProcessEnergy.class).stream()
                .mapToDouble(nrg -> nrg.data().stream().mapToDouble(e -> e.energy).sum())
                .sum()));
    
    EmissionsConverter converter = EmissionsConverters.forLocale("USA");
    System.out.println(
        String.format(
          "Consumed %.6f emissions",
          jcarbon.getSignal(ProcessEnergy.class).stream()
              .mapToDouble(nrg -> converter.convert(nrg).getEmissions())
              .sum()));
  
    
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
