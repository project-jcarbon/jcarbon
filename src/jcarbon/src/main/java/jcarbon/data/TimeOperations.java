package jcarbon.data;

import java.time.Duration;
import java.time.Instant;

/** Utilities for algebra with {@link Instants} and {@link Durations}. */
public final class TimeOperations {
  /** Returns the maximum (i.e. furtherest in the future) {@link Instant}. */
  public static Instant max(Instant first, Instant second) {
    if (first.isAfter(second)) {
      return first;
    } else {
      return second;
    }
  }

  /** Returns the maximum (i.e. furtherest in the future) {@link Instant}. */
  public static Instant max(Instant first, Instant... others) {
    Instant timestamp = first;
    for (Instant other : others) {
      timestamp = max(timestamp, other);
    }
    return timestamp;
  }

  /** Returns the minimum (i.e. furtherest in the past) {@link Instant}. */
  public static Instant min(Instant first, Instant second) {
    if (first.isBefore(second)) {
      return first;
    } else {
      return second;
    }
  }

  /** Returns the minimum (i.e. furtherest in the past) {@link Instant}. */
  public static Instant min(Instant first, Instant... others) {
    Instant timestamp = first;
    for (Instant other : others) {
      timestamp = min(timestamp, other);
    }
    return timestamp;
  }

  /**
   * Computes the ratio of elapsed time between two {@link Durations}. It is recommended that the
   * {@code dividend} is less than the {@code divisor} otherwise the value is somewhat non-sensical.
   */
  public static double divide(Duration dividend, Duration divisor) {
    return (double) dividend.toNanos() / divisor.toNanos();
  }

  private TimeOperations() {}
}
