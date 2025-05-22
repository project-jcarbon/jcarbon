package jcarbon.linux.jiffies;

import static jcarbon.util.Timestamps.isAfter;

import java.util.ArrayList;
import java.util.Optional;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.util.Timestamps;

/** Class to compute the activity of tasks in a process using jiffies. */
public final class JiffiesAccounting {
  /**
   * Computes the activity of all tasks in the overlapping region of two intervals by using the
   * ratio between a task's jiffies and cpu jiffies of the task's executing cpu. This also safely
   * bounds the value from 0 to 1, in the cases that the jiffies are misaligned due to the kernel
   * update timing.
   */
  // TODO: Need to find (or write) something that strictly mentions the timing issue
  public static Optional<SignalInterval> computeTaskActivity(
      SignalInterval proc, SignalInterval sys) {
    if (isAfter(proc.getStart(), sys.getEnd()) || isAfter(sys.getStart(), proc.getEnd())) {
      return Optional.empty();
    }
    ArrayList<SignalData> tasks = new ArrayList<>();
    // Set this up to correct for kernel update.
    int[] totalJiffies = new int[sys.getDataCount()];
    for (SignalData task : proc.getDataList()) {
      int cpu = Integer.parseInt(task.getMetadata(1).getValue());
      totalJiffies[cpu] += task.getValue();
    }
    for (SignalData task : proc.getDataList()) {
      // Don't bother if there are no jiffies.
      if (task.getValue() == 0) {
        continue;
      }
      // Correct for the kernel update by using total jiffies reported by tasks if the cpu
      // reported one is too small (this also catches zero jiffies reported by the cpu).
      int cpu = Integer.parseInt(task.getMetadata(1).getValue());
      double cpuJiffies = Math.max(sys.getData(cpu).getValue(), totalJiffies[cpu]);
      double taskActivity = Math.min(1.0, task.getValue() / cpuJiffies);
      tasks.add(task.toBuilder().setValue(taskActivity).build());
    }
    // Don't bother if there is no activity.
    if (!tasks.isEmpty()) {
      return Optional.of(
          SignalInterval.newBuilder()
              .setStart(Timestamps.max(proc.getStart(), sys.getStart()))
              .setEnd(Timestamps.min(proc.getEnd(), sys.getEnd()))
              .addAllData(tasks)
              .build());
    } else {
      return Optional.empty();
    }
  }

  private JiffiesAccounting() {}
}
