package jcarbon.benchmarks.data;

import java.util.Arrays;

public final class Uncertainty {
  public static Uncertainty ofInts(int[] values) {
    double average = Arrays.stream(values).average().getAsDouble();
    double deviation =
        Math.sqrt(
            Arrays.stream(values)
                    .mapToDouble(value -> (double) value - average)
                    .map(value -> value * value)
                    .sum()
                / values.length);
    return new Uncertainty(average, deviation);
  }

  public static Uncertainty ofLongs(long[] values) {
    double average = Arrays.stream(values).average().getAsDouble();
    double deviation =
        Math.sqrt(
            Arrays.stream(values)
                    .mapToDouble(value -> (double) value - average)
                    .map(value -> value * value)
                    .sum()
                / values.length);
    return new Uncertainty(average, deviation);
  }

  public static Uncertainty ofDoubles(double[] values) {
    double average = Arrays.stream(values).average().getAsDouble();
    double deviation =
        Math.sqrt(
            Arrays.stream(values).map(value -> value - average).map(value -> value * value).sum()
                / values.length);
    return new Uncertainty(average, deviation);
  }

  public final double average;
  public final double deviation;

  Uncertainty(double average, double deviation) {
    this.average = average;
    this.deviation = deviation;
  }

  @Override
  public String toString() {
    return String.format("%.6f ± %.6f", average, deviation);
  }
}
