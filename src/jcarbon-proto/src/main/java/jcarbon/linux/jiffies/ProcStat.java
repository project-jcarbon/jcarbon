package jcarbon.linux.jiffies;

import static jcarbon.util.Timestamps.fromInstant;
import static jcarbon.util.Timestamps.nowAsInstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;

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
    // TODO: using the traditional java method to support android
    try {
      BufferedReader reader = new BufferedReader(new FileReader(SYSTEM_STAT_FILE));
      stats = readCpus(reader);
      reader.close();
    } catch (Exception e) {
      System.out.println("unable to read " + SYSTEM_STAT_FILE);
    }

    return new SystemSample(nowAsInstant(), parseCpus(stats));
  }

  public static SignalInterval between(SystemSample first, SystemSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    return SignalInterval.newBuilder()
        .setStart(fromInstant(first.timestamp()))
        .setEnd(fromInstant(second.timestamp()))
        .addAllData(difference(first.data(), second.data()))
        .build();
  }

  private static List<SignalData> difference(List<CpuJiffies> first, List<CpuJiffies> second) {
    if (first.size() != second.size()) {
      throw new IllegalArgumentException(
          String.format(
              "readings do not have the same number of cpus (%s != %s)",
              first.size(), second.size()));
    }
    ArrayList<SignalData> jiffies = new ArrayList<>();
    for (CpuJiffies cpu : first) {
      jiffies.add(
          SignalData.newBuilder()
              .addMetadata(
                  SignalData.Metadata.newBuilder()
                      .setName("cpu")
                      .setValue(Integer.toString(cpu.cpu)))
              .setValue(
                  second.get(cpu.cpu).user
                      - cpu.user
                      + second.get(cpu.cpu).nice
                      - cpu.nice
                      + second.get(cpu.cpu).system
                      - cpu.system
                      + second.get(cpu.cpu).idle
                      - cpu.idle
                      + second.get(cpu.cpu).iowait
                      - cpu.iowait
                      + second.get(cpu.cpu).irq
                      - cpu.irq
                      + second.get(cpu.cpu).softirq
                      - cpu.softirq
                      + second.get(cpu.cpu).steal
                      - cpu.steal
                      + second.get(cpu.cpu).guest
                      - cpu.guest
                      + second.get(cpu.cpu).guestNice
                      - cpu.guestNice)
              .build());
    }
    return jiffies;
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
  private static ArrayList<CpuJiffies> parseCpus(String[] stats) {
    ArrayList<CpuJiffies> readings = new ArrayList<>();
    for (int i = 0; i < stats.length; i++) {
      String[] stat = stats[i].split(" ");
      if (stat.length != 11) {
        continue;
      }
      readings.add(
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
              Integer.parseInt(stat[CpuIndex.GUEST_NICE.index])));
    }
    return readings;
  }

  private ProcStat() {}
}
