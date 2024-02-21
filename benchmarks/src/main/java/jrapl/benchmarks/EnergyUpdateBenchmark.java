package jrapl.benchmarks;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import jrapl.EnergySample;
import jrapl.Rapl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** A benchmark to test how frequently the rapl msr is updated. */
public class EnergyUpdateBenchmark {
  private static final HashMap<Integer, ArrayList<double[]>> samples = new HashMap<>();

  @State(Scope.Benchmark)
  public static class SamplingState {

    private AtomicInteger currentIteration = new AtomicInteger();

    @TearDown(Level.Iteration)
    public void iterationStart() {
      samples.computeIfAbsent(currentIteration.get(), unused -> new ArrayList<>());
    }

    @TearDown(Level.Iteration)
    public void iterationEnd() {
      currentIteration.getAndIncrement();
    }

    public void sample() {
      samples.get(currentIteration.get()).add(Rapl.read());
    }
  }

  @Benchmark
  public void postProcessAddSample(SamplingState state) {
    state.sample();
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(EnergyUpdateBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(25)
            .mode(Mode.Throughput)
            .build();

    new Runner(opt).run();

    System.out.println(
        String.format(
            "{%s}",
            samples.entrySet().stream()
                .map(
                    e ->
                        String.format(
                            "\"%s\",[%s]",
                            e.getKey(),
                            e.getValue().stream()
                                .map(Rapl::readingToSample)
                                .map(EnergySample::toString)
                                .collect(joining(","))))
                .collect(joining(","))));
  }
}
