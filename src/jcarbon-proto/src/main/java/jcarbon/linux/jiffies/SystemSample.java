package jcarbon.linux.jiffies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A {@link Sample} of cpu jiffies since boot. */
public final class SystemSample implements Comparable<SystemSample> {
  private final Instant timestamp;
  private final ArrayList<CpuJiffies> jiffies = new ArrayList<>();

  SystemSample(Instant timestamp, List<CpuJiffies> jiffies) {
    this.timestamp = timestamp;
    jiffies.forEach(this.jiffies::add);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public List<CpuJiffies> data() {
    return new ArrayList<>(jiffies);
  }

  @Override
  public int compareTo(SystemSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
