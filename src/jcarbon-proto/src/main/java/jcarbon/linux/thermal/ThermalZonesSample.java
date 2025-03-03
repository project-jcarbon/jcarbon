package jcarbon.linux.thermal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Sample;
import jcarbon.data.Unit;

/** A sample from the thermal sysfs system that represents the current temperatures ordered by zone id. */
public final class ThermalZonesSample
    implements Sample<ThermalZone>, Comparable<ThermalZonesSample> {
  private final Instant timestamp;
  private final ArrayList<ThermalZone> temperatures = new ArrayList<>();

  ThermalZonesSample(Instant timestamp, Iterable<ThermalZone> temperatures) {
    this.timestamp = timestamp;
    temperatures.forEach(this.temperatures::add);
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
  public Unit unit() {
    return Unit.CELSIUS;
  }

  @Override
  public List<ThermalZone> data() {
    return new ArrayList<>(temperatures);
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public int compareTo(ThermalZonesSample other) {
    return timestamp().compareTo(other.timestamp());
  }

}