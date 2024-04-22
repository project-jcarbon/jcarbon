package jcarbon.cpu.eflect;

import static jcarbon.cpu.CpuInfo.getCpuSocketMapping;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import jcarbon.cpu.jiffies.ProcessActivity;
import jcarbon.cpu.jiffies.TaskActivity;
import jcarbon.cpu.rapl.RaplEnergy;
import jcarbon.data.TimeOperations;

/** Class to compute the energy consumption of tasks based on fractional consumption. */
public final class EflectAccounting {
  private static final int[] SOCKETS_MAP = getCpuSocketMapping();

  /**
   * Computes the attributed energy of all tasks in the overlapping region of two intervals by using
   * the fractional activity per socket.
   */
  public static Optional<ProcessEnergy> computeTaskEnergy(
      ProcessActivity process, RaplEnergy energy) {
    if (energy.data().length == 0) {
      return Optional.empty();
    }

    // Get the fraction of time the interval encompasses.
    Instant start = TimeOperations.max(process.start(), energy.start());
    Instant end = TimeOperations.min(process.end(), energy.end());
    double intervalFraction =
        TimeOperations.divide(
            Duration.between(start, end), Duration.between(energy.start(), energy.end()));

    ArrayList<TaskEnergy> tasks = new ArrayList<>();
    double[] totalActivity = new double[energy.data().length];
    // Set this up for the conversation to sockets.
    for (TaskActivity activity : process.data()) {
      totalActivity[SOCKETS_MAP[activity.component.cpu]] += activity.activity;
    }
    for (TaskActivity activity : process.data()) {
      // Don't bother if there is no activity.
      if (activity.activity == 0) {
        continue;
      }

      int socket = SOCKETS_MAP[activity.component.cpu];
      // Don't bother if there is no energy.
      if (energy.data()[socket].energy == 0) {
        continue;
      }

      // Attribute a fraction of the total energy to the task based on its activity on the socket.
      double taskEnergy =
          energy.data()[socket].energy
              * intervalFraction
              * activity.activity
              / totalActivity[socket];
      tasks.add(new TaskEnergy(activity.component, taskEnergy));
    }
    if (!tasks.isEmpty()) {
      return Optional.of(new ProcessEnergy(start, end, process.component, tasks));
    } else {
      return Optional.empty();
    }
  }

  private EflectAccounting() {}
}
