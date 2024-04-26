package jcarbon.benchmarks.util;

import static java.nio.file.Files.newBufferedWriter;
import static java.util.stream.Collectors.groupingBy;
import static jcarbon.benchmarks.util.LoggerUtil.getLogger;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.cpu.eflect.TaskEnergy;
import jcarbon.cpu.jiffies.ProcessActivity;
import jcarbon.cpu.jiffies.TaskActivity;
import jcarbon.emissions.Emission;
import jcarbon.emissions.Emissions;

public final class JCarbonUtil {
  private static final Logger logger = getLogger();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static final int DEFAULT_PERIOD_MS = 10;
  private static final String OUTPUT_PATH = System.getProperty("jcarbon.benchmarks.output", "/tmp");

  public static JCarbon createJCarbon() {
    String period = System.getProperty("jcarbon.benchmarks.period", "10");
    int periodMillis = DEFAULT_PERIOD_MS;
    try {
      periodMillis = Integer.parseInt(period);
    } catch (Exception e) {
      logger.log(Level.INFO, String.format("ignoring bad period (%s) for new JCarbon", period), e);
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
    List<ProcessActivity> processActivity = report.getSignal(ProcessActivity.class);
    double activity =
        processActivity.stream()
            .mapToDouble(
                nrg -> nrg.data().stream().mapToDouble(TaskActivity::value).sum() / CPU_COUNT)
            .average()
            .getAsDouble();
    List<ProcessEnergy> processEnergy = report.getSignal(ProcessEnergy.class);
    double energy =
        processEnergy.stream()
            .mapToDouble(nrg -> nrg.data().stream().mapToDouble(TaskEnergy::value).sum())
            .sum();
    Instant start = processEnergy.stream().map(ProcessEnergy::start).min(Instant::compareTo).get();
    Instant end = processEnergy.stream().map(ProcessEnergy::end).max(Instant::compareTo).get();
    logger.info("JCarbon report summary:");
    logger.info(
        String.format(" - %.4f seconds", (double) Duration.between(start, end).toMillis() / 1000));
    logger.info(String.format(" - %.4f%s of cycles", activity, '%'));
    logger.info(String.format(" - %.4f joules", energy));
    logger.info(
        String.format(" - %.4f watts", 1000 * energy / Duration.between(start, end).toMillis()));
    report.getSignal(Emissions.class).stream()
        .collect(groupingBy(emissions -> emissions.component()))
        .forEach(
            (component, signal) ->
                logger.info(
                    String.format(
                        " - %.4f grams of CO2 consumed by %s",
                        signal.stream()
                            .mapToDouble(
                                emissions ->
                                    emissions.data().stream().mapToDouble(Emission::value).sum())
                            .sum(),
                        component)));
    report.getSignal(Emissions.class).stream()
        .filter(emissions -> emissions.component().contains("process="))
        .flatMap(emissions -> emissions.data().stream())
        .collect(groupingBy(emission -> emission.component()))
        .forEach(
            (component, emissions) ->
                logger.info(
                    String.format(
                        " - %.4f grams of CO2 consumed by %s",
                        emissions.stream().mapToDouble(Emission::value).sum(), component)));
  }

  public static void dump(List<JCarbonReport> reports) {
    Path outputPath = outputPath();
    logger.info(String.format("writing reports to %s", outputPath));
    try (PrintWriter writer = new PrintWriter(newBufferedWriter(outputPath)); ) {
      writer.write("[");
      int i = 0;
      for (JCarbonReport report : reports) {
        Set<Class<?>> signalTypes = report.getSignalTypes();
        int j = 0;
        writer.println("{");
        for (Class<?> signalType : signalTypes) {
          // System.out.println("?");
          List<?> signal = report.getSignal(signalType);
          writer.println(String.format("  \"%s\":[", signalType.getName()));
          int k = 0;
          for (Object data : signal) {
            writer.write("    " + data.toString());
            if (k + 1 < signal.size()) {
              writer.println(",");
              k++;
            }
          }
          writer.write("]");
          if (j + 1 < signalTypes.size()) {
            writer.println(",");
            j++;
          } else {
            writer.println();
          }
        }
        writer.write("}");
        if (i + 1 < reports.size()) {
          writer.println(",");
          i++;
        }
      }
      writer.println("]");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
