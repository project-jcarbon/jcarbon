package jrapl;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;

/** A timestamp interval from a rapl energy system that represents consumption over a time range. */
public final class EnergyInterval {
  // TODO: immutable data structures are "safe" as public
  public final Instant start;
  public final Instant end;

  private final EnergyReading[] readings;

  public EnergyInterval(Instant start, Instant end, EnergyReading[] readings) {
    this.start = start;
    this.end = end;
    this.readings = Arrays.copyOf(readings, readings.length);
  }

  public EnergyReading[] getReadings() {
    return Arrays.copyOf(readings, readings.length);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":"
            + "{\"seconds\":%d,\"nanos\":%d},\"readings\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        Arrays.stream(readings).map(EnergyReading::toString).collect(joining(",")));
  }
}
