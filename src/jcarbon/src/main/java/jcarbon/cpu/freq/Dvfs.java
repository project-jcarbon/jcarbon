package jcarbon.linux.freq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A simple (unsafe) wrapper for direct dvfs access. Consult
 * https://www.kernel.org/doc/html/v4.14/admin-guide/pm/cpufreq.html for more details.
 */
// TODO: this is kinda big, might want to break it into DvfsReader and DvfsWriter
public final class Dvfs {
  static final String FREQ_PATH = "/sys/devices/system/cpu/cpu%d/cpufreq";

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int[][] AVAILABLE_FREQUENCIES_KHZ = readAvailableFrequencies();
  private static final String[][] AVAILABLE_GOVERNORS = readAvailableGovernors();
  // TODO: the governors are not always the same so we may need to do some version control to
  // guarantee that the methods that change frequencies will not break
  private static final String DEFAULT_GOVERNOR = "ondemand";
  private static final String SCALABLE_GOVERNOR = "userspace";

  /** Returns the available frequencies in KHz for a cpu. */
  public static int[] getAvailableFrequencies(int cpu) {
    return Arrays.copyOf(AVAILABLE_FREQUENCIES_KHZ[cpu], AVAILABLE_FREQUENCIES_KHZ[cpu].length);
  }

  /** Returns the available governors for a cpu. */
  public static String[] getAvailableGovernors(int cpu) {
    return Arrays.copyOf(AVAILABLE_GOVERNORS[cpu], AVAILABLE_GOVERNORS[cpu].length);
  }

  /** Returns the available frequencies cpus can be scaled to in KHz. */
  public static int[][] getAvailableFrequencies() {
    int[][] frequencies = new int[CPU_COUNT][];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      frequencies[cpu] = getAvailableFrequencies(cpu);
    }
    return frequencies;
  }

  /** Returns the available governors. */
  public static String[][] getAvailableGovernors() {
    String[][] governors = new String[CPU_COUNT][];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      governors[cpu] = getAvailableGovernors(cpu);
    }
    return governors;
  }

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
      String governor = getGovernor(cpu);
      int observedFrequency = getObservedFrequency(cpu);
      readings[cpu] = new CpuFrequency(cpu, governor, observedFrequency, getFrequency(cpu));
    }
    return new CpuFrequencySample(timestamp, Arrays.asList(readings));
  }

  /** Sets the expected frequency of a cpu in KHz. */
  public static void setFrequency(int cpu, int frequency) {
    setGovernor(cpu, SCALABLE_GOVERNOR);
    writeToComponent(cpu, "scaling_setspeed", String.format("%d", frequency));
  }

  /** Sets the governor of a cpu. */
  public static void setGovernor(int cpu, String governor) {
    writeToComponent(cpu, "scaling_governor", governor);
  }

  /** Sets the governor of a cpu to the default governor. */
  public static void reset(int cpu) {
    setGovernor(cpu, DEFAULT_GOVERNOR);
  }

  /** Sets the governor of all cpus to the default governor. */
  public static void reset() {
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      reset(cpu);
    }
  }

  private static int[][] readAvailableFrequencies() {
    int[][] frequencies = new int[CPU_COUNT][];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      String freqs = readFromComponent(cpu, "scaling_available_frequencies");
      if (freqs.isBlank()) {
        frequencies[cpu] = new int[0];
      }
      frequencies[cpu] =
          Stream.of(freqs.split(" "))
              .mapToInt(
                  f -> {
                    try {
                      return Integer.parseInt(f);
                    } catch (NumberFormatException nfe) {
                      // nfe.printStackTrace();
                      return 0;
                    }
                  })
              .filter(f -> f > 0)
              .sorted()
              .toArray();
    }
    return frequencies;
  }

  private static String[][] readAvailableGovernors() {
    String[][] governors = new String[CPU_COUNT][];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      governors[cpu] = readFromComponent(cpu, "scaling_available_governors").split(" ");
    }
    return governors;
  }

  private static int readCounter(int cpu, String component) {
    String counter = readFromComponent(cpu, component);
    if (counter.isBlank()) {
      return 0;
    }
    return Integer.parseInt(counter);
  }

  private static synchronized String readFromComponent(int cpu, String component) {
    try (BufferedReader reader =
        new BufferedReader(new FileReader(getFrequencyComponent(cpu, component)))) {
      return reader.readLine();
    } catch (Exception e) {
      // e.printStackTrace();
      return "";
    }
  }

  private static synchronized boolean writeToComponent(int cpu, String component, String value) {
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(getFrequencyComponent(cpu, component)))) {
      writer.write(value);
      return true;
    } catch (Exception e) {
      // e.printStackTrace();
      return false;
    }
  }

  private static String getFrequencyComponent(int cpu, String component) {
    return String.join("/", String.format(FREQ_PATH, cpu), component);
  }

  /** A class wrapping the static methods for a specific cpu. */
  public static final class Cpu {
    public final int cpu;

    public Cpu(int cpu) {
      this.cpu = cpu;
    }

    /** Returns the available frequencies in KHz for the cpu. */
    public int[] getAvailableFrequencies() {
      return Dvfs.getAvailableFrequencies(this.cpu);
    }

    /** Returns the expected frequency of the cpu in KHz. */
    public int getFrequency() {
      return Dvfs.getFrequency(this.cpu);
    }

    /** Sets the expected frequency of the cpu in KHz. */
    public void setFrequency(int frequency) {
      Dvfs.setFrequency(this.cpu, frequency);
    }

    /** Returns the observed frequency of the cpu in KHz. */
    public int getObservedFrequency() {
      return Dvfs.getObservedFrequency(this.cpu);
    }

    /** Returns the current governor of the cpu. */
    public String getGovernor() {
      return readFromComponent(this.cpu, "scaling_governor");
    }

    /** Sets the governor of the cpus to the default governor. */
    public void reset() {
      Dvfs.reset(this.cpu);
    }
  }

  private Dvfs() {}
}
