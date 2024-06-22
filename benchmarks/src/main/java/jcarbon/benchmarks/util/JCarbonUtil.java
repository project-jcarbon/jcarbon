package jcarbon.benchmarks.util;

import static jcarbon.benchmarks.util.LoggerUtil.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonApplicationMonitor;
import jcarbon.signal.Component;
import jcarbon.signal.Report;
import jcarbon.signal.Signal;

public final class JCarbonUtil {
  private static final Logger logger = getLogger();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static final int DEFAULT_PERIOD_MS = 10;
  private static final String OUTPUT_PATH = System.getProperty("jcarbon.benchmarks.output", "/tmp");
  private static final AtomicInteger counter = new AtomicInteger(0);

  public static JCarbon createJCarbon() {
    String period = System.getProperty("jcarbon.benchmarks.period", "10");
    int periodMillis = DEFAULT_PERIOD_MS;
    try {
      periodMillis = Integer.parseInt(period);
    } catch (Exception e) {
      logger.log(Level.INFO, String.format("ignoring bad period (%s) for new JCarbon", period), e);
      return new JCarbonApplicationMonitor(DEFAULT_PERIOD_MS, ProcessHandle.current().pid());
    }
    if (periodMillis < 0) {
      logger.info(String.format("rejecting negative period (%d) for new JCarbon", periodMillis));
      return new JCarbonApplicationMonitor(DEFAULT_PERIOD_MS, ProcessHandle.current().pid());
    }
    logger.info(String.format("creating JCarbon with period of %d milliseconds", periodMillis));
    return new JCarbonApplicationMonitor(periodMillis, ProcessHandle.current().pid());
  }

  public static Path outputPath() {
    return Path.of(
        OUTPUT_PATH,
        String.format(
            "jcarbon-%d-%d.json", ProcessHandle.current().pid(), counter.getAndIncrement()));
  }

  public static void summary(Report report) {
    logger.info("JCarbon report summary:");
    for (Component component : report.getComponentList()) {
      if (component.getComponentType() == "linux_process") {
        for (Signal signal : component.getSignalList()) {
          switch (signal.getUnit()) {
            case GRAMS_OF_CO2:
              logger.info(String.format(" - %.4f grams of CO2", sumSignal(signal, 1)));
              break;
            case JIFFIES:
              logger.info(
                  String.format(
                      " - %.4f%s of cycles",
                      100 * sumSignal(signal, CPU_COUNT) / signal.getIntervalCount(), '%'));
              break;
            case JOULES:
              logger.info(String.format(" - %.4f joules", sumSignal(signal, 1)));
              break;
            default:
              continue;
          }
        }
      }
    }
  }

  public static void writeReports(List<Report> reports) {
    logger.info("writing jcarbon reports");
    for (Report report : reports) {
      Path outputPath = JCarbonUtil.outputPath();
      try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
        report.writeTo(outputStream);
        logger.info(String.format("wrote report to %s", outputPath));
      } catch (IOException e) {
        logger.log(Level.WARNING, "unable to write jcarbon report!", e);
      }
    }
  }

  private static double sumSignal(Signal signal, int factor) {
    return signal.getIntervalList().stream()
        .mapToDouble(
            interval ->
                interval.getDataList().stream().mapToDouble(d -> d.getValue()).sum() / factor)
        .sum();
  }
}
