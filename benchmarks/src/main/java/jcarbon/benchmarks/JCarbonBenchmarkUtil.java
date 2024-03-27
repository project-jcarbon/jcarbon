package jcarbon.benchmarks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.emissions.EmissionsInterval;

final class JCarbonBenchmarkUtil {
  private static final Logger logger = getLogger();
  private static final String OUTPUT_PATH = System.getProperty("jcarbon.benchmarks.output", "/tmp");

  static JCarbon createJCarbon() {
    String period = System.getProperty("jcarbon.benchmarks.period");
    int periodMillis = 10;
    try {
      periodMillis = Integer.parseInt(period);
      if (periodMillis < 0) {
        logger.info(String.format("rejecting negative period (%d) for new JCarbon", periodMillis));
        periodMillis = 10;
      }
    } catch (Exception e) {
      logger.log(
          Level.INFO, String.format("ignoring bad period value (%s) for new JCarbon", period), e);
      periodMillis = 10;
    }
    logger.info(String.format("creating JCarbon with period of %d milliseconds", periodMillis));
    return new JCarbon(periodMillis);
  }

  static Path outputPath() {
    return Path.of(
        OUTPUT_PATH,
        String.format(
            "jcarbon-%d-%d.json", ProcessHandle.current().pid(), System.currentTimeMillis()));
  }

  static void summary(JCarbonReport report) {
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

  static void dump(List<JCarbonReport> reports) {
    Path outputPath = outputPath();
    logger.info(String.format("writing reports to %s", outputPath));
    // TODO: ugly json encoding
    ArrayList<String> records = new ArrayList<>();
    records.add("[");
    reports.forEach(r -> records.addAll(JCarbonBenchmarkUtil.toJson(r)));
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

  private static final SimpleDateFormat dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");

  private static String makePrefix(Date date) {
    return String.join(
        " ",
        "jcarbon-benchmarks",
        "(" + dateFormatter.format(date) + ")",
        "[" + Thread.currentThread().getName() + "]:");
  }

  private static Logger getLogger() {
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(
        new Formatter() {
          @Override
          public String format(LogRecord record) {
            return String.join(
                " ",
                makePrefix(new Date(record.getMillis())),
                record.getMessage(),
                System.lineSeparator());
          }
        });

    Logger logger = Logger.getLogger("jcarbon-benchmarks");
    logger.setUseParentHandlers(false);

    for (Handler hdlr : logger.getHandlers()) {
      logger.removeHandler(hdlr);
    }
    logger.addHandler(handler);

    return logger;
  }
}
