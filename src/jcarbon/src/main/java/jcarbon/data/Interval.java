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

  /** What is in the interval. */
  List<T> data();

  public default String toJson() {
    Instant start = start();
    Instant end = end();
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":{\"seconds\":%d,\"nanos\":%d},\"component\":\"%s\",\"data\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        component(),
        data().stream().map(Data::toJson).collect(joining(",")));
  }
}
