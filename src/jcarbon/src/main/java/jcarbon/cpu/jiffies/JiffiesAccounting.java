package jcarbon.cpu.jiffies;

import java.util.ArrayList;
import java.util.Optional;
import jcarbon.data.TimeOperations;

/** Class to compute the activity of tasks in a process using jiffies. */
public final class JiffiesAccounting {
  /**
   * Computes the activity of all tasks in the overlapping region of two intervals by using the
   * ratio between a task's jiffies and cpu jiffies of the task's executing cpu. This also safely
   * bounds the value from 0 to 1, in the cases that the jiffies are misaligned due to the kernel
   * update timing.
   */
  // TODO: Need to find (or write) something that strictly mentions the timing issue
  public static Optional<ProcessActivity> computeTaskActivity(
      ProcessJiffies proc, SystemJiffies sys) {
    if (proc.start().isAfter(sys.end()) || sys.start().isAfter(proc.end())) {
      return Optional.empty();
    }
    ArrayList<TaskActivity> tasks = new ArrayList<>();
    // Set this up to correct for kernel update.
    int[] totalJiffies = new int[sys.data().length];
    for (TaskJiffies reading : proc.data()) {
      totalJiffies[reading.cpu] += reading.totalJiffies;
    }
    for (TaskJiffies task : proc.data()) {
      // Don't bother if there are no jiffies.
      if (task.totalJiffies == 0) {
        continue;
      }
      // Correct for the kernel update by using total jiffies reported by tasks if the cpu
      // reported one is too small (this also catches zero jiffies reported by the cpu).
      double cpuJiffies = Math.max(sys.data()[task.cpu].activeJiffies, totalJiffies[task.cpu]);
      double taskActivity = Math.min(1.0, task.totalJiffies / cpuJiffies);
      tasks.add(new TaskActivity(task.taskId, task.processId, task.cpu, taskActivity));
    }
    // Don't bother if there is no activity.
    if (!tasks.isEmpty()) {
      return Optional.of(
          new ProcessActivity(
              TimeOperations.max(proc.start(), sys.start()),
              TimeOperations.min(proc.end(), sys.end()),
              tasks));
    } else {
      return Optional.empty();
    }
  }

  private JiffiesAccounting() {}
}
