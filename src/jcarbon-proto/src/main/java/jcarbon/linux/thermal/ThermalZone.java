package jcarbon.linux.thermal;

// import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Data;

/** A reading from a thermal sysfs system. */
public final class ThermalZone implements Data {
  // TODO: immutable data structures are "safe" as public
  public final int zone;
  public final String type;
  public final int temperature;

  ThermalZone(int zone, String type, int temperature) {
    this.zone = zone;
    this.type = type;
    this.temperature = temperature;
  }

  @Override
  public String component() {
    return type;
  }

  @Override
  public double value() {
    return temperature;
  }

  @Override
  public String toString() {
    return toJson();
  }
}