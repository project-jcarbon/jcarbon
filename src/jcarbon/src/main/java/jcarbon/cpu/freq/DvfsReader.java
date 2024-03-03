package jcarbon.cpu.freq;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * A simple (unsafe) wrapper for direct dvfs access. Consult
 * https://www.kernel.org/doc/html/v4.14/admin-guide/pm/cpufreq.html for more details.
 */
public final class DvfsReader {
  private static final Path CPUFREQ_ROOT = Paths.get("/sys", "devices", "system", "cpu");

  // ,"cpu%d","cpufreq";

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  /** Returns the expected frequency in KHz of a cpu. */
  public static int getFrequency(int cpu) {
    return readCounter(cpu, "cpuinfo_cur_freq");
  }

  /** Returns the observed frequency in KHz of a cpu. */
  public static int getObservedFrequency(int cpu) {
    return readCounter(cpu, "scaling_cur_freq");
  }

  /** Returns the current governor of a cpu. */
  public static String getGovernor(int cpu) {
    return readFromComponent(cpu, "scaling_governor");
  }

  public static CpuFrequencySample sample() {
    Instant timestamp = Instant.now();
    CpuFrequency[] readings = new CpuFrequency[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      readings[cpu] =
          new CpuFrequency(cpu, getGovernor(cpu), getObservedFrequency(cpu), getFrequency(cpu));
    }
    return new CpuFrequencySample(timestamp, readings);
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
      System.out.println(getComponentPath(cpu, component));
      return Files.readString(getComponentPath(cpu, component));
    } catch (Exception e) {
      // e.printStackTrace();
      return "";
    }
  }

  private static Path getComponentPath(int cpu, String component) {
    return Paths.get(CPUFREQ_ROOT.toString(), String.format("cpu%d", cpu), "cpufreq", component);
  }

  private DvfsReader() {}
}
