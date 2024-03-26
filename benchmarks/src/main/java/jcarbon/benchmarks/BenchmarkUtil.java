package jcarbon.benchmarks;

import static jcarbon.util.LoggerUtil.getLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcarbon.JCarbonReport;

final class BenchmarkUtil {
  private static final Logger logger = getLogger();
  private static final String OUTPUT_PATH = System.getProperty("jcarbon.benchmarks.output", "/tmp");

  static Path outputPath() {
    return Path.of(
        OUTPUT_PATH,
        String.format(
            "jcarbon-%d-%d.json", ProcessHandle.current().pid(), System.currentTimeMillis()));
  }

  static void dump(List<JCarbonReport> reports) {
    Path outputPath = outputPath();
    logger.info(String.format("writing reports to %s", outputPath));
    // TODO: ugly json encoding
    ArrayList<String> records = new ArrayList<>();
    records.add("[");
    reports.forEach(r -> records.addAll(BenchmarkUtil.toJson(r)));
    records.remove(records.size() - 1);
    records.add("]");

    try {
      Files.write(outputPath, records);
    } catch (Exception e) {
      logger.log(Level.INFO, String.format("unable to write reports to %s", OUTPUT_PATH), e);
    }
  }

  private static List<String> toJson(JCarbonReport report) {
    ArrayList<String> records = new ArrayList<>();
    records.add("{");
    report
        .getSignals()
        .forEach(
            (signal, data) -> {
              records.add(String.format("\"%s\":[", signal.getSimpleName()));
              data.stream()
                  .forEach(
                      o -> {
                        records.add(o.toString());
                        records.add(",");
                      });
              records.remove(records.size() - 1);
              records.add("]");
              records.add(",");
            });
    records.remove(records.size() - 1);
    records.add("}");
    records.add(",");
    return records;
  }
}
