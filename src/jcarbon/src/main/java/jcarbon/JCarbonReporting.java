package jcarbon;

import static java.nio.file.Files.newBufferedWriter;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import jcarbon.util.LoggerUtil;

public final class JCarbonReporting {
  private static final Logger logger = LoggerUtil.getLogger();

  public static void writeReport(JCarbonReport report, Path outputPath) {
    logger.info(String.format("writing report to %s", outputPath));
    try (PrintWriter writer = new PrintWriter(newBufferedWriter(outputPath)); ) {
      Set<Class<?>> signalTypes = report.getSignalTypes();
      int j = 0;
      writer.write("[");
      for (Class<?> signalType : signalTypes) {
        writer.println("{");
        List<?> signal = report.getSignal(signalType);
        writer.println(
            String.format("  \"signal_name\":\"%s\",\"signal\":[", signalType.getName()));
        int k = 0;
        for (Object data : signal) {
          writer.write("    " + data.toString());
          if (k + 1 < signal.size()) {
            writer.println(",");
            k++;
          }
        }
        writer.write("]");
        writer.write("}");
        if (j + 1 < signalTypes.size()) {
          writer.println(",");
          j++;
        } else {
          writer.println();
        }
      }
      writer.write("]");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeReports(List<JCarbonReport> reports, Path outputPath) {
    logger.info(String.format("writing reports to %s", outputPath));
    try (PrintWriter writer = new PrintWriter(newBufferedWriter(outputPath)); ) {
      writer.write("[");
      int i = 0;
      for (JCarbonReport report : reports) {
        Set<Class<?>> signalTypes = report.getSignalTypes();
        int j = 0;
        writer.write("{\"signal\":[");
        for (Class<?> signalType : signalTypes) {
          List<?> signal = report.getSignal(signalType);
          writer.println("{");
          writer.println(
              String.format("  \"signal_name\":\"%s\",\"signal\":[", signalType.getName()));
          int k = 0;
          for (Object data : signal) {
            writer.write("    " + data.toString());
            if (k + 1 < signal.size()) {
              writer.println(",");
              k++;
            }
          }
          writer.write("]");
          writer.write("}");
          if (j + 1 < signalTypes.size()) {
            writer.println(",");
            j++;
          } else {
            writer.println();
          }
        }
        writer.write("]}");
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

  private JCarbonReporting() {}
}
