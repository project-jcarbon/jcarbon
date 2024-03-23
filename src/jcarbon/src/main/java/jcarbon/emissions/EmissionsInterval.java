package jcarbon.emissions;

import java.time.Instant;
import jcarbon.data.Interval;

/** An {@link Interval} of emission consumption over a time range. */
public final class EmissionsInterval implements Interval<Double> {
  private final Instant start;
  private final Instant end;
  private final double emissions;

  public EmissionsInterval(Instant start, Instant end, double emissions) {
    this.start = start;
    this.end = end;
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
  public Double data() {
    return Double.valueOf(emissions);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":{\"seconds\":%d,\"nanos\":%d},\"emissions\":[%s]}",
        start.getEpochSecond(), start.getNano(), end.getEpochSecond(), end.getNano(), emissions);
  }
}
