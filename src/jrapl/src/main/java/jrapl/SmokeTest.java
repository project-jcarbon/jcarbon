package jrapl;

import static java.util.stream.Collectors.joining;

import java.time.Duration;
import java.util.stream.IntStream;

/** A smoke test to check which components are available and if they are reporting similarly. */
final class SmokeTest {
  private static int fib(int n) {
    if (n == 0 || n == 1) {
      return 1;
    } else {
      return fib(n - 1) + fib(n - 2);
    }
  }

  private static void exercise() {
    fib(42);
  }

  private static boolean raplAvailable() throws Exception {
    if (!NativeLibrary.initialize()) {
      return false;
    }

    if (MicroArchitecture.NAME.equals(MicroArchitecture.UNKNOWN)) {
      LoggerUtil.LOGGER.info("no microarchitecture could be found through rapl!");
      return false;
    }

    if (MicroArchitecture.SOCKETS < 1) {
      LoggerUtil.LOGGER.info("microarchitecture has no energy domains through rapl!");
      return false;
    }

    EnergySample start = Rapl.sample();

    exercise();

    EnergyInterval interval = Rapl.difference(start, Rapl.sample());

    if (IntStream.range(0, MicroArchitecture.SOCKETS)
            .mapToDouble(socket -> interval.getReading(socket).total)
            .sum()
        == 0) {
      LoggerUtil.LOGGER.info("no energy consumed with the difference of two rapl samples!");
      return false;
    }

    LoggerUtil.LOGGER.info(
        String.join(
            System.lineSeparator(),
            String.format(
                "rapl report - microarchitecture: %s, time: %s",
                MicroArchitecture.NAME, Duration.between(interval.start, interval.end)),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package: %.3fJ, dram: %.3fJ, core: %.3fJ, gpu: %.3fJ",
                            socket,
                            interval.getReading(socket).pkg,
                            interval.getReading(socket).dram,
                            interval.getReading(socket).core,
                            interval.getReading(socket).gpu))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  private static boolean powercapAvailable() throws Exception {
    if (Powercap.SOCKETS < 1) {
      return false;
    }

    EnergySample start = Powercap.sample();

    exercise();

    EnergyInterval interval = Powercap.difference(start, Powercap.sample());

    if (IntStream.range(0, MicroArchitecture.SOCKETS)
            .mapToDouble(socket -> interval.getReading(socket).total)
            .sum()
        == 0) {
      LoggerUtil.LOGGER.info("no energy consumed with the difference of two powercap samples!");
      return false;
    }

    LoggerUtil.LOGGER.info(
        String.join(
            System.lineSeparator(),
            String.format(
                "powercap report - time: %s", Duration.between(interval.start, interval.end)),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package: %.3fJ, dram: %.3fJ",
                            socket,
                            interval.getReading(socket).pkg,
                            interval.getReading(socket).dram))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  private static boolean checkEquivalence() throws Exception {
    if (!NativeLibrary.initialize()) {
      return false;
    }

    if (Powercap.SOCKETS != MicroArchitecture.SOCKETS) {
      LoggerUtil.LOGGER.info(
          String.format(
              "energy domains for powercap (%s) does not match rapl (%d)!",
              Powercap.SOCKETS, MicroArchitecture.SOCKETS));
      return false;
    }

    EnergySample rapl = Rapl.sample();
    EnergySample powercap = Powercap.sample();

    exercise();

    return isSimilar(
        Rapl.difference(rapl, Rapl.sample()), Powercap.difference(powercap, Powercap.sample()));
  }

  private static boolean isSimilar(EnergyInterval rapl, EnergyInterval powercap) {
    // if (Durations.between(rapl.start, powercap.start) > 2000) {
    //   LoggerUtil.LOGGER.info(
    //       String.format(
    //           "powercap start time (%s) does not match rapl start time (%s)",
    //           powercap.getStart(), rapl.getStart()));
    //   return false;
    // }

    // if (Durations.toMicros(Timestamps.between(rapl.getEnd(), powercap.getEnd())) > 2000) {
    //   LoggerUtil.LOGGER.info(
    //       String.format(
    //           "powercap end time (%s) does not match rapl end time (%s)",
    //           powercap.getEnd(), rapl.getEnd()));
    //   return false;
    // }

    // if (powercap.getReadingCount() != rapl.getReadingCount()) {
    //   LoggerUtil.LOGGER.info(
    //       String.format(
    //           "powercap reading count (%s) does not match rapl reading count (%s)",
    //           powercap.getReadingCount(), rapl.getReadingCount()));
    //   return false;
    // }

    // Map<Integer, JRaplReading> raplReadings =
    //     rapl.getReadingList().stream().collect(toMap(r -> r.getSocket(), r -> r));
    // Map<Integer, JRaplReading> powercapReadings =
    //     powercap.getReadingList().stream().collect(toMap(r -> r.getSocket(), r -> r));
    // raplReadings.keySet().equals(powercapReadings.keySet());
    // for (int socket : raplReadings.keySet()) {
    //   if (Math.abs(
    //           raplReadings.get(socket).getPackage() - powercapReadings.get(socket).getPackage())
    //       > 1) {
    //     LoggerUtil.LOGGER.info(
    //         String.format(
    //             "powercap package energy (%f) does not match rapl package energy (%f)",
    //             powercapReadings.get(socket).getPackage(),
    // raplReadings.get(socket).getPackage()));
    //     return false;
    //   }

    //   if (Math.abs(raplReadings.get(socket).getDram() - powercapReadings.get(socket).getDram())
    //       > 1) {
    //     LoggerUtil.LOGGER.info(
    //         String.format(
    //             "powercap dram energy (%f) does not match rapl dram energy (%f)",
    //             powercapReadings.get(socket).getDram(), raplReadings.get(socket).getDram()));
    //     return false;
    //   }
    // }

    // LoggerUtil.LOGGER.info(
    //     String.join(
    //         System.lineSeparator(),
    //         String.format(
    //             "equivalence report - elapsed time difference: %.6fs",
    //             Math.abs(
    //                 (double)
    //                         (Durations.toMicros(Timestamps.between(rapl.getStart(),
    // rapl.getEnd()))
    //                             - Durations.toMicros(
    //                                 Timestamps.between(powercap.getStart(), powercap.getEnd())))
    //                     / 1000000)),
    //         raplReadings.values().stream()
    //             .map(
    //                 r ->
    //                     String.format(
    //                         " - socket: %dJ, package difference: %.3fJ, dram difference: %.3fJ",
    //                         r.getSocket(),
    //                         Math.abs(
    //                             r.getPackage() -
    // powercapReadings.get(r.getSocket()).getPackage()),
    //                         Math.abs(r.getDram() -
    // powercapReadings.get(r.getSocket()).getDram())))
    //             .collect(joining(System.lineSeparator()))));
    return true;
  }

  public static void main(String[] args) throws Exception {
    if ((raplAvailable() & powercapAvailable()) && checkEquivalence()) {
      LoggerUtil.LOGGER.info("all smoke tests passed!");
    } else {
      LoggerUtil.LOGGER.info("smoke testing failed; please consult the log.");
    }
  }
}
