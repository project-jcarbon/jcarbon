package jcarbon.linux.thermal;

import static java.util.stream.Collectors.toMap;
import static jcarbon.util.LoggerUtil.getLogger;
import static jcarbon.util.Timestamps.fromInstant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;

/**
 * A simple (unsafe) wrapper for reading the thermal sysfs system. Consult
 * https://www.kernel.org/doc/Documentation/thermal/sysfs-api.txt for more details.
 */
public final class SysThermal {
  private static final Logger logger = getLogger();

  private static final Path SYS_THERMAL = Paths.get("/sys", "class", "thermal");
  private static final int ZONE_COUNT = getThermalZoneCount();
  private static final Map<Integer, String> ZONES = getZones();

  public static int getTemperature(int zone) {
    return readCounter(zone, "temp") / 1000;
  }

  public static int getZoneCount() {
    return ZONES.size();
  }

  public static String getZoneType(int zone) {
    return ZONES.get(zone);
  }

  public static ThermalZonesSample sample() {
    Instant timestamp = Instant.now();
    ArrayList<ThermalZoneTemperature> readings = new ArrayList<>();
    for (int zone = 0; zone < ZONE_COUNT; zone++) {
      readings.add(new ThermalZoneTemperature(zone, ZONES.get(zone), getTemperature(zone)));
    }
    return new ThermalZonesSample(timestamp, readings);
  }

  public static List<SignalData> between(
      List<ThermalZoneTemperature> first, List<ThermalZoneTemperature> second) {
    Map<Integer, ThermalZoneTemperature> secondMap =
        second.stream().collect(toMap(r -> r.zone, r -> r));
    ArrayList<SignalData> temperatures = new ArrayList<>();
    for (ThermalZoneTemperature reading : first) {
      if (secondMap.containsKey(reading.zone)) {
        ThermalZoneTemperature other = secondMap.get(reading.zone);
        temperatures.add(
            SignalData.newBuilder()
                .addMetadata(
                    SignalData.Metadata.newBuilder()
                        .setName("zone")
                        .setValue(Integer.toString(reading.zone)))
                .addMetadata(
                    SignalData.Metadata.newBuilder().setName("type").setValue(reading.type))
                .setValue(reading.temperature)
                .build());
      }
    }
    return temperatures;
  }

  public static SignalInterval difference(ThermalZonesSample first, ThermalZonesSample second) {
    return SignalInterval.newBuilder()
        .setStart(fromInstant(first.timestamp()))
        .setEnd(fromInstant(second.timestamp()))
        .addAllData(between(first.data(), second.data()))
        .build();
  }

  /**
   * Reads thermal zone information from /sys/class/thermal/ and returns the number of available
   * thermal zones.
   */
  private static int getThermalZoneCount() {
    if (!Files.exists(SYS_THERMAL)) {
      logger.warning("couldn't check the thermal zone count; thermal sysfs likely not available");
      return 0;
    }
    try {
      return (int)
          Files.list(SYS_THERMAL)
              .filter(p -> p.getFileName().toString().contains("thermal_zone"))
              .count();
    } catch (Exception e) {
      logger.warning("couldn't check the thermal zone count; thermal sysfs likely not available");
      return 0;
    }
  }

  /**
   * Reads thermal zone information from /sys/class/thermal/ and returns a map of each thermal zone
   * to its type.
   */
  private static Map<Integer, String> getZones() {
    if (!Files.exists(SYS_THERMAL)) {
      logger.warning("couldn't check the thermal zones; thermal sysfs likely not available");
      return Map.of();
    }
    try {
      return Files.list(SYS_THERMAL)
          .filter(p -> p.getFileName().toString().contains("thermal_zone"))
          .collect(
              Collectors.toMap(
                  p -> Integer.parseInt(p.toString().replaceAll("\\D+", "")),
                  p -> {
                    try {
                      return Files.readString(p.resolve("type")).toString().trim();
                    } catch (IOException e) {
                      logger.warning(
                          "couldn't read from /sys/class/thermal; thermal sysfs likely not"
                              + " available");
                      return "";
                    }
                  }));
    } catch (Exception e) {
      logger.warning("couldn't check the socket count; thermal sysfs likely not available");
      return Map.of();
    }
  }

  private static int readCounter(int cpu, String component) {
    String counter = readFromComponent(cpu, component);
    if (counter.isBlank()) {
      return 0;
    }
    return Integer.parseInt(counter);
  }

  private static synchronized String readFromComponent(int cpu, String component) {
    try {
      return Files.readString(getComponentPath(cpu, component)).trim();
    } catch (Exception e) {
      // e.printStackTrace();
      return "";
    }
  }

  private static Path getComponentPath(int zone, String component) {
    return Paths.get(SYS_THERMAL.toString(), String.format("thermal_zone%d", zone), component);
  }

  private SysThermal() {}
}
