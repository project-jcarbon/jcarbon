package jcarbon.linux.thermal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A sample from the thermal sysfs system that represents the current temperatures ordered by zone
 * id.
 */
public final class ThermalZonesSample implements Comparable<ThermalZonesSample> {
  private final Instant timestamp;
  private final ArrayList<ThermalZoneTemperature> readings = new ArrayList<>();

  ThermalZonesSample(Instant timestamp, Iterable<ThermalZoneTemperature> readings) {
    this.timestamp = timestamp;
    readings.forEach(this.readings::add);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public List<ThermalZoneTemperature> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public int compareTo(ThermalZonesSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
