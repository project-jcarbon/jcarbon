package jcarbon.data;

import java.time.Instant;

/** A piece of data over a timestamped range. */
// TODO: might need to make this abstract to 
public interface Interval<T> {
  Instant start();

  Instant end();

  T data();
}
