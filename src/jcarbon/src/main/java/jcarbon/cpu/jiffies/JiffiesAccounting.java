package jcarbon.cpu.jiffies;

import static jcarbon.data.DataOperations.forwardApply;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jcarbon.data.TimeOperations;

public final class JiffiesAccounting {
  public static List<TaskActivityInterval> accountTasks(
      List<ProcessSample> process, List<SystemSample> system) {
    List<ProcessJiffiesInterval> procInterval =
        forwardApply(process, ProcessJiffiesInterval::between);
    List<SystemJiffiesInterval> sysInterval = forwardApply(system, SystemJiffiesInterval::between);
    return alignJiffies(procInterval.iterator(), sysInterval.iterator());
  }

  private static List<TaskActivityInterval> alignJiffies(
      Iterator<ProcessJiffiesInterval> procIt, Iterator<SystemJiffiesInterval> sysIt) {
    ProcessJiffiesInterval proc = procIt.next();
    SystemJiffiesInterval sys = sysIt.next();
    ArrayList<TaskActivityInterval> activity = new ArrayList<>();
    while (true) {
      // TODO: there needs to be a better way to check if intervals overlap.
      if (!TimeOperations.lessThan(proc.start(), sys.end())) {
        if (!procIt.hasNext()) {
          break;
        }
        proc = procIt.next();
        continue;
      }
      if (!TimeOperations.lessThan(sys.start(), proc.end())) {
        if (!sysIt.hasNext()) {
          break;
        }
        sys = sysIt.next();
        continue;
      }

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
            Math.min(1.0, reading.totalJiffies / Math.max(cpuJiffies, totalJiffies[reading.cpu]));
        tasks.add(new TaskActivity(reading.taskId, reading.processId, reading.cpu, taskActivity));
      }
      if (tasks.size() > 0) {
        activity.add(
            new TaskActivityInterval(
                TimeOperations.max(proc.start(), sys.start()),
                TimeOperations.min(proc.end(), sys.end()),
                tasks));
      }

      if (TimeOperations.lessThan(proc.start(), sys.start())) {
        if (!procIt.hasNext()) {
          break;
        }
        proc = procIt.next();
      } else {
        if (!sysIt.hasNext()) {
          break;
        }
        sys = sysIt.next();
      }
    }
    return activity;
  }

  private JiffiesAccounting() {}
}
