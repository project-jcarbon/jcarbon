package jcarbon.benchmarks;

import java.util.ArrayList;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import org.renaissance.Plugin;

public final class JCarbonPlugin
    implements Plugin.BeforeBenchmarkTearDownListener,
        Plugin.AfterOperationSetUpListener,
        Plugin.BeforeOperationTearDownListener {
  private final JCarbon jcarbon = JCarbonBenchmarkUtil.createJCarbon();
  private final ArrayList<JCarbonReport> reports = new ArrayList<>();

  @Override
  public void afterOperationSetUp(String benchmark, int opIndex, boolean isLastOp) {
    jcarbon.start();
  }

  @Override
  public void beforeOperationTearDown(String benchmark, int opIndex, long durationNanos) {
    jcarbon.stop().ifPresent(reports::add);
    JCarbonBenchmarkUtil.summary(reports.get(reports.size() - 1));
  }

  @Override
  public void beforeBenchmarkTearDown(String benchmark) {
    JCarbonBenchmarkUtil.dump(reports);
    reports.clear();
  }
}
