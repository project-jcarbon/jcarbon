package jcarbon.cpu.rapl;

import static java.util.stream.Collectors.joining;
import static jcarbon.util.LoggerUtil.getLogger;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/** A smoke test to check which components are available and if they are reporting similarly. */
final class SmokeTest {
  private static final Logger logger = getLogger();
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
      logger.info("the native library isn't available!");
      return false;
    }

    if (MicroArchitecture.NAME.equals(MicroArchitecture.UNKNOWN)) {
      logger.info("no microarchitecture could be found through rapl!");
      return false;
    }

    if (MicroArchitecture.SOCKETS < 1) {
      logger.info("microarchitecture has no energy domains through rapl!");
      return false;
    }

    RaplSample start = Rapl.sample().get();

    exercise();

    RaplInterval interval = Rapl.difference(start, Rapl.sample().get());

    if (IntStream.range(0, MicroArchitecture.SOCKETS)
            .mapToDouble(socket -> interval.data()[socket].total)
            .sum()
        == 0) {
      logger.info("no energy consumed with the difference of two rapl samples!");
      return false;
    }

    logger.info(
        String.join(
            System.lineSeparator(),
            "rapl report",
            String.format(" - microarchitecture: %s", MicroArchitecture.NAME),
            String.format(
                " - elapsed time: %.6fs",
                (double) Duration.between(interval.start(), interval.end()).toNanos() / 1000000000),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package: %.6fJ, dram: %.6fJ, core: %.6fJ, gpu: %.6fJ",
                            socket + 1,
                            interval.data()[socket].pkg,
                            interval.data()[socket].dram,
                            interval.data()[socket].core,
                            interval.data()[socket].gpu))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  /** Checks if powercap is available for sampling. */
  private static boolean powercapAvailable() throws Exception {
    if (Powercap.SOCKETS < 1) {
      logger.info("system has no energy domains through powercap!");
      return false;
    }

    RaplSample start = Powercap.sample().get();

    exercise();

    RaplInterval interval = Powercap.difference(start, Powercap.sample().get());

    if (IntStream.range(0, MicroArchitecture.SOCKETS)
            .mapToDouble(socket -> interval.data()[socket].total)
            .sum()
        == 0) {
      logger.info("no energy consumed with the difference of two powercap samples!");
      return false;
    }

    logger.info(
        String.join(
            System.lineSeparator(),
            "powercap report",
            String.format(
                " - elapsed time: %.6fs",
                (double) Duration.between(interval.start(), interval.end()).toNanos() / 1000000000),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package: %.6fJ, dram: %.6fJ",
                            socket + 1, interval.data()[socket].pkg, interval.data()[socket].dram))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  /**
   * Checks if rapl and powercap report similarly. This acts as a correctness test since you will be
   * able to determine if values are not sane.
   */
  private static boolean checkEquivalence() throws Exception {
    if (Powercap.SOCKETS != MicroArchitecture.SOCKETS) {
      logger.info(
          String.format(
              "energy domains for powercap (%s) does not match rapl (%d)!",
              Powercap.SOCKETS, MicroArchitecture.SOCKETS));
      return false;
    }

    RaplSample rapl = Rapl.sample().get();
    RaplSample powercap = Powercap.sample().get();

    exercise();

    return isSimilar(
        Powercap.difference(powercap, Powercap.sample().get()),
        Rapl.difference(rapl, Rapl.sample().get()));
  }

  private static boolean isSimilar(RaplInterval powercap, RaplInterval rapl) {
    if (!validateTimestamps(powercap, rapl)) {
      return false;
    }

    if (!validateComponents(powercap, rapl)) {
      return false;
    }

    logger.info(
        String.join(
            System.lineSeparator(),
            "equivalence report",
            String.format(
                " - elapsed time difference: %.6fs",
                Math.abs(
                        (double)
                            (Duration.between(rapl.start(), rapl.end()).toNanos()
                                - Duration.between(powercap.start(), powercap.end()).toNanos()))
                    / 1000000000),
            IntStream.range(0, powercap.data().length)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, package difference: %.6fJ, dram difference: %.6fJ",
                            socket + 1,
                            Math.abs(powercap.data()[socket].pkg - rapl.data()[socket].pkg),
                            Math.abs(powercap.data()[socket].dram - rapl.data()[socket].dram)))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  // TODO: Since Java's time precision is only guaranteed up to milliseconds
  // (https://shorturl.at/cjuL8), there is some potential for mismatch between the microsecond unix
  // time reported by Rapl and the native Java Instant time.
  private static boolean validateTimestamps(RaplInterval powercap, RaplInterval rapl) {
    boolean passed = true;
    if (Duration.between(rapl.start(), powercap.start()).compareTo(ONE_SECOND) > 0) {
      logger.info(
          String.format(
              "powercap start time (%s) does not match rapl start time (%s)",
              powercap.start(), rapl.start()));
      passed = false;
    }

    if (Duration.between(rapl.end(), powercap.end()).compareTo(ONE_SECOND) > 0) {
      logger.info(
          String.format(
              "powercap end time (%s) does not match rapl end time (%s)",
              powercap.end(), rapl.end()));
      passed = false;
    }
    return passed;
  }

  private static boolean validateComponents(RaplInterval powercap, RaplInterval rapl) {
    if (powercap.data().length != rapl.data().length) {
      logger.info(
          String.format(
              "powercap reading count (%s) does not match rapl reading count (%s)",
              powercap.data().length, rapl.data().length));
      return false;
    }

    boolean passed = true;
    for (int socket = 0; socket < powercap.data().length; socket++) {
      RaplReading powercapReading = powercap.data()[socket];
      RaplReading raplReading = rapl.data()[socket];
      if (Math.abs(powercapReading.pkg - raplReading.pkg) > 1) {
        logger.info(
            String.format(
                "powercap package energy (%f) for socket %d does not match rapl package energy"
                    + " (%f)",
                powercapReading.pkg, socket, raplReading.pkg));
        passed = false;
      }
      if (Math.abs(powercapReading.dram - raplReading.dram) > 1) {
        logger.info(
            String.format(
                "powercap dram energy (%f) for socket %d  does not match rapl dram energy (%f)",
                powercapReading.dram, socket, raplReading.dram));
        passed = false;
      }
    }
    return passed;
  }

  public static void main(String[] args) throws Exception {
    logger.info("warming up...");
    for (int i = 0; i < 5; i++) exercise();
    logger.info("testing rapl...");
    if ((raplAvailable() & powercapAvailable()) && checkEquivalence()) {
      logger.info("all smoke tests passed!");
    } else {
      logger.info("smoke testing failed; please consult the log.");
    }
  }
}
