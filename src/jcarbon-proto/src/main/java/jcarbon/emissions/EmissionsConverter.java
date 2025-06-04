package jcarbon.emissions;

import jcarbon.signal.Signal;

/** An interface that converts an interval of something to co2 emissions. */
public interface EmissionsConverter {
  /**
   * Converts some {@link Signal} to {@link SignalIntervals} of {@link Unit.GRAMS_OF_CO2}. If it
   * cannot be converted to co2, an empty list is returned.
   */
  Signal convert(Signal signal);
}
