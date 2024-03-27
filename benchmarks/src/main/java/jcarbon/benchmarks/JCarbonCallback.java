package jcarbon.benchmarks;

import java.util.ArrayList;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public class JCarbonCallback extends Callback {
  private final JCarbon jcarbon = JCarbonBenchmarkUtil.createJCarbon();
  private final ArrayList<JCarbonReport> reports = new ArrayList<>();

  public JCarbonCallback(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    jcarbon.start();
    super.start(benchmark);
  }

  @Override
  public void complete(String benchmark, boolean valid, boolean warmup) {
    super.complete(benchmark, valid, warmup);
    jcarbon.stop().ifPresent(reports::add);
    JCarbonBenchmarkUtil.summary(reports.get(reports.size() - 1));
  }

  @Override
  public boolean runAgain() {
    // if we have run every iteration, dump the data and terminate
    if (!super.runAgain()) {
      JCarbonBenchmarkUtil.dump(reports);
      return false;
    } else {
      return true;
    }
  }
}
