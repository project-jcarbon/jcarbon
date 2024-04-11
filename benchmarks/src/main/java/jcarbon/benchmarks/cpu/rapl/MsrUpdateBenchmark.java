package jcarbon.benchmarks.cpu.rapl;

import static java.util.stream.Collectors.toList;
import static jcarbon.data.DataOperations.forwardApply;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jcarbon.benchmarks.data.Uncertainty;
import jcarbon.benchmarks.data.UncertaintyPropagation;
import jcarbon.cpu.rapl.Rapl;
import jcarbon.cpu.rapl.RaplEnergy;
import jcarbon.cpu.rapl.RaplSample;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** A benchmark to test how frequently the rapl msr is updated. */
public class MsrUpdateBenchmark {
  private static final int WARMUP_ITERATIONS = 20;
  private static final int MEASUREMENT_ITERATIONS = 50;

  @State(Scope.Benchmark)
  public static class State_ {
    @Param({"0", "1", "2", "3", "4", "8", "16"})
    public int sleepTimeMs;

    private final ArrayList<RaplSample> samples = new ArrayList<>(65536);
    private final Map<String, ArrayList<Uncertainty>> values =
        Map.of(
            "energy", new ArrayList<>(),
            "time", new ArrayList<>());

    @TearDown(Level.Iteration)
    public void computeValues() {
      List<RaplEnergy> intervals =
          forwardApply(samples, Rapl::difference).stream()
              .filter(
                  interval -> Arrays.stream(interval.data()).mapToDouble(r -> r.total).sum() > 0)
              .collect(toList());
      samples.clear();

      Uncertainty energy =
          Uncertainty.ofDoubles(
              intervals.stream()
                  .mapToDouble(i -> Arrays.stream(i.data()).mapToDouble(e -> e.total).sum())
                  .toArray());
      Uncertainty time =
          Uncertainty.ofLongs(
              forwardApply(intervals, (i1, i2) -> Duration.between(i1.end(), i2.start())).stream()
                  .mapToLong(d -> d.toMillis())
                  .toArray());
      // System.out.println(String.format("energy update: %s, update time: %s", energy, time));
      values.get("energy").add(energy);
      values.get("time").add(time);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
      Uncertainty energy =
          UncertaintyPropagation.average(
              values.get("energy").stream().skip(WARMUP_ITERATIONS).collect(toList()));
      Uncertainty time =
          UncertaintyPropagation.average(
              values.get("time").stream().skip(WARMUP_ITERATIONS).collect(toList()));
      System.out.println(String.format("energy update: %s, update time: %s", energy, time));
      values.get("energy").clear();
      values.get("time").clear();
    }
  }

  @Benchmark
  public void sample(State_ state) throws Exception {
    Instant start = null;
    if (state.sleepTimeMs > 0) {
      start = Instant.now();
    }
    state.samples.add(Rapl.sample().get());
    if (state.sleepTimeMs > 0 && start != null) {
      Thread.sleep(Duration.between(start, start.plusNanos(1000 * state.sleepTimeMs)).toMillis());
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(MsrUpdateBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(WARMUP_ITERATIONS)
            .measurementIterations(MEASUREMENT_ITERATIONS)
            .mode(Mode.Throughput)
            .build();

    new Runner(opt).run();
  }
}
