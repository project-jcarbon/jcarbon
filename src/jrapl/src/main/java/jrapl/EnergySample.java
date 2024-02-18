package jrapl;

import java.time.Instant;
import java.util.Arrays;

/** A timestamped sample from a rapl energy system that represents consumption since boot. */
public final class EnergySample {
  // TODO: immutable data structures are "safe" as public
  public final Instant timestamp;

  private final EnergyReading[] readings;

  EnergySample(Instant timestamp, EnergyReading[] readings) {
    this.timestamp = timestamp;
    this.readings = Arrays.copyOf(readings, readings.length);
  }

  public EnergyReading[] getReadings() {
    return Arrays.copyOf(readings, readings.length);
  }
}
