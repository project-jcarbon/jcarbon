package jcarbon.cpu.eflect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Interval;
import jcarbon.data.Unit;

/** An {@link Interval} of task energy for a process over a time range. */
public final class ProcessEnergy implements Interval<TaskEnergy>, Comparable<ProcessEnergy> {
  public final long processId;

  private final Instant start;
  private final Instant end;
  private final String component;
  private final ArrayList<TaskEnergy> tasks = new ArrayList<>();

  ProcessEnergy(Instant start, Instant end, long processId, Iterable<TaskEnergy> tasks) {
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
    return Unit.JOULES;
  }

  @Override
  public List<TaskEnergy> data() {
    return new ArrayList<>(tasks);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return toJson();
  }

  @Override
  public int compareTo(ProcessEnergy other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
