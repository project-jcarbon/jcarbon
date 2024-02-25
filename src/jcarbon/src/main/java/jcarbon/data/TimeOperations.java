package jcarbon.data;

import java.time.Instant;

/** Utilities for algebra with {@link Instant}s. */
public final class TimeOperations {
  // boolean comparisons
  public static boolean atMost(Instant first, Instant second) {
    return first.compareTo(second) <= 0;
  }

  public static boolean atLeast(Instant first, Instant second) {
    return first.compareTo(second) >= 0;
  }

  public static boolean equal(Instant first, Instant second) {
    return first.compareTo(second) == 0;
  }

  public static boolean greaterThan(Instant first, Instant second) {
    return first.compareTo(second) > 0;
  }

  public static boolean lessThan(Instant first, Instant second) {
    return first.compareTo(second) < 0;
  }

  // algebraic comparisons
  public static Instant max(Instant first, Instant second) {
    if (greaterThan(first, second)) {
      return first;
    } else {
      return second;
    }
  }

  public static Instant max(Instant first, Instant... others) {
    Instant timestamp = first;
    for (Instant other : others) {
      timestamp = max(timestamp, other);
    }
    return timestamp;
  }

  public static Instant min(Instant first, Instant second) {
    if (lessThan(first, second)) {
      return first;
    } else {
      return second;
    }
  }

  public static Instant min(Instant first, Instant... others) {
    Instant timestamp = first;
    for (Instant other : others) {
      timestamp = min(timestamp, other);
    }
    return timestamp;
  }

  public enum Region {
    BEFORE,
    IN_RANGE,
    AFTER;
  }

  public static Region between(Instant timestamp, Instant start, Instant end) {
    if (!atLeast(start, timestamp)) {
      return Region.BEFORE;
    } else if (!atMost(timestamp, end)) {
      return Region.AFTER;
    }
    return Region.IN_RANGE;
  }

  private TimeOperations() {}
}
