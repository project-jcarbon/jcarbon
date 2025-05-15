package jcarbon.rapl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A sample of rapl energy consumption since boot. */
public final class RaplSample implements Comparable<RaplSample> {
  private final Instant timestamp;
  private final ArrayList<RaplReading> readings = new ArrayList<>();

  RaplSample(Instant timestamp, Iterable<RaplReading> readings) {
    this.timestamp = timestamp;
    readings.forEach(this.readings::add);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public List<RaplReading> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public int compareTo(RaplSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
