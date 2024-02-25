package jcarbon.cpu.rapl;

import static jcarbon.util.LoggerUtil.getLogger;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** Very simple energy monitor that reports energy consumption over 10 millisecond intervals. */
final class RaplMonitor {
  private static final Logger logger = getLogger();

  // TODO: this is cumbersome, should really have an `isAvailable` method somewhere
  private static Supplier<RaplSample> getEnergySource() {
    if (Rapl.isAvailable()) {
      return Rapl::sample;
    } else if (Powercap.isAvailable()) {
      return Powercap::sample;
    }
    throw new IllegalStateException("no energy source found!");
  }

  private static BiFunction<RaplSample, RaplSample, RaplInterval> getEnergyDiffer() {
    if (Rapl.isAvailable()) {
      return Rapl::difference;
    } else if (Powercap.isAvailable()) {
      return Powercap::difference;
    }
    throw new IllegalStateException("no energy source found!");
  }

  public static void main(String[] args) throws Exception {
    Supplier<RaplSample> source = getEnergySource();
    BiFunction<RaplSample, RaplSample, RaplInterval> differ = getEnergyDiffer();

    RaplSample last = source.get();
    while (true) {
      Thread.sleep(10);
      RaplSample current = source.get();
      RaplInterval interval = differ.apply(last, current);
      logger.info(String.format("%s", interval));
      last = current;
    }
  }
}
