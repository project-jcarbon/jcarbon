package jrapl;

import java.time.Instant;
import java.util.Arrays;

public final class EnergyInterval {
  public final Instant start;
  public final Instant end;

  private final EnergyReading[] readings;

  public EnergyInterval(Instant start, Instant end, EnergyReading[] readings) {
    this.start = start;
    this.end = end;
    this.readings = Arrays.copyOf(readings, readings.length);
  }

  public EnergyReading getReading(int socket) {
    return readings[socket];
  }
}
