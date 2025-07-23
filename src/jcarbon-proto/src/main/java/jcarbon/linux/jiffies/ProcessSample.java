package jcarbon.linux.jiffies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A {@link Sample} of task jiffies for a process since task birth. */
public final class ProcessSample implements Comparable<ProcessSample> {
  public final long processId;

  private final Instant timestamp;
  private final ArrayList<TaskJiffies> jiffies = new ArrayList<>();

  ProcessSample(Instant timestamp, long processId, Iterable<TaskJiffies> jiffies) {
    this.timestamp = timestamp;
    this.processId = processId;
    jiffies.forEach(this.jiffies::add);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public List<TaskJiffies> data() {
    return new ArrayList<>(jiffies);
  }

  @Override
  public int compareTo(ProcessSample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
