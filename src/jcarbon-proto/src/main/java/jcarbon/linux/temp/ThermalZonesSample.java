package jcarbon.linux.temp;

import static java.util.stream.Collectors.toMap;
import static jcarbon.util.Timestamps.fromInstant;
import static jcarbon.util.Timestamps.nowAsInstant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Sample;
import jcarbon.data.Unit;

import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.signal.SignalInterval.Timestamp;


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

  public static SignalInterval thermalZoneDifference(
      ThermalZonesSample first, ThermalZonesSample second) {
    return SignalInterval.newBuilder()
        .setStart(fromInstant(first.timestamp()))
        .setEnd(fromInstant(second.timestamp()))
        .addAllData(difference(first.data(), second.data()))
        .build();
  }

  public static List<SignalData> difference(List<ThermalZone> first, List<ThermalZone> second) {
    Map<Integer, ThermalZone> secondMap = second.stream().collect(toMap(r -> r.zone, r -> r));
    ArrayList<SignalData> temperatures = new ArrayList<>();
    for (ThermalZone task : first) {
      if (secondMap.containsKey(task.zone)) {
        ThermalZone other = secondMap.get(task.zone);
        temperatures.add(
            SignalData.newBuilder()
                .addMetadata(
                    SignalData.Metadata.newBuilder()
                        .setName("zone")
                        .setValue(Integer.toString(task.zone)))
                .addMetadata(
                    SignalData.Metadata.newBuilder()
                        .setName("type")
                        .setValue(task.type))
                .setValue(other.temperature / 1000)
                .build());
      }
    }
    return temperatures;
  }
}