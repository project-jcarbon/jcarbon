package jcarbon.data;

import java.time.Instant;

/** An instantaneous piece of timestamped data. */
public interface Sample<T> {
  Instant timestamp();

  T data();
}
