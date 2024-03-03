package jcarbon.cpu.rapl;

import static jcarbon.util.LoggerUtil.getLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/** Simple wrapper to read powercap's energy with pure Java. */
// TODO: this doesn't appear to work on more modern implementations that are hierarchical
public final class Powercap {
  private static final Logger logger = getLogger();

  private static final Path POWERCAP_ROOT =
      Paths.get("/sys", "devices", "virtual", "powercap", "intel-rapl");

  public static final int SOCKETS = getSocketCount();

  /** Returns whether we can read values. */
  public static boolean isAvailable() {
    return SOCKETS > 0;
  }

  /** Returns an {@link RaplSample} populated by parsing the string returned by {@ readNative}. */
  public static RaplSample sample() {
    if (!isAvailable()) {
      return new RaplSample(Instant.now(), new RaplReading[0]);
    }

    Instant timestamp = Instant.now();
    RaplReading[] readings = new RaplReading[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      readings[socket] = new RaplReading(socket, readPackage(socket), readDram(socket), 0.0, 0.0);
    }

    return new RaplSample(timestamp, readings);
  }

  /** Computes the difference of two {@link RaplReadings}. */
  public static RaplReading difference(RaplReading first, RaplReading second) {
    if (first.socket != second.socket) {
      throw new IllegalArgumentException(
          String.format(
              "readings are not from the same domain (%d != %d)", first.socket, second.socket));
    }
    return new RaplReading(first.socket, second.pkg - first.pkg, second.dram - first.dram, 0, 0);
  }

  /** Computes the difference of two {@link RaplReadings}, applying the wraparound. */
  public static RaplInterval difference(RaplSample first, RaplSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    return new RaplInterval(
        first.timestamp(),
        second.timestamp(),
        IntStream.range(0, SOCKETS)
            .mapToObj(socket -> difference(first.data()[socket], second.data()[socket]))
            .toArray(RaplReading[]::new));
  }

  private static int getSocketCount() {
    if (!Files.exists(POWERCAP_ROOT)) {
      logger.warning("couldn't check the socket count; powercap likely not available");
      return 0;
    }
    try {
      return (int)
          Files.list(POWERCAP_ROOT)
              .filter(p -> p.getFileName().toString().contains("intel-rapl"))
              .count();
    } catch (Exception e) {
      logger.warning("couldn't check the socket count; powercap likely not available");
      return 0;
    }
  }

  /**
   * Parses the contents of /sys/devices/virtual/powercap/intel-rapl/intel-rapl:<socket>/energy_uj,
   * which contains the number of microjoules consumed by the package since boot as an integer.
   */
  private static double readPackage(int socket) {
    String socketPrefix = String.format("intel-rapl:%d", socket);
    Path energyFile = Paths.get(POWERCAP_ROOT.toString(), socketPrefix, "energy_uj");
    try {
      return Double.parseDouble(Files.readString(energyFile)) / 1000000;
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
    Path energyFile =
        Paths.get(
            POWERCAP_ROOT.toString(),
            socketPrefix,
            String.format("%s:0", socketPrefix),
            "energy_uj");
    try {
      return Double.parseDouble(Files.readString(energyFile)) / 1000000;
    } catch (Exception e) {
      return 0;
    }
  }
}
