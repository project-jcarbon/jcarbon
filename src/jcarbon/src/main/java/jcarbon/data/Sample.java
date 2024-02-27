package jcarbon.data;

import java.time.Instant;

/** An instantaneous piece of timestamped data. */
public interface Sample<T> {
  /** When the sample was taken. */
  Instant timestamp();

  /** What is in the sample. */
  T data();
}
