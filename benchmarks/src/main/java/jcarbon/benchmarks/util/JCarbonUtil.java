package jcarbon.benchmarks.util;

import static java.nio.file.Files.newBufferedWriter;
import static jcarbon.benchmarks.util.LoggerUtil.getLogger;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.emissions.EmissionsInterval;

public final class JCarbonUtil {
  private static final int DEFAULT_PERIOD_MS = 10;
  private static final String OUTPUT_PATH = System.getProperty("jcarbon.benchmarks.output", "/tmp");
  private static final Logger logger = getLogger();

  public static JCarbon createJCarbon() {
    String period = System.getProperty("jcarbon.benchmarks.period", "10");
    int periodMillis = DEFAULT_PERIOD_MS;
    try {
      periodMillis = Integer.parseInt(period);
    } catch (Exception e) {
      logger.log(
          Level.INFO, String.format("ignoring bad period (%s) for new JCarbon", period), e);
      return new JCarbon(DEFAULT_PERIOD_MS);
    }
    if (periodMillis < 0) {
      logger.info(String.format("rejecting negative period (%d) for new JCarbon", periodMillis));
      return new JCarbon(DEFAULT_PERIOD_MS);
    }
    logger.info(String.format("creating JCarbon with period of %d milliseconds", periodMillis));
    return new JCarbon(periodMillis);
  }

  public static Path outputPath() {
    return Path.of(
        OUTPUT_PATH,
        String.format(
            "jcarbon-%d-%d.json", ProcessHandle.current().pid(), System.currentTimeMillis()));
  }

  public static void summary(JCarbonReport report) {
    List<ProcessEnergy> processEnergy = report.getSignal(ProcessEnergy.class);
    double energy =
        processEnergy.stream()
            .mapToDouble(nrg -> nrg.data().stream().mapToDouble(e -> e.energy).sum())
            .sum();
    Instant start = processEnergy.stream().map(ProcessEnergy::start).min(Instant::compareTo).get();
    Instant end = processEnergy.stream().map(ProcessEnergy::end).max(Instant::compareTo).get();
    logger.info("JCarbon report summary:");
    logger.info(
        String.format(" - %.4f seconds", (double) Duration.between(start, end).toMillis() / 1000));
    logger.info(String.format(" - %.4f joules", energy));
    logger.info(
        String.format(" - %.4f watts", 1000 * energy / Duration.between(start, end).toMillis()));
    logger.info(
        String.format(
            " - %.4f grams of CO2",
            report.getSignal(EmissionsInterval.class).stream()
                .mapToDouble(EmissionsInterval::data)
                .sum()));
  }

  public static void dump(List<JCarbonReport> reports) {
    Path outputPath = outputPath();
    logger.info(String.format("writing reports to %s", outputPath));
    try (PrintWriter writer = new PrintWriter(newBufferedWriter(outputPath)); ) {
      writer.println(JsonUtil.toJsonReports(reports));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
