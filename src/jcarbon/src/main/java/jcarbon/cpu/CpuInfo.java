package jcarbon.cpu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Set;

public final class CpuInfo {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String CPU_INFO = "/proc/cpuinfo";
  private static final int[] CPU_TO_SOCKETS = createCpuSocketMapping();

  public static final int SOCKETS = Set.of(CPU_TO_SOCKETS).size();

  /** Returns the physical socket for each executable cpu. */
  public static int[] getCpuSocketMapping() {
    return Arrays.copyOf(CPU_TO_SOCKETS, CPU_COUNT);
  }

  private static int[] createCpuSocketMapping() {
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
    } catch (Exception e) {
      System.out.println("unable to read cpuinfo");
    }
    return mapping;
  }

  private CpuInfo() {}
}
