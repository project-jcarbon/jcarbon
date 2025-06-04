package jcarbon.linux.powercap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A {@link Sample} of powercap energy consumption since boot. */
public final class PowercapSample implements Comparable<PowercapSample> {
  private final Instant timestamp;
  private final ArrayList<PowercapReading> readings = new ArrayList<>();

  PowercapSample(Instant timestamp, Iterable<PowercapReading> readings) {
    this.timestamp = timestamp;
    readings.forEach(this.readings::add);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public List<PowercapReading> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public int compareTo(PowercapSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
