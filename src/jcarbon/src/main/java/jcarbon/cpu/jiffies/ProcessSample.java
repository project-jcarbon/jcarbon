package jcarbon.cpu.jiffies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Sample;

/** A {@link Sample} of task jiffies for a process since task birth. */
public final class ProcessSample implements Sample<TaskJiffies>, Comparable<ProcessSample> {
  public final long processId;

  private final Instant timestamp;
  private final String component;
  private final ArrayList<TaskJiffies> jiffies = new ArrayList<>();

  ProcessSample(Instant timestamp, long processId, Iterable<TaskJiffies> jiffies) {
    this.timestamp = timestamp;
    this.processId = processId;
    this.component = LinuxComponents.processComponent(processId);
    jiffies.forEach(this.jiffies::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public String component() {
    return component;
  }

  @Override
  public List<TaskJiffies> data() {
    return new ArrayList<>(jiffies);
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public int compareTo(ProcessSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
