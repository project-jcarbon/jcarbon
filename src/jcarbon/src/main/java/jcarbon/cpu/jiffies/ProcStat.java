package jcarbon.cpu.jiffies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;

/**
 * Helper for reading system jiffies from /proc system. Refer to
 * https://man7.org/linux/man-pages/man5/proc.5.html
 */
public final class ProcStat {
  // system information
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");

  // indicies for cpu stat because there are so many
  private enum CpuIndex {
    CPU(0),
    USER(1),
    NICE(2),
    SYSTEM(3),
    IDLE(4),
    IOWAIT(5),
    IRQ(6),
    SOFTIRQ(7),
    STEAL(8),
    GUEST(9),
    GUEST_NICE(10);

    private int index;

    private CpuIndex(int index) {
      this.index = index;
    }
  }

  public static SystemSample sampleCpus() {
    String[] stats = new String[0];
    try {
      BufferedReader reader = new BufferedReader(new FileReader(SYSTEM_STAT_FILE));
      stats = readCpus(reader);
    } catch (Exception e) {
      System.out.println("unable to read " + SYSTEM_STAT_FILE);
    }

    return new SystemSample(Instant.now(), parseCpus(stats));
  }

  /** Reads the system's stat file and returns individual cpus. */
  private static String[] readCpus(BufferedReader reader) throws Exception {
    String[] stats = new String[CPU_COUNT];
    reader.readLine(); // first line is total summary; we need by cpu
    for (int i = 0; i < CPU_COUNT; i++) {
      stats[i] = reader.readLine();
    }
    return stats;
  }

  /** Turns stat strings into a {@link CpuSample}. */
  private static CpuJiffies[] parseCpus(String[] stats) {
    CpuJiffies[] readings = new CpuJiffies[stats.length];
    for (int i = 0; i < stats.length; i++) {
      String[] stat = stats[i].split(" ");
      if (stat.length != 11) {
        continue;
      }
      readings[i] =
          new CpuJiffies(
              Integer.parseInt(stat[CpuIndex.CPU.index].substring(3)),
              Integer.parseInt(stat[CpuIndex.USER.index]),
              Integer.parseInt(stat[CpuIndex.NICE.index]),
              Integer.parseInt(stat[CpuIndex.SYSTEM.index]),
              Integer.parseInt(stat[CpuIndex.IDLE.index]),
              Integer.parseInt(stat[CpuIndex.IOWAIT.index]),
              Integer.parseInt(stat[CpuIndex.IRQ.index]),
              Integer.parseInt(stat[CpuIndex.SOFTIRQ.index]),
              Integer.parseInt(stat[CpuIndex.STEAL.index]),
              Integer.parseInt(stat[CpuIndex.GUEST.index]),
              Integer.parseInt(stat[CpuIndex.GUEST_NICE.index]));
    }
    return readings;
  }

  private ProcStat() {}
}
