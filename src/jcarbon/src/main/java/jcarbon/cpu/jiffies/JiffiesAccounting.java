package jcarbon.cpu.jiffies;

import static jcarbon.data.DataOperations.forwardAlign;
import static jcarbon.data.DataOperations.forwardApply;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jcarbon.data.TimeOperations;

public final class JiffiesAccounting {
  public static List<TaskActivityInterval> accountTasks(
      List<ProcessSample> process, List<SystemSample> system) {
    return forwardAlign(
        forwardApply(process, ProcessJiffiesInterval::between),
        forwardApply(system, SystemJiffiesInterval::between),
        JiffiesAccounting::accountInterval);
  }

  private static Optional<TaskActivityInterval> accountInterval(
      ProcessJiffiesInterval proc, SystemJiffiesInterval sys) {
    ArrayList<TaskActivity> tasks = new ArrayList<>();
    int[] totalJiffies = new int[sys.data().length];
    for (TaskJiffies reading : proc.data()) {
      totalJiffies[reading.cpu] += reading.totalJiffies;
    }
    for (TaskJiffies reading : proc.data()) {
      if (reading.totalJiffies == 0) {
        continue;
      }
      double cpuJiffies = sys.data()[reading.cpu].activeJiffies;
      double taskActivity =
          Math.min(
              1.0,
              reading.totalJiffies / Math.max(1, Math.max(cpuJiffies, totalJiffies[reading.cpu])));
      tasks.add(new TaskActivity(reading.taskId, reading.processId, reading.cpu, taskActivity));
    }
    if (!tasks.isEmpty()) {
      return Optional.of(
          new TaskActivityInterval(
              TimeOperations.max(proc.start(), sys.start()),
              TimeOperations.min(proc.end(), sys.end()),
              tasks));
    } else {
      return Optional.empty();
    }
  }

  private JiffiesAccounting() {}
}
