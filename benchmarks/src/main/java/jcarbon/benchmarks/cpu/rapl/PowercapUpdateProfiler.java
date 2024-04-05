package jcarbon.benchmarks.cpu.rapl;

import static java.util.stream.Collectors.toList;
import static jcarbon.data.DataOperations.forwardApply;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import jcarbon.cpu.rapl.Powercap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

public final class PowercapUpdateProfiler implements ExternalProfiler {
  public PowercapUpdateProfiler() {}

  @Override
  public String getDescription() {
    return "powercap-update";
  }

  @Override
  public void beforeTrial(BenchmarkParams benchmarkParams) {}

  @Override
  public Collection<? extends Result> afterTrial(
      BenchmarkResult br, long pid, File stdOut, File stdErr) {
    List<ScalarResult> results =
        forwardApply(
            forwardApply(PowercapUpdateBenchmark.samples, Powercap::difference).stream()
                .filter(
                    interval -> Arrays.stream(interval.data()).mapToDouble(r -> r.total).sum() > 0)
                .collect(toList()),
            (i1, i2) ->
                new ScalarResult(
                    "msr-update-time",
                    Duration.between(i1.end(), i2.start()).toMillis(),
                    "ms",
                    AggregationPolicy.AVG));
    PowercapUpdateBenchmark.samples.clear();
    return results;
  }

  @Override
  public boolean allowPrintErr() {
    return false;
  }

  @Override
  public boolean allowPrintOut() {
    return false;
  }

  @Override
  public Collection<String> addJVMInvokeOptions(BenchmarkParams params) {
    return List.of();
  }

  @Override
  public Collection<String> addJVMOptions(BenchmarkParams params) {
    return List.of();
  }
}
