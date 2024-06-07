package jcarbon.cpu.rapl;

import static java.util.stream.Collectors.toList;
import static jcarbon.util.LoggerUtil.getLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/** Simple wrapper to read powercap's energy with pure Java. */
// TODO: this doesn't appear to work on more modern implementations that are hierarchical
public final class Powercap {
  private static final Logger logger = getLogger();

  private static final Path POWERCAP_ROOT =
      Paths.get("/sys", "devices", "virtual", "powercap", "intel-rapl");

  public static final int SOCKETS = getSocketCount();
  public static final double[][] MAX_ENERGY_JOULES = getMaximumEnergy();

  /** Returns whether we can read values. */
  public static boolean isAvailable() {
    return SOCKETS > 0;
  }

  /** Returns an {@link RaplSample} populated by parsing the string returned by {@ readNative}. */
  public static Optional<RaplSample> sample() {
    if (!isAvailable()) {
      return Optional.empty();
    }

    Instant timestamp = Instant.now();
    ArrayList<RaplReading> readings = new ArrayList<>();
    for (int socket = 0; socket < SOCKETS; socket++) {
      readings.add(new RaplReading(socket, readPackage(socket), readDram(socket), 0.0, 0.0));
    }

    return Optional.of(new RaplSample(timestamp, readings));
  }

  /** Computes the difference of two {@link RaplReadings}. */
  public static RaplReading difference(RaplReading first, RaplReading second, int socket) {
    if (first.socket != second.socket) {
      throw new IllegalArgumentException(
          String.format(
              "readings are not from the same domain (%d != %d)", first.socket, second.socket));
    }
    return new RaplReading(
        first.socket,
        diffWithWraparound(first.pkg, second.pkg, socket, 0),
        diffWithWraparound(first.dram, second.dram, socket, 1),
        0,
        0);
  }

  /** Computes the difference of two {@link RaplReadings}, applying the wraparound. */
  public static RaplEnergy difference(RaplSample first, RaplSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    List<RaplReading> firstData = first.data();
    List<RaplReading> secondData = second.data();
    return new RaplEnergy(
        first.timestamp(),
        second.timestamp(),
        IntStream.range(0, SOCKETS)
            .mapToObj(socket -> difference(firstData.get(socket), secondData.get(socket), socket))
            .collect(toList()));
  }

  private static double diffWithWraparound(double first, double second, int socket, int component) {
    double energy = second - first;
    if (energy < 0) {
      logger.info(String.format("powercap overflow on %d:%d", socket, component));
      energy += MAX_ENERGY_JOULES[socket][component];
    }
    return energy;
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

  private static double[][] getMaximumEnergy() {
    if (!Files.exists(POWERCAP_ROOT)) {
      logger.warning("couldn't check the maximum energy; powercap likely not available");
      return new double[0][0];
    }
    try {
      return Files.list(POWERCAP_ROOT)
          .filter(p -> p.getFileName().toString().contains("intel-rapl"))
          .map(
              socket -> {
                try {
                  return Files.list(socket)
                      .filter(p -> p.getFileName().toString().contains("max_energy_range_uj"))
                      .mapToDouble(
                          component -> {
                            try {
                              return Double.parseDouble(Files.readString(component)) / 1000000;
                            } catch (Exception e) {
                              return 0;
                            }
                          })
                      .toArray();
                } catch (Exception e) {
                  logger.warning(
                      String.format("couldn't check the maximum energy for socket %s", socket));
                  return 0;
                }
              })
          .toArray(double[][]::new);
    } catch (Exception e) {
      logger.warning("couldn't check the maximum energy; powercap likely not available");
      return new double[0][0];
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
