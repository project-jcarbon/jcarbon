package jcarbon.benchmarks.data;

public final class Uncertainty {
  public final double average;
  public final double deviation;

  public Uncertainty(double average, double deviation) {
    this.average = average;
    this.deviation = deviation;
  }

  @Override
  public String toString() {
    return String.format("%.6f +/- %.6f", average, deviation);
  }
}
