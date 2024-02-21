package jrapl.benchmarks;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jrapl.EnergyInterval;
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
    private final AtomicInteger currentIteration = new AtomicInteger();

    @Setup(Level.Iteration)
    public void iterationStart() {
      if (!samples.containsKey(currentIteration.get())) {
        samples.put(currentIteration.get(), new ArrayList<>());
      }
    }

    @TearDown(Level.Iteration)
    public void iterationEnd() {
      currentIteration.getAndIncrement();
    }

    @TearDown(Level.Trial)
    public void trialEnd() {
      // TODO: this feels like it should be a profiler that is reported with the jmh results
      samples.entrySet().stream()
          .forEach(
              e -> {
                List<EnergySample> s =
                    e.getValue().stream().map(Rapl::readingToSample).collect(toList());
                ArrayList<EnergyInterval> intervals = new ArrayList<>();
                for (int i = 0; i < s.size() - 1; i++) {
                  intervals.add(Rapl.difference(s.get(i), s.get(i + 1)));
                }
                try (FileWriter writer =
                    new FileWriter(
                        String.format("/tmp/jrapl-energy-update-benchmark-%d.json", e.getKey()))) {
                  writer.write(
                      String.format(
                          "[%s]",
                          intervals.stream().map(EnergyInterval::toString).collect(joining(","))));
                } catch (Exception err) {
                  System.out.println("couldn't write data!");
                }
              });
    }

    public void sample() {
      samples.get(currentIteration.get()).add(Rapl.read());
    }
  }

  @Benchmark
  public void sample(SamplingState state) {
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
  }
}
