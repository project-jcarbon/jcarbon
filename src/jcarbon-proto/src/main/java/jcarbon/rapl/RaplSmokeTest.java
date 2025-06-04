package jcarbon.rapl;

import static java.util.stream.Collectors.joining;
import static jcarbon.util.LoggerUtil.getLogger;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.util.Timestamps;

/** A smoke test to check which components are available and if they are reporting similarly. */
final class RaplSmokeTest {
  private static final Logger logger = getLogger();

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

    SignalInterval interval = Rapl.difference(start, Rapl.sample().get());

    List<SignalData> readings = interval.getDataList();
    if (IntStream.range(0, MicroArchitecture.SOCKETS)
            .mapToDouble(socket -> readings.get(socket).getValue())
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
                (double) Timestamps.between(interval.getStart(), interval.getEnd()).toNanos()
                    / 1000000000),
            IntStream.range(0, MicroArchitecture.SOCKETS)
                .mapToObj(
                    socket ->
                        String.format(
                            " - socket: %d, energy: %.6fJ",
                            socket + 1, readings.get(socket).getValue()))
                .collect(joining(System.lineSeparator()))));
    return true;
  }

  public static void main(String[] args) throws Exception {
    logger.info("warming up...");
    for (int i = 0; i < 5; i++) exercise();
    logger.info("testing rapl...");
    if (raplAvailable()) {
      logger.info("smoke test passed!");
    } else {
      logger.info("smoke testing failed; please consult the log.");
    }
  }
}
