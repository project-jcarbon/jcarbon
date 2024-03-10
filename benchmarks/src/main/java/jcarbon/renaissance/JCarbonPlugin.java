package jcarbon.renaissance;

import jcarbon.JCarbon;
import org.renaissance.Plugin;

public final class JCarbonPlugin
    implements Plugin.BeforeBenchmarkTearDownListener,
        Plugin.AfterOperationSetUpListener,
        Plugin.BeforeOperationTearDownListener {
  private final JCarbon jcarbon = new JCarbon();

  @Override
  public void afterOperationSetUp(String benchmark, int opIndex, boolean isLastOp) {
    jcarbon.start();
  }

  @Override
  public void beforeOperationTearDown(String benchmark, int opIndex, long durationNanos) {
    jcarbon.stop();
  }

  @Override
  public void beforeBenchmarkTearDown(String benchmark) {
    // System.out.println("dumping data");
  }
}
