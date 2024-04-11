package jcarbon.data;

import java.time.Instant;

/** An instantaneous piece of timestamped data. */
public interface Sample<T> {
  /** When the sample was taken. */
  Instant timestamp();

  /** The executing component. */
  Component component();

  /** What is in the sample. */
  T data();
}
