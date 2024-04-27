package jcarbon.cpu.rapl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Interval;
import jcarbon.data.Unit;

/** An {@link Interval} of rapl energy consumption over a time range. */
public final class RaplEnergy implements Interval<RaplReading>, Comparable<RaplEnergy> {
  private final Instant start;
  private final Instant end;
  private final ArrayList<RaplReading> readings = new ArrayList<>();

  RaplEnergy(Instant start, Instant end, Iterable<RaplReading> readings) {
    this.start = start;
    this.end = end;
    readings.forEach(this.readings::add);
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
  public String component() {
    return LinuxComponents.OS_COMPONENT;
  }

  @Override
  public Unit unit() {
    return Unit.JOULES;
  }

  @Override
  public List<RaplReading> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public String toString() {
    return toJson();
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
