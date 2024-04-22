package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.ProcessComponent;
import jcarbon.data.Component;
import jcarbon.data.Sample;

/** A {@link Sample} of task jiffies for a process since task birth. */
public final class ProcessSample implements Sample<List<TaskJiffies>>, Comparable<ProcessSample> {
  final ProcessComponent component;

  private final Instant timestamp;
  private final ArrayList<TaskJiffies> jiffies = new ArrayList<>();

  ProcessSample(Instant timestamp, long processId, Iterable<TaskJiffies> jiffies) {
    this.timestamp = timestamp;
    this.component = new ProcessComponent(processId);
    jiffies.forEach(this.jiffies::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public Component component() {
    return component;
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
        component.processId,
        jiffies.stream().map(TaskJiffies::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(ProcessSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
