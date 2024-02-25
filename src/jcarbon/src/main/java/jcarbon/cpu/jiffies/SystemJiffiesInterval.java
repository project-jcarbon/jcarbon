package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.data.Interval;

/** A sample of rapl energy consumption since boot. */
public final class SystemJiffiesInterval
    implements Interval<CpuJiffiesReading[]>, Comparable<SystemJiffiesInterval> {
  private final Instant start;
  private final Instant end;
  private final CpuJiffiesReading[] readings;

  SystemJiffiesInterval(Instant start, Instant end, CpuJiffiesReading[] readings) {
    this.start = start;
    this.end = end;
    this.readings = Arrays.copyOf(readings, readings.length);
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
  public CpuJiffiesReading[] data() {
    return Arrays.copyOf(readings, readings.length);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":{\"seconds\":%d,\"nanos\":%d},\"data\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        Arrays.stream(readings).map(CpuJiffiesReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(SystemJiffiesInterval other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
