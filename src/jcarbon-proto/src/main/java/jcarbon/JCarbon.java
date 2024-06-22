package jcarbon;

import java.util.Optional;
import jcarbon.signal.Report;

/** A class to collect and provide jcarbon signals. */
public interface JCarbon {
  /** Starts monitoring. */
  void start();

  /** Stops monitoring and maybe return a report if there was any data. */
  Optional<Report> stop();
}
