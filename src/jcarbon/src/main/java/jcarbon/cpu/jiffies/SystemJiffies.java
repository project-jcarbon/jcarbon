package jcarbon.cpu.jiffies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Interval;
import jcarbon.data.Unit;

/** An {@link Interval} of cpu jiffies over a time range. */
public final class SystemJiffies implements Interval<CpuJiffies>, Comparable<SystemJiffies> {
  public static SystemJiffies between(SystemSample first, SystemSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    return new SystemJiffies(
        first.timestamp(), second.timestamp(), difference(first.data(), second.data()));
  }

  private static List<CpuJiffies> difference(List<CpuJiffies> first, List<CpuJiffies> second) {
    if (first.size() != second.size()) {
      throw new IllegalArgumentException(
          String.format(
              "readings do not have the same number of cpus (%s != %s)",
              first.size(), second.size()));
    }
    ArrayList<CpuJiffies> jiffies = new ArrayList<>();
    for (CpuJiffies cpu : first) {
      jiffies.add(
          new CpuJiffies(
              cpu.cpu,
              second.get(cpu.cpu).user - cpu.user,
              second.get(cpu.cpu).nice - cpu.nice,
              second.get(cpu.cpu).system - cpu.system,
              second.get(cpu.cpu).idle - cpu.idle,
              second.get(cpu.cpu).iowait - cpu.iowait,
              second.get(cpu.cpu).irq - cpu.irq,
              second.get(cpu.cpu).softirq - cpu.softirq,
              second.get(cpu.cpu).steal - cpu.steal,
              second.get(cpu.cpu).guest - cpu.guest,
              second.get(cpu.cpu).guestNice - cpu.guestNice));
    }
    return jiffies;
  }

  private final Instant start;
  private final Instant end;
  private final ArrayList<CpuJiffies> jiffies = new ArrayList<>();

  SystemJiffies(Instant start, Instant end, Iterable<CpuJiffies> jiffies) {
    this.start = start;
    this.end = end;
    jiffies.forEach(this.jiffies::add);
  }

  @Override
  public Instant start() {
    return start;
  }

  @Override
  public Instant end() {
    return end;
  }

  @Override
  public String component() {
    return LinuxComponents.OS_COMPONENT;
  }

  @Override
  public Unit unit() {
    return Unit.JIFFIES;
  }

  @Override
  public List<CpuJiffies> data() {
    return new ArrayList<>(jiffies);
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public int compareTo(SystemJiffies other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
