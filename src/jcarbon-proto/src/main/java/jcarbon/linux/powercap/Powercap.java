package jcarbon.linux.powercap;

import static java.util.stream.Collectors.toList;
import static jcarbon.util.LoggerUtil.getLogger;
import static jcarbon.util.Timestamps.fromInstant;
import static jcarbon.util.Timestamps.nowAsInstant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;

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

  /**
   * Returns an {@link PowercapSample} populated by parsing the string returned by {@ readNative}.
   */
  public static Optional<PowercapSample> sample() {
    if (!isAvailable()) {
      return Optional.empty();
    }

    Instant timestamp = nowAsInstant();
    ArrayList<PowercapReading> readings = new ArrayList<>();
    for (int socket = 0; socket < SOCKETS; socket++) {
      readings.add(new PowercapReading(socket, readPackage(socket), readDram(socket), 0.0, 0.0));
    }

    return Optional.of(new PowercapSample(timestamp, readings));
  }

  /** Computes the difference of two {@link PowercapReadings}. */
  public static SignalData difference(PowercapReading first, PowercapReading second) {
    if (first.socket != second.socket) {
      throw new IllegalArgumentException(
          String.format(
              "readings are not from the same domain (%d != %d)", first.socket, second.socket));
    }
    return SignalData.newBuilder()
        .addMetadata(
            SignalData.Metadata.newBuilder()
                .setName("socket")
                .setValue(Integer.toString(first.socket)))
        .setValue(second.pkg - first.pkg + second.dram - first.dram)
        .build();
  }

  /** Computes the difference of two {@link PowercapReadings}, applying the wraparound. */
  public static SignalInterval difference(PowercapSample first, PowercapSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    List<PowercapReading> firstData = first.data();
    List<PowercapReading> secondData = second.data();
    return SignalInterval.newBuilder()
        .setStart(fromInstant(first.timestamp()))
        .setEnd(fromInstant(second.timestamp()))
        .addAllData(
            IntStream.range(0, SOCKETS)
                .mapToObj(socket -> difference(firstData.get(socket), secondData.get(socket)))
                .collect(toList()))
        .build();
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
