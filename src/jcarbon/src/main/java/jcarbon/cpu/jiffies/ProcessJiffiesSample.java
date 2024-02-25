package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.data.Sample;

/** A sample of the jiffies of all tasks in a process. */
public final class ProcessJiffiesSample
    implements Sample<List<TaskJiffiesReading>>, Comparable<ProcessJiffiesSample> {
  private final Instant timestamp;
  private final long processId;
  private final ArrayList<TaskJiffiesReading> readings = new ArrayList<>();

  ProcessJiffiesSample(Instant timestamp, long processId, Iterable<TaskJiffiesReading> readings) {
    this.timestamp = timestamp;
    this.processId = processId;
    readings.forEach(this.readings::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
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
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"process_id\":%d,\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        processId,
        readings.stream().map(TaskJiffiesReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(ProcessJiffiesSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
