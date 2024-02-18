package jrapl;

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
}
