package jcarbon.cpu.jiffies;

import static java.util.stream.Collectors.toMap;
import static jcarbon.data.DataOperations.forwardApply;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jcarbon.data.TimeOperations;

public final class JiffiesAccounting {
  public static List<TaskActivityInterval> accountTasks(
      List<ProcessJiffiesSample> process, List<SystemJiffiesSample> system) {
    List<ProcessJiffiesInterval> procInterval =
        forwardApply(process, JiffiesAccounting::processDifference);
    List<SystemJiffiesInterval> sysInterval =
        forwardApply(system, JiffiesAccounting::systemDifference);
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
      for (TaskJiffiesReading reading : proc.data()) {
        totalJiffies[reading.cpu] += reading.totalJiffies;
      }
      for (TaskJiffiesReading reading : proc.data()) {
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

  private static SystemJiffiesInterval systemDifference(
      SystemJiffiesSample first, SystemJiffiesSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    return new SystemJiffiesInterval(
        first.timestamp(), second.timestamp(), difference(first.data(), second.data()));
  }

  public static CpuJiffiesReading[] difference(
      CpuJiffiesReading[] first, CpuJiffiesReading[] second) {
    if (first.length != second.length) {
      throw new IllegalArgumentException(
          String.format(
              "readings do not have the same number of cpus (%s != %s)",
              first.length, second.length));
    }
    CpuJiffiesReading[] readings = new CpuJiffiesReading[first.length];
    for (CpuJiffiesReading reading : first) {
      readings[reading.cpu] =
          new CpuJiffiesReading(
              reading.cpu,
              second[reading.cpu].user - reading.user,
              second[reading.cpu].nice - reading.nice,
              second[reading.cpu].system - reading.system,
              second[reading.cpu].idle - reading.idle,
              second[reading.cpu].iowait - reading.iowait,
              second[reading.cpu].irq - reading.irq,
              second[reading.cpu].softirq - reading.softirq,
              second[reading.cpu].steal - reading.steal,
              second[reading.cpu].guest - reading.guest,
              second[reading.cpu].guestNice - reading.guestNice);
    }
    return readings;
  }

  public static ProcessJiffiesInterval processDifference(
      ProcessJiffiesSample first, ProcessJiffiesSample second) {
    if (first.compareTo(second) > -1) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp(), second.timestamp()));
    }
    return new ProcessJiffiesInterval(
        first.timestamp(),
        second.timestamp(),
        first.processId(),
        difference(first.data(), second.data()));
  }

  public static List<TaskJiffiesReading> difference(
      List<TaskJiffiesReading> first, List<TaskJiffiesReading> second) {
    Map<Long, TaskJiffiesReading> secondMap = second.stream().collect(toMap(r -> r.taskId, r -> r));
    ArrayList<TaskJiffiesReading> readings = new ArrayList<>();
    for (TaskJiffiesReading reading : first) {
      if (secondMap.containsKey(reading.taskId)) {
        TaskJiffiesReading other = secondMap.get(reading.taskId);
        if ((other.userJiffies - reading.userJiffies) > 0
            || (other.systemJiffies - reading.systemJiffies) > 0) {
          readings.add(
              new TaskJiffiesReading(
                  reading.taskId,
                  reading.processId,
                  reading.cpu,
                  Math.max(0, other.userJiffies - reading.userJiffies),
                  Math.max(0, other.systemJiffies - reading.systemJiffies)));
        }
      }
    }
    return readings;
  }

  private JiffiesAccounting() {}
}
