package jrapl;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Very simple energy monitor that reports energy consumption over 10 millisecond intervals. */
final class EnergyMonitor {
  private static Supplier<EnergySample> getEnergySource() {
    if (MicroArchitecture.SOCKETS > 1) {
      return Rapl::sample;
    } else if (Powercap.SOCKETS > 1) {
      return Powercap::sample;
    }
    throw new IllegalStateException("no energy source found!");
  }

  private static BiFunction<EnergySample, EnergySample, EnergyInterval> getEnergyDiffer() {
    if (MicroArchitecture.SOCKETS > 1) {
      return Rapl::difference;
    } else if (Powercap.SOCKETS > 1) {
      return Powercap::difference;
    }
    throw new IllegalStateException("no energy source found!");
  }

  public static void main(String[] args) throws Exception {
    Supplier<EnergySample> source = getEnergySource();
    BiFunction<EnergySample, EnergySample, EnergyInterval> differ = getEnergyDiffer();

    EnergySample last = source.get();
    while (true) {
      Thread.sleep(10);
      EnergySample current = source.get();
      LoggerUtil.LOGGER.info(String.format("%s", differ.apply(last, current)));
      last = current;
    }
  }
}
