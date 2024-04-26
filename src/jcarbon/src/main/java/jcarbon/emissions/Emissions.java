package jcarbon.emissions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.data.Interval;

/** An {@link Interval} of emissions over a time range. */
public final class Emissions implements Interval<Emission> {
  private final Instant start;
  private final Instant end;
  private final String component;
  private final ArrayList<Emission> emissions = new ArrayList<>();

  public Emissions(Instant start, Instant end, String component, Iterable<Emission> emissions) {
    this.start = start;
    this.end = end;
    this.component = component;
    emissions.forEach(this.emissions::add);
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
    return component;
  }

  @Override
  public List<Emission> data() {
    return new ArrayList<>(emissions);
  }

  @Override
  public String toString() {
    return toJson();
  }
}
