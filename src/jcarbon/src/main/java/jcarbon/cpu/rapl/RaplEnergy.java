package jcarbon.cpu.rapl;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.cpu.CpuComponent;
import jcarbon.data.Component;
import jcarbon.data.Interval;

/** An {@link Interval} of rapl energy consumption over a time range. */
public final class RaplEnergy implements Interval<RaplReading[]>, Comparable<RaplEnergy> {
  private final Instant start;
  private final Instant end;
  private final RaplReading[] readings;

  RaplEnergy(Instant start, Instant end, RaplReading[] readings) {
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
  public Component component() {
    return CpuComponent.INSTANCE;
  }

  @Override
  public RaplReading[] data() {
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
        Arrays.stream(readings).map(RaplReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(RaplEnergy other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
