package jcarbon.benchmarks;

import static java.util.stream.Collectors.joining;
import static jcarbon.util.LoggerUtil.getLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcarbon.JCarbonReport;

final class BenchmarkUtil {
  private static final Logger logger = getLogger();
  private static final String OUTPUT_PATH = System.getProperty("jcarbon.benchmarks.output", "/tmp");

  static void dump(List<JCarbonReport> reports) {
    Path outputPath =
        Path.of(
            OUTPUT_PATH,
            String.format(
                "jcarbon-%d-%d.json", ProcessHandle.current().pid(), System.currentTimeMillis()));
    logger.info(String.format("writing reports to %s", outputPath));
    try {
      Files.writeString(
          outputPath,
          String.format(
              "[%s]", reports.stream().map(JCarbonReport::toString).collect(joining(","))));
    } catch (Exception e) {
      logger.log(Level.INFO, String.format("unable to write reports to %s", OUTPUT_PATH), e);
    }
  }
}
