package jcarbon.linux;

import static jcarbon.util.LoggerUtil.getLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A class that exposes information from /proc/cpuinfo. */
public final class CpuInfo {
  private static final Logger logger = getLogger();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String CPU_INFO = "/proc/cpuinfo";
  private static final int[] CPU_TO_SOCKETS = createCpuSocketMapping();

  public static final int SOCKETS = Set.of(CPU_TO_SOCKETS).size();

  /** Returns the physical socket for each executable cpu. */
  public static int[] getCpuSocketMapping() {
    return Arrays.copyOf(CPU_TO_SOCKETS, CPU_COUNT);
  }

  private static int[] createCpuSocketMapping() {
    // Implicitly maps everything to a single socket.
    int[] mapping = new int[Runtime.getRuntime().availableProcessors()];
    // TODO: using the traditional java method to support android
    try {
      BufferedReader reader = new BufferedReader(new FileReader(CPU_INFO));
      int lastCpu = -1;
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        } else if (line.contains("processor")) {
          lastCpu = Integer.parseInt(line.split(":")[1].trim());
        } else if (line.contains("physical id")) {
          mapping[lastCpu] = Integer.parseInt(line.split(":")[1].trim());
        }
      }
      reader.close();
      logger.info("loaded mapping of cpus to sockets");
    } catch (Exception e) {
      logger.log(Level.INFO, "unable to read cpuinfo", e);
    }
    return mapping;
  }

  private CpuInfo() {}
}
