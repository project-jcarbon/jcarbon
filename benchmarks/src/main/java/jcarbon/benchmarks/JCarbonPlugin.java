package jcarbon.benchmarks;

import java.util.ArrayList;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import jcarbon.benchmarks.util.JCarbonUtil;
import org.renaissance.Plugin;

public final class JCarbonPlugin
    implements Plugin.BeforeBenchmarkTearDownListener,
        Plugin.AfterOperationSetUpListener,
        Plugin.BeforeOperationTearDownListener {
  private final JCarbon jcarbon = JCarbonUtil.createJCarbon();
  private final ArrayList<JCarbonReport> reports = new ArrayList<>();

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
    JCarbonUtil.dump(reports);
    reports.clear();
  }
}
