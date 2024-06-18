package jcarbon.util;

import java.time.Duration;
import java.time.Instant;
import jcarbon.signal.SignalInterval.Timestamp;

/** Utilities for algebra with {@link Instants} and {@link Durations}. */
public final class Timestamps {
  /** Returns the maximum (i.e. furthest in the future) {@link Instant}. */
  public static Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSecs(), timestamp.getNanos());
  }

  /** Returns the maximum (i.e. furthest in the future) {@link Instant}. */
  public static Timestamp fromInstant(Instant timestamp) {
    return Timestamp.newBuilder()
        .setSecs(timestamp.getEpochSecond())
        .setNanos(timestamp.getNano())
        .build();
  }

  /** Returns the maximum (i.e. furthest in the future) {@link Instant}. */
  public static boolean isBefore(Timestamp first, Timestamp second) {
    return toInstant(first).isBefore(toInstant(second));
  }

  /** Returns the maximum (i.e. furtherest in the future) {@link Instant}. */
  public static boolean isAfter(Timestamp first, Timestamp second) {
    return toInstant(first).isAfter(toInstant(second));
  }

  /** Returns the minimum (i.e. furthest in the past) {@link Instant}. */
  public static Timestamp min(Timestamp first, Timestamp second) {
    if (isBefore(first, second)) {
      return first;
    } else {
      return second;
    }
  }

  /** Returns the maximum (i.e. furthest in the future) {@link Instant}. */
  public static Timestamp max(Timestamp first, Timestamp second) {
    if (isAfter(first, second)) {
      return first;
    } else {
      return second;
    }
  }

  /** Returns the minimum (i.e. furthest in the past) {@link Instant}. */
  public static Timestamp min(Timestamp first, Timestamp... others) {
    Timestamp timestamp = first;
    for (Timestamp other : others) {
      timestamp = min(timestamp, other);
    }
    return timestamp;
  }

  /** Returns the maximum (i.e. furthest in the future) {@link Instant}. */
  public static Timestamp max(Timestamp first, Timestamp... others) {
    Timestamp timestamp = first;
    for (Timestamp other : others) {
      timestamp = max(timestamp, other);
    }
    return timestamp;
  }

  /**
   * Computes the ratio of elapsed time between two {@link Durations}. It is recommended that the
   * {@code dividend} is less than the {@code divisor} otherwise the value is somewhat non-sensical.
   */
  public static Duration between(Timestamp first, Timestamp second) {
    return Duration.between(toInstant(first), toInstant(second));
  }

  /**
   * Computes the ratio of elapsed time between two {@link Durations}. It is recommended that the
   * {@code dividend} is less than the {@code divisor} otherwise the value is somewhat non-sensical.
   */
  public static double divide(Duration dividend, Duration divisor) {
    return (double) dividend.toNanos() / divisor.toNanos();
  }

  // Native methods
  private static final boolean HAS_NATIVE;

  public static Timestamp now() {
    if (!HAS_NATIVE) {
      return fromInstant(Instant.now());
    }
    long timestamp = epochTimeNative();
    long secs = timestamp / 1000000;
    long nanos = timestamp - 1000000 * secs;
    return Timestamp.newBuilder().setSecs(secs).setNanos(nanos).build();
  }

  public static Instant nowAsInstant() {
    if (!HAS_NATIVE) {
      return Instant.now();
    }
    long timestamp = epochTimeNative();
    long secs = timestamp / 1000000;
    long nanos = timestamp - 1000000 * secs;
    return Instant.ofEpochSecond(secs, nanos);
  }

  public static Timestamp monotonicTime() {
    if (!HAS_NATIVE) {
      return fromInstant(Instant.now());
    }
    long monotonicTime = monotonicTimeNative();
    long secs = monotonicTime / 1000000000;
    long nanos = monotonicTime - 1000000000 * secs;
    return Timestamp.newBuilder().setSecs(secs).setNanos(nanos).build();
  }

  private static native long epochTimeNative();

  private static native long monotonicTimeNative();

  private static boolean loadLibrary() {
    try {
      // TODO: Remember to fix this when we migrate the files over to /src/jcarbon.
      NativeUtils.loadLibraryFromJar("/jcarbon-proto/src/main/c/jcarbon/util/libtime.so");
      return true;
    } catch (Exception e) {
      LoggerUtil.getLogger().info("couldn't load native timestamps library");
      e.printStackTrace();
      return false;
    }
  }

  static {
    HAS_NATIVE = loadLibrary();
  }

  private Timestamps() {}
}
