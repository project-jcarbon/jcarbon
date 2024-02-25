package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.data.Interval;

/** A sample of the jiffies of all tasks in a process. */
public final class ProcessJiffiesInterval
    implements Interval<List<TaskJiffiesReading>>, Comparable<ProcessJiffiesInterval> {
  private final Instant start;
  private final Instant end;
  private final long processId;
  private final ArrayList<TaskJiffiesReading> readings = new ArrayList<>();

  ProcessJiffiesInterval(
      Instant start, Instant end, long processId, Iterable<TaskJiffiesReading> readings) {
    this.start = start;
    this.end = end;
    this.processId = processId;
    readings.forEach(this.readings::add);
  }

  @Override
  public Instant start() {
    return start;
  }

  @Override
  public Instant end() {
    return end;
  }

  public long processId() {
    return processId;
  }

  @Override
  public List<TaskJiffiesReading> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":{\"seconds\":%d,\"nanos\":%d},\"process_id\":%d,\"data\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        processId,
        readings.stream().map(TaskJiffiesReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(ProcessJiffiesInterval other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
