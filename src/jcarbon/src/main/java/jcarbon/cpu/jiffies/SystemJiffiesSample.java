package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.data.Sample;

/** A sample of the system's jiffies (i.e. cycles) since boot. */
public final class SystemJiffiesSample
    implements Sample<CpuJiffiesReading[]>, Comparable<SystemJiffiesSample> {
  private final Instant timestamp;
  private final CpuJiffiesReading[] readings;

  SystemJiffiesSample(Instant timestamp, CpuJiffiesReading[] readings) {
    this.timestamp = timestamp;
    this.readings = Arrays.copyOf(readings, readings.length);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public CpuJiffiesReading[] data() {
    return Arrays.copyOf(readings, readings.length);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        Arrays.stream(readings).map(CpuJiffiesReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(SystemJiffiesSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
