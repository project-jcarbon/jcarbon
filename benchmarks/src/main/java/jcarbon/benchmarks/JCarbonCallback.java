package jcarbon.benchmarks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import jcarbon.JCarbon;
import jcarbon.benchmarks.util.JCarbonUtil;
import jcarbon.signal.Report;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public class JCarbonCallback extends Callback {
  private final JCarbon jcarbon = JCarbonUtil.createJCarbon();
  private final ArrayList<Report> reports = new ArrayList<>();

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
    JCarbonUtil.summary(reports.get(reports.size() - 1));
  }

  @Override
  public boolean runAgain() {
    // if we have run every iteration, dump the data and terminate
    if (!super.runAgain()) {
      System.out.println("writing jcarbon reports");
      for (Report report : reports) {
        try (OutputStream outputStream = Files.newOutputStream(JCarbonUtil.outputPath())) {
          report.writeTo(outputStream);
        } catch (IOException e) {
          System.out.println("unable to write jcarbon report!");
        }
      }
      return false;
    } else {
      return true;
    }
  }
}
