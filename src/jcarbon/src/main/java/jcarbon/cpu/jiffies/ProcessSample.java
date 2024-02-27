package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.data.Sample;

/** A {@link Sample} of task jiffies for a process since task birth. */
public final class ProcessSample implements Sample<List<TaskJiffies>>, Comparable<ProcessSample> {
  private final Instant timestamp;
  private final long processId;
  private final ArrayList<TaskJiffies> jiffies = new ArrayList<>();

  ProcessSample(Instant timestamp, long processId, Iterable<TaskJiffies> jiffies) {
    this.timestamp = timestamp;
    this.processId = processId;
    jiffies.forEach(this.jiffies::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  public long processId() {
    return processId;
  }

  @Override
  public List<TaskJiffies> data() {
    return new ArrayList<>(jiffies);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"process_id\":%d,\"data\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        processId,
        jiffies.stream().map(TaskJiffies::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(ProcessSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
