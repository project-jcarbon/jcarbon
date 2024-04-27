package jcarbon.data;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.List;

/** A piece of data over a timestamped range. */
public interface Interval<T extends Data> {
  /** Start of the interval. */
  Instant start();

  /** End of the interval. */
  Instant end();

  /** The executing component. */
  String component();

  /** What the data is. */
  Unit unit();

  /** What is in the interval. */
  List<T> data();

  public default String toJson() {
    Instant start = start();
    Instant end = end();
    return String.format(
      "{\"start\":{\"secs\":%d,\"nanos\":%d},\"end\":{\"secs\":%d,\"nanos\":%d},\"component\":\"%s\",\"unit\":\"%s\",\"data\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        component(),
        unit(),
        data().stream().map(Data::toJson).collect(joining(",")));
  }
}
