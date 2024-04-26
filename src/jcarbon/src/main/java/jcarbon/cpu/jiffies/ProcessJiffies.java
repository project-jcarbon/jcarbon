package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Interval;

/** An {@link Interval} of task jiffies for a process over a time range. */
public final class ProcessJiffies implements Interval<TaskJiffies>, Comparable<ProcessJiffies> {
  public static ProcessJiffies between(ProcessSample first, ProcessSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    return new ProcessJiffies(
        first.timestamp(),
        second.timestamp(),
        first.processId,
        difference(first.data(), second.data()));
  }

  private static List<TaskJiffies> difference(List<TaskJiffies> first, List<TaskJiffies> second) {
    Map<Long, TaskJiffies> secondMap = second.stream().collect(toMap(r -> r.taskId, r -> r));
    ArrayList<TaskJiffies> jiffies = new ArrayList<>();
    for (TaskJiffies task : first) {
      if (secondMap.containsKey(task.taskId)) {
        TaskJiffies other = secondMap.get(task.taskId);
        if ((other.userJiffies - task.userJiffies) > 0
            || (other.systemJiffies - task.systemJiffies) > 0) {
          jiffies.add(
              new TaskJiffies(
                  task.processId,
                  task.taskId,
                  task.cpu,
                  Math.max(0, other.userJiffies - task.userJiffies),
                  Math.max(0, other.systemJiffies - task.systemJiffies)));
        }
      }
    }
    return jiffies;
  }

  public final long processId;

  private final Instant start;
  private final Instant end;
  private final String component;
  private final ArrayList<TaskJiffies> readings = new ArrayList<>();

  ProcessJiffies(Instant start, Instant end, long processId, Iterable<TaskJiffies> readings) {
    this.processId = processId;
    this.start = start;
    this.end = end;
    this.component = LinuxComponents.processComponent(processId);
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

  @Override
  public String component() {
    return component;
  }

  @Override
  public List<TaskJiffies> data() {
    return new ArrayList<>(readings);
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public int compareTo(ProcessJiffies other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
