package jcarbon.cpu.eflect;

import static jcarbon.cpu.jiffies.ProcStat.getCpuSocketMapping;
import static jcarbon.data.DataOperations.forwardPartialAlign;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jcarbon.cpu.jiffies.TaskActivity;
import jcarbon.cpu.jiffies.TaskActivityInterval;
import jcarbon.cpu.rapl.Powercap;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.data.TimeOperations;

/** Class to compute the energy consumption of tasks based on fractional consumption. */
public final class EflectAccounting {
  private static final int[] SOCKETS = getCpuSocketMapping();

  /** Aligns activty and energy. */
  public static List<ProcessEnergy> accountTaskEnergy(
      List<TaskActivityInterval> tasks, List<RaplInterval> energy) {
    return forwardPartialAlign(tasks, energy, EflectAccounting::accountInterval);
  }

  /**
   * Computes the attributed energy of all tasks in the overlapping region of two intervals by using
   * the fractional activity per socket.
   */
  private static Optional<ProcessEnergy> accountInterval(
      TaskActivityInterval task, RaplInterval energy) {
    // Get the fraction of time the interval encompasses.
    Instant start = TimeOperations.max(task.start(), energy.start());
    Instant end = TimeOperations.min(task.end(), energy.end());
    double intervalFraction =
        TimeOperations.divide(
            Duration.between(start, end), Duration.between(energy.start(), energy.end()));

    ArrayList<TaskEnergy> tasks = new ArrayList<>();
    double[] totalActivity = new double[Powercap.SOCKETS];
    // Set this up for the conversation to sockets.
    for (TaskActivity activity : task.data()) {
      totalActivity[SOCKETS[activity.cpu]] += activity.activity;
    }
    for (TaskActivity activity : task.data()) {
      // Don't bother if there is no activity.
      if (activity.activity == 0) {
        continue;
      }

      int socket = SOCKETS[activity.cpu];
      // Don't bother if there is no energy.
      if (energy.data()[socket].total == 0) {
        continue;
      }

      // Attribute a fraction of the total energy to the task based on its activity on the socket.
      double taskEnergy =
          energy.data()[socket].total
              * intervalFraction
              * activity.activity
              / totalActivity[socket];
      tasks.add(new TaskEnergy(activity.taskId, activity.processId, activity.cpu, taskEnergy));
    }
    if (!tasks.isEmpty()) {
      return Optional.of(new ProcessEnergy(start, end, tasks));
    } else {
      return Optional.empty();
    }
  }

  private EflectAccounting() {}
}
