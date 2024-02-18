package jrapl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Simple wrapper to read powercap's energy with pure Java. */
public final class Powercap {
  private static final String POWERCAP_PATH =
      String.join("/", "/sys", "devices", "virtual", "powercap", "intel-rapl");

  public static final int SOCKETS = getSocketCount();

  /** Returns an {@link EnergySample} populated by parsing the string returned by {@ readNative}. */
  public static EnergySample sample() {
    if (SOCKETS == 0) {
      LoggerUtil.LOGGER.warning("no sockets founds; power likely not available");
      return new EnergySample(Instant.now(), new EnergyReading[0]);
    }

    Instant timestamp = Instant.now();
    EnergyReading[] readings = new EnergyReading[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      readings[socket] = new EnergyReading(socket, readPackage(socket), readDram(socket), 0.0, 0.0);
    }

    return new EnergySample(timestamp, readings);
  }

  /** Computes the difference of two {@link EnergyReadings}. */
  public static EnergyReading difference(EnergyReading first, EnergyReading second) {
    if (first.socket != second.socket) {
      throw new IllegalArgumentException(
          String.format(
              "readings are not from the same domain (%d != %d)", first.socket, second.socket));
    }
    return new EnergyReading(first.socket, second.pkg - first.pkg, second.dram - first.dram, 0, 0);
  }

  /** Computes the difference of two {@link EnergyReadings}, applying the wraparound. */
  public static EnergyInterval difference(EnergySample first, EnergySample second) {
    if (first.timestamp.compareTo(second.timestamp) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp, second.timestamp));
    }
    return new EnergyInterval(
        first.timestamp,
        second.timestamp,
        IntStream.range(0, SOCKETS)
            .mapToObj(socket -> difference(first.getReading(socket), second.getReading(socket)))
            .toArray(EnergyReading[]::new));
  }

  private static int getSocketCount() {
    try {
      return (int)
          Stream.of(new File(POWERCAP_PATH).list()).filter(f -> f.contains("intel-rapl")).count();
    } catch (Exception e) {
      LoggerUtil.LOGGER.warning("couldn't check the socket count; powercap likely not available");
      return 0;
    }
  }

  /**
   * Parses the contents of /sys/devices/virtual/powercap/intel-rapl/intel-rapl:<socket>/energy_uj,
   * which contains the number of microjoules consumed by the package since boot as an integer.
   */
  private static double readPackage(int socket) {
    String energyFile =
        String.join("/", POWERCAP_PATH, String.format("intel-rapl:%d", socket), "energy_uj");
    try (BufferedReader reader = new BufferedReader(new FileReader(energyFile))) {
      return Double.parseDouble(reader.readLine()) / 1000000;
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Parses the contents of
   * /sys/devices/virtual/powercap/intel-rapl/intel-rapl:<socket>/intel-rapl:<socket>:0/energy_uj,
   * which contains the number of microjoules consumed by the dram since boot as an integer.
   */
  private static double readDram(int socket) {
    String socketPrefix = String.format("intel-rapl:%d", socket);
    String energyFile =
        String.join(
            "/", POWERCAP_PATH, socketPrefix, String.format("%s:0", socketPrefix), "energy_uj");
    try (BufferedReader reader = new BufferedReader(new FileReader(energyFile))) {
      return Double.parseDouble(reader.readLine()) / 1000000;
    } catch (Exception e) {
      return 0;
    }
  }
}
