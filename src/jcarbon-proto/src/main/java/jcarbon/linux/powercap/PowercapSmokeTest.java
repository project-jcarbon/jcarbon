package jcarbon.linux.powercap;

import static java.util.stream.Collectors.joining;
import static jcarbon.util.LoggerUtil.getLogger;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import jcarbon.signal.SignalInterval;
import jcarbon.util.Timestamps;

/** A smoke test to check which components are available and if they are reporting similarly. */
final class PowercapSmokeTest {
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

  /** Checks if powercap is available for sampling. */
  private static boolean powercapAvailable() throws Exception {
    if (Powercap.SOCKETS < 1) {
      logger.info("system has no energy domains through powercap!");
      return false;
    }

    PowercapSample start = Powercap.sample().get();

    exercise();

    SignalInterval interval = Powercap.difference(start, Powercap.sample().get());

    List<SignalInterval.SignalData> readings = interval.getDataList();
    if (IntStream.range(0, Powercap.SOCKETS)
            .mapToDouble(socket -> readings.get(socket).getValue())
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
                (double) Timestamps.between(interval.getStart(), interval.getEnd()).toNanos()
                    / 1000000000),
            IntStream.range(0, Powercap.SOCKETS)
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
    if (powercapAvailable()) {
      logger.info("all smoke tests passed!");
    } else {
      logger.info("smoke testing failed; please consult the log.");
    }
  }
}
