package jrapl;

import java.time.Instant;
import java.util.Arrays;

public final class EnergySample {
  public final Instant timestamp;

  private final EnergyReading[] readings;

  public EnergySample(Instant timestamp, EnergyReading[] readings) {
    this.timestamp = timestamp;
    this.readings = Arrays.copyOf(readings, readings.length);
  }

  public EnergyReading getReading(int socket) {
    return readings[socket];
  }
}
