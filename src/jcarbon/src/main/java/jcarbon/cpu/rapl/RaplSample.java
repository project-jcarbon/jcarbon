package jcarbon.cpu.rapl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Sample;

/** A {@link Sample} of rapl energy consumption since boot. */
public final class RaplSample implements Sample<RaplReading>, Comparable<RaplSample> {
  private final Instant timestamp;
  private final ArrayList<RaplReading> readings = new ArrayList<>();

  RaplSample(Instant timestamp, Iterable<RaplReading> readings) {
    this.timestamp = timestamp;
    readings.forEach(this.readings::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public String component() {
    return LinuxComponents.OS_COMPONENT;
  }

  @Override
  public List<RaplReading> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return toJson();
  }

  @Override
  public int compareTo(RaplSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
