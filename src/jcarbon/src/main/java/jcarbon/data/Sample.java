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

  /** What is in the sample. */
  List<T> data();

  public default String toJson() {
    Instant timestamp = timestamp();
    return String.format(
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"component\":\"%s\",\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        component(),
        data().stream().map(Data::toJson).collect(joining(",")));
  }
}
