package jcarbon.cpu.jiffies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Sample;

/** A {@link Sample} of cpu jiffies since boot. */
public final class SystemSample implements Sample<CpuJiffies>, Comparable<SystemSample> {
  private final Instant timestamp;
  private final ArrayList<CpuJiffies> jiffies = new ArrayList<>();

  SystemSample(Instant timestamp, List<CpuJiffies> jiffies) {
    this.timestamp = timestamp;
    jiffies.forEach(this.jiffies::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public String component() {
    return LinuxComponents.OS_COMPONENT;
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
  public int compareTo(SystemSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
