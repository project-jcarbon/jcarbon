package jcarbon.benchmarks.data;

import java.util.Arrays;
import java.util.Collection;

public final class UncertaintyPropagation {
  public static Uncertainty average(int[] values) {
    double average = Arrays.stream(values).average().getAsDouble();
    double deviation =
        Math.sqrt(
            Arrays.stream(values).mapToDouble(d -> (double) d - average).map(d -> d * d).sum()
                / values.length);
    return new Uncertainty(average, deviation);
  }

  public static Uncertainty average(long[] values) {
    double average = Arrays.stream(values).average().getAsDouble();
    double deviation =
        Math.sqrt(
            Arrays.stream(values).mapToDouble(d -> (double) d - average).map(d -> d * d).sum()
                / values.length);
    return new Uncertainty(average, deviation);
  }

  public static Uncertainty average(double[] values) {
    double average = Arrays.stream(values).average().getAsDouble();
    double deviation =
        Math.sqrt(
            Arrays.stream(values).map(d -> d - average).map(d -> d * d).sum() / values.length);
    return new Uncertainty(average, deviation);
  }

  public static Uncertainty average(Collection<Uncertainty> uncertainties) {
    double average =
        uncertainties.stream().mapToDouble(u -> u.average).sum() / uncertainties.size();
    double deviation =
        Math.sqrt(
            uncertainties.stream().mapToDouble(u -> u.deviation * u.deviation).sum()
                / uncertainties.size());
    return new Uncertainty(average, deviation);
  }

  public static Uncertainty average(Uncertainty first, Uncertainty second, Uncertainty... others) {
    double average = first.average + second.average;
    for (Uncertainty other : others) {
      average += other.average;
    }
    average /= others.length + 2;
    double deviation = first.deviation * first.deviation + second.deviation * second.deviation;
    for (Uncertainty other : others) {
      deviation += other.deviation * other.deviation;
    }
    deviation = Math.sqrt(deviation / others.length + 2);
    return new Uncertainty(average, deviation);
  }
}
