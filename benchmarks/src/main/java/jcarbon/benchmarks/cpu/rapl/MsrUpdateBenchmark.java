package jcarbon.benchmarks.cpu.rapl;

import java.util.ArrayList;
import jcarbon.cpu.rapl.Rapl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** A benchmark to test how frequently the rapl msr is updated. */
public class MsrUpdateBenchmark {
  static final ArrayList<double[]> samples = new ArrayList<>();

  @Benchmark
  public void sample() {
    samples.add(Rapl.read());
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(MsrUpdateBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(25)
            .mode(Mode.Throughput)
            .addProfiler(MsrUpdateProfiler.class)
            .build();

    new Runner(opt).run();
  }
}
