package jcarbon.dacapo;

import java.util.HashMap;
import java.util.List;
import jcarbon.cpu.eflect.Eflect;
import jcarbon.cpu.eflect.EnergyFootprint;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public class JCarbonCallback extends Callback {
  private final HashMap<Integer, List<EnergyFootprint>> energy = new HashMap<>();
  private final Eflect eflect = new Eflect();

  private int iteration = 0;

  public JCarbonCallback(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    eflect.start();
    super.start(benchmark);
  }

  @Override
  public void complete(String benchmark, boolean valid, boolean warmup) {
    super.complete(benchmark, valid, warmup);

    List<EnergyFootprint> footprints = eflect.stop();
    System.out.println(
        String.format(
            "Consumed %.6f joules",
            footprints.stream()
                .mapToDouble(nrg -> nrg.data().stream().mapToDouble(e -> e.energy).sum())
                .sum()));
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
