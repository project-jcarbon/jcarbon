package jcarbon.emissions;

import java.time.Instant;
import jcarbon.data.Component;
import jcarbon.data.Interval;

/** An {@link Interval} of emission consumption over a time range. */
public final class EmissionsInterval implements Interval<Double> {
  private final Instant start;
  private final Instant end;
  private final Component component;
  private final double emissions;

  public EmissionsInterval(Instant start, Instant end, Component component, double emissions) {
    this.start = start;
    this.end = end;
    this.component = component;
    this.emissions = emissions;
  }

  @Override
  public Instant start() {
    return start;
  }

  @Override
  public Instant end() {
    return end;
  }

  @Override
  public Component component() {
    return component;
  }

  @Override
  public Double data() {
    return Double.valueOf(emissions);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":{\"seconds\":%d,\"nanos\":%d},\"data\":[%s]}",
        start.getEpochSecond(), start.getNano(), end.getEpochSecond(), end.getNano(), emissions);
  }
}
