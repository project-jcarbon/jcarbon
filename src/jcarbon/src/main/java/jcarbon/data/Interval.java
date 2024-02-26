package jcarbon.data;

import java.time.Instant;

/** A piece of data over a timestamped range. */
public interface Interval<T> {
  Instant start();

  Instant end();

  T data();
}
