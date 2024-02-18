package jrapl;

import static java.util.stream.Collectors.joining;

import java.time.Duration;
import java.util.stream.IntStream;

/** A smoke test to check which components are available and if they are reporting similarly. */
final class SmokeTest {
  private static final Duration ONE_SECOND = Duration.ofSeconds(1L);

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

  /** Checks if rapl is available for sampling. */
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
            .mapToDouble(socket -> interval.getReadings()[socket].total)
            .sum()
        == 0) {
      LoggerUtil.LOGGER.info("no energy consumed with the difference of two rapl samples!");
      return false;
    }

    LoggerUtil.LOGGER.info(
        String.join(
            System.lineSeparator(),
            "rapl report",
            String.format(
                "- microarchitecture: %s, time: %s",
                MicroArchitecture.NAME, Duration.between(interval.start, interval.end)),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package: %.3fJ, dram: %.3fJ, core: %.3fJ, gpu: %.3fJ",
                            socket + 1,
                            interval.getReadings()[socket].pkg,
                            interval.getReadings()[socket].dram,
                            interval.getReadings()[socket].core,
                            interval.getReadings()[socket].gpu))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  /** Checks if powercap is available for sampling. */
  private static boolean powercapAvailable() throws Exception {
    if (Powercap.SOCKETS < 1) {
      return false;
    }

    EnergySample start = Powercap.sample();

    exercise();

    EnergyInterval interval = Powercap.difference(start, Powercap.sample());

    if (IntStream.range(0, MicroArchitecture.SOCKETS)
            .mapToDouble(socket -> interval.getReadings()[socket].total)
            .sum()
        == 0) {
      LoggerUtil.LOGGER.info("no energy consumed with the difference of two powercap samples!");
      return false;
    }

    LoggerUtil.LOGGER.info(
        String.join(
            System.lineSeparator(),
            "powercap report",
            String.format("- time: %s", Duration.between(interval.start, interval.end)),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package: %.3fJ, dram: %.3fJ",
                            socket + 1,
                            interval.getReadings()[socket].pkg,
                            interval.getReadings()[socket].dram))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  /**
   * Checks if rapl and powercap report similarly. This acts as a correctness test since you will be
   * able to determine if values are not sane.
   */
  private static boolean checkEquivalence() throws Exception {
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
        Powercap.difference(powercap, Powercap.sample()), Rapl.difference(rapl, Rapl.sample()));
  }

  private static boolean isSimilar(EnergyInterval powercap, EnergyInterval rapl) {
    if (!validateTimestamps(powercap, rapl)) {
      return false;
    }

    if (!validateComponents(powercap, rapl)) {
      return false;
    }

    LoggerUtil.LOGGER.info(
        String.join(
            System.lineSeparator(),
            "equivalence report",
            String.format(
                "- elapsed time difference: %.6fs",
                Math.abs(
                        (double)
                            (Duration.between(rapl.start, rapl.end).toNanos()
                                - Duration.between(powercap.start, powercap.end).toNanos()))
                    / 1000000000),
            IntStream.range(0, powercap.getReadings().length)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package difference: %.3fJ, dram difference: %.3fJ",
                            socket + 1,
                            Math.abs(
                                powercap.getReadings()[socket].pkg
                                    - rapl.getReadings()[socket].pkg),
                            Math.abs(
                                powercap.getReadings()[socket].dram
                                    - rapl.getReadings()[socket].dram)))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  // TODO: Since Java's time precision is only guaranteed up to milliseconds
  // (https://shorturl.at/cjuL8), there is some potential for mismatch between the microsecond unix
  // time reported by Rapl and the native Java Instant time.
  private static boolean validateTimestamps(EnergyInterval powercap, EnergyInterval rapl) {
    boolean passed = true;
    if (Duration.between(rapl.start, powercap.start).compareTo(ONE_SECOND) > 0) {
      LoggerUtil.LOGGER.info(
          String.format(
              "powercap start time (%s) does not match rapl start time (%s)",
              powercap.start, rapl.start));
      passed = false;
    }

    if (Duration.between(rapl.end, powercap.end).compareTo(ONE_SECOND) > 0) {
      LoggerUtil.LOGGER.info(
          String.format(
              "powercap end time (%s) does not match rapl end time (%s)", powercap.end, rapl.end));
      passed = false;
    }
    return passed;
  }

  private static boolean validateComponents(EnergyInterval powercap, EnergyInterval rapl) {
    if (powercap.getReadings().length != rapl.getReadings().length) {
      LoggerUtil.LOGGER.info(
          String.format(
              "powercap reading count (%s) does not match rapl reading count (%s)",
              powercap.getReadings().length, rapl.getReadings().length));
      return false;
    }

    boolean passed = true;
    for (int socket = 0; socket < powercap.getReadings().length; socket++) {
      EnergyReading powercapReading = powercap.getReadings()[socket];
      EnergyReading raplReading = rapl.getReadings()[socket];
      if (Math.abs(powercapReading.pkg - raplReading.pkg) > 1) {
        LoggerUtil.LOGGER.info(
            String.format(
                "powercap package energy (%f) for socket %d does not match rapl package energy"
                    + " (%f)",
                powercapReading.pkg, socket, raplReading.pkg));
        passed = false;
      }
      if (Math.abs(powercapReading.dram - raplReading.dram) > 1) {
        LoggerUtil.LOGGER.info(
            String.format(
                "powercap dram energy (%f) for socket %d  does not match rapl dram energy (%f)",
                powercapReading.dram, socket, raplReading.dram));
        passed = false;
      }
    }
    return passed;
  }

  public static void main(String[] args) throws Exception {
    if ((raplAvailable() & powercapAvailable()) && checkEquivalence()) {
      LoggerUtil.LOGGER.info("all smoke tests passed!");
    } else {
      LoggerUtil.LOGGER.info("smoke testing failed; please consult the log.");
    }
  }
}
