package jcarbon.cpu.jiffies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Interval;
import jcarbon.data.Unit;

/** An {@link Interval} of fractional task activity for a process over a time range. */
public final class ProcessActivity implements Interval<TaskActivity>, Comparable<ProcessActivity> {
  public final long processId;

  private final Instant start;
  private final Instant end;
  public final String component;
  private final ArrayList<TaskActivity> tasks = new ArrayList<>();

  ProcessActivity(Instant start, Instant end, long processId, Iterable<TaskActivity> tasks) {
    this.processId = processId;
    this.start = start;
    this.end = end;
    this.component = LinuxComponents.processComponent(processId);
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
  public String component() {
    return component;
  }

  @Override
  public Unit unit() {
    return Unit.ACTIVITY;
  }

  @Override
  public List<TaskActivity> data() {
    return new ArrayList<>(tasks);
  }

  @Override
  public String toString() {
    return toJson();
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
