package jcarbon.gpu;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.data.Sample;

public final class GpuSample implements Sample<GpuReading[]>, Comparable<GpuSample> {
  private final Instant timestamp;
  private final GpuReading[] readings;

  GpuSample(Instant timestamp, GpuReading[] readings) {
    this.timestamp = timestamp;
    this.readings = Arrays.copyOf(readings, readings.length);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public GpuReading[] data() {
    return Arrays.copyOf(readings, readings.length);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        Arrays.stream(readings).map(GpuReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(GpuSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
