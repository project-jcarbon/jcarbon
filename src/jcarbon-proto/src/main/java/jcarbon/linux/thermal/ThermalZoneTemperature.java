package jcarbon.linux.thermal;

/** A reading from a thermal sysfs system. */
public final class ThermalZoneTemperature {
  // TODO: immutable data structures are "safe" as public
  public final int zone;
  public final String type;
  public final int temperature;

  ThermalZoneTemperature(int zone, String type, int temperature) {
    this.zone = zone;
    this.type = type;
    this.temperature = temperature;
  }
}
