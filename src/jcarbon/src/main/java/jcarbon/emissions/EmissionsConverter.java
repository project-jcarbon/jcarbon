package jcarbon.emissions;

import jcarbon.data.Data;
import jcarbon.data.Interval;

/** An interface that converts an interval of something to co2 emissions. */
public interface EmissionsConverter {
  /** Converts some interval to {@link Emissions} if possible. */
  <T extends Interval<? extends Iterable<? extends Data>>> Emissions convert(T interval);
}
