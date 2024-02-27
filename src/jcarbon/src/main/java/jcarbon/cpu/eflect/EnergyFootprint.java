package jcarbon.cpu.eflect;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.data.Interval;

/** An {@link Interval} of task energy for a process over a time range. */
public final class EnergyFootprint
    implements Interval<List<TaskEnergy>>, Comparable<EnergyFootprint> {
  private final Instant start;
  private final Instant end;
  private final ArrayList<TaskEnergy> tasks = new ArrayList<>();

  EnergyFootprint(Instant start, Instant end, Iterable<TaskEnergy> tasks) {
    this.start = start;
    this.end = end;
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
  public List<TaskEnergy> data() {
    return new ArrayList<>(tasks);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":"
            + "{\"seconds\":%d,\"nanos\":%d},\"data\":[%s]}",
        start.getEpochSecond(),
        start.getNano(),
        end.getEpochSecond(),
        end.getNano(),
        tasks.stream().map(TaskEnergy::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(EnergyFootprint other) {
    int start = start().compareTo(other.start());
    if (start < 0) {
      return start;
    } else {
      return end().compareTo(other.end());
    }
  }
}
