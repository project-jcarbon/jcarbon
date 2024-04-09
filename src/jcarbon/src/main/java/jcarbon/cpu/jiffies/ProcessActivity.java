package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.ProcessComponent;
import jcarbon.data.Component;
import jcarbon.data.Interval;

/** An {@link Interval} of fractional task activity for a process over a time range. */
public final class ProcessActivity
    implements Interval<List<TaskActivity>>, Comparable<ProcessActivity> {
  private final Instant start;
  private final Instant end;
  private final ProcessComponent component;
  private final ArrayList<TaskActivity> tasks = new ArrayList<>();

  ProcessActivity(Instant start, Instant end, long processId, Iterable<TaskActivity> tasks) {
    this.start = start;
    this.end = end;
    this.component = new ProcessComponent(processId);
    tasks.forEach(this.tasks::add);
  }

  @Override
  public Instant start() {
    return start;
  }

  @Override
  public Instant end() {
    return end;
  }

  @Override
  public Component component() {
    return component;
  }

  public long processId() {
    return component.processId;
  }

  @Override
  public List<TaskActivity> data() {
    return new ArrayList<>(tasks);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":"
            + "{\"seconds\":%d,\"nanos\":%d},\"process_id\":%d,\"data\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        component.processId,
        tasks.stream().map(TaskActivity::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(ProcessActivity other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
