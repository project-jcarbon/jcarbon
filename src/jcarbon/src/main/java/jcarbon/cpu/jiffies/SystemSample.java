package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.data.Sample;

/** A sample of the system's jiffies (i.e. cycles) since boot. */
public final class SystemSample implements Sample<CpuJiffies[]>, Comparable<SystemSample> {
  private final Instant timestamp;
  private final CpuJiffies[] jiffies;

  SystemSample(Instant timestamp, CpuJiffies[] jiffies) {
    this.timestamp = timestamp;
    this.jiffies = Arrays.copyOf(jiffies, jiffies.length);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public CpuJiffies[] data() {
    return Arrays.copyOf(jiffies, jiffies.length);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        Arrays.stream(jiffies).map(CpuJiffies::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(SystemSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
