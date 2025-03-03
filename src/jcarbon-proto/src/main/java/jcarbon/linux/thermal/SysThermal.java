package jcarbon.linux.thermal;

import static jcarbon.util.LoggerUtil.getLogger;
import static java.util.stream.Collectors.toMap;
import static jcarbon.util.Timestamps.fromInstant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.logging.Logger;

import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.signal.SignalInterval.Timestamp;

/**
 * A simple (unsafe) wrapper for reading the thermal sysfs system. Consult
 * https://www.kernel.org/doc/Documentation/thermal/sysfs-api.txt for more details.
 */
public final class SysThermal {
    private static final Logger logger = getLogger();

    private static final Path SYS_THERMAL = 
      Paths.get("/sys", "class", "thermal");
    private static final int ZONE_COUNT = getThermalZoneCount();
    public static final Map<Integer, String> ZONES = getZones();

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

    private static Map<Integer, String> getZones(){
      if (!Files.exists(SYS_THERMAL)) {
        logger.warning("couldn't check the thermal zones; thermal sysfs likely not available");
        return Map.of();
      }
      try{
          return
          Files.list(SYS_THERMAL)
            .filter(p -> p.getFileName().toString().contains("thermal_zone"))
            .collect(Collectors.toMap(
              p -> Integer.parseInt(p.toString().replaceAll("\\D+", "")),
              p -> {
                  try{
                    return Files.readString(p.resolve("type")).toString().trim();
                  } catch(IOException e){
                    logger.warning("couldn't read from /sys/class/thermal; thermal sysfs likely not available");
                    return "";
                  }
              } 
            ));
      } catch (Exception e) {
        logger.warning("couldn't check the socket count; thermal sysfs likely not available");
        return Map.of();
      }
    }

    public static int getTemperature(int zone){
      return readCounter(zone, "temp");
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

    public static Optional<ThermalZonesSample> sample() {
      Instant timestamp = Instant.now();
      ArrayList<ThermalZone> readings = new ArrayList<>();
      for (int zone = 0; zone < ZONE_COUNT; zone++) {
        readings.add(
            new ThermalZone(zone, ZONES.get(zone), getTemperature(zone)));
      }
      return Optional.of(new ThermalZonesSample(timestamp, readings));
    }

    public static ThermalZonesSample sampleTemps() {
      Instant timestamp = Instant.now();
      ArrayList<ThermalZone> readings = new ArrayList<>();
      for (int zone = 0; zone < ZONE_COUNT; zone++) {
        readings.add(
            new ThermalZone(zone, ZONES.get(zone), getTemperature(zone)));
      }
      return new ThermalZonesSample(timestamp, readings);
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

    private SysThermal(){}
}