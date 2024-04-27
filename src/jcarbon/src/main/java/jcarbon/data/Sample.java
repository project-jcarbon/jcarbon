package jcarbon.data;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.List;

/** An instantaneous piece of timestamped data. */
public interface Sample<T extends Data> {
  /** When the sample was taken. */
  Instant timestamp();

  /** The executing component. */
  String component();

  /** What the data is. */
  Unit unit();

  /** What is in the sample. */
  List<T> data();

  public default String toJson() {
    Instant timestamp = timestamp();
    return String.format(
        "{\"timestamp\":{\"secs\":%d,\"nanos\":%d},\"component\":\"%s\",\"unit\":\"%s\"\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        component(),
        unit(),
        data().stream().map(Data::toJson).collect(joining(",")));
  }
}
