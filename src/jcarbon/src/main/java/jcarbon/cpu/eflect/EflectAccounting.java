package jcarbon.cpu.eflect;

import static jcarbon.data.DataOperations.forwardAlign;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jcarbon.cpu.jiffies.TaskActivity;
import jcarbon.cpu.jiffies.TaskActivityInterval;
import jcarbon.cpu.rapl.Powercap;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.data.TimeOperations;

public final class EflectAccounting {
  private static final String CPU_INFO = "/proc/cpuinfo";
  private static final int[] CPU_SOCKET_MAPPING = getCpuSocketMapping();

  public static List<EnergyFootprint> accountTasks(
      List<TaskActivityInterval> tasks, List<RaplInterval> energy) {
    return forwardAlign(tasks, energy, EflectAccounting::accountInterval);
  }

  private static Optional<EnergyFootprint> accountInterval(
      TaskActivityInterval task, RaplInterval energy) {
    ArrayList<TaskEnergy> tasks = new ArrayList<>();
    int[] totalActivity = new int[Powercap.SOCKETS];
    for (TaskActivity activity : task.data()) {
      totalActivity[CPU_SOCKET_MAPPING[activity.cpu]] += activity.activity;
    }
    for (TaskActivity activity : task.data()) {
      if (activity.activity == 0) {
        continue;
      }

      int socket = CPU_SOCKET_MAPPING[activity.cpu];
      if (energy.data()[socket].total == 0) {
        continue;
      }

      double taskEnergy = energy.data()[socket].total * activity.activity / totalActivity[socket];
      tasks.add(new TaskEnergy(activity.taskId, activity.processId, activity.cpu, taskEnergy));
    }
    if (!tasks.isEmpty()) {
      return Optional.of(
          new EnergyFootprint(
              TimeOperations.max(task.start(), energy.start()),
              TimeOperations.min(task.end(), energy.end()),
              tasks));
    } else {
      return Optional.empty();
    }
  }

  private static int[] getCpuSocketMapping() {
    int[] mapping = new int[Runtime.getRuntime().availableProcessors()];
    // TODO: using the traditional java method to support android
    try {
      BufferedReader reader = new BufferedReader(new FileReader(CPU_INFO));
      int lastCpu = -1;
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        } else if (line.contains("processor")) {
          lastCpu = Integer.parseInt(line.split(":")[1].trim());
        } else if (line.contains("physical id")) {
          mapping[lastCpu] = Integer.parseInt(line.split(":")[1].trim());
        }
      }
      reader.close();
    } catch (Exception e) {
      System.out.println("unable to read cpuinfo");
    }
    return mapping;
  }

  private EflectAccounting() {}
}
