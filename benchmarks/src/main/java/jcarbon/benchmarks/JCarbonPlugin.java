package jcarbon.benchmarks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import jcarbon.JCarbon;
import jcarbon.benchmarks.util.JCarbonUtil;
import jcarbon.signal.Report;
import org.renaissance.Plugin;

public final class JCarbonPlugin
    implements Plugin.BeforeBenchmarkTearDownListener,
        Plugin.AfterOperationSetUpListener,
        Plugin.BeforeOperationTearDownListener {
  private final JCarbon jcarbon = JCarbonUtil.createJCarbon();
  private final ArrayList<Report> reports = new ArrayList<>();

  @Override
  public void afterOperationSetUp(String benchmark, int opIndex, boolean isLastOp) {
    jcarbon.start();
  }

  @Override
  public void beforeOperationTearDown(String benchmark, int opIndex, long durationNanos) {
    jcarbon.stop().ifPresent(reports::add);
    JCarbonUtil.summary(reports.get(reports.size() - 1));
  }

  @Override
  public void beforeBenchmarkTearDown(String benchmark) {
    System.out.println("writing jcarbon reports");
    for (Report report : reports) {
      try (OutputStream outputStream = Files.newOutputStream(JCarbonUtil.outputPath())) {
        report.writeTo(outputStream);
      } catch (IOException e) {
        System.out.println("unable to write jcarbon report!");
      }
    }
    reports.clear();
  }
}
