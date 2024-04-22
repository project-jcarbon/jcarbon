package jcarbon.cpu.jiffies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper for reading task jiffies from /proc system. Refer to
 * https://man7.org/linux/man-pages/man5/proc.5.html
 */
public final class ProcTask {
  private static final long PID = ProcessHandle.current().pid();
  // task stat indicies
  private static final int STAT_LENGTH = 52;

  private enum TaskIndex {
    TID(0),
    CPU(38),
    USER(13),
    SYSTEM(14);

    private int index;

    private TaskIndex(int index) {
      this.index = index;
    }
  };

  /** Reads from a process's tasks and returns a {@link Sample} of it. */
  public static ProcessSample sampleTasksFor(long pid) {
    return new ProcessSample(Instant.now(), pid, parseTasks(readTasks(pid), pid));
  }

  /** Reads this process's tasks and returns a {@link Sample} of it. */
  public static ProcessSample sampleTasks() {
    return new ProcessSample(Instant.now(), PID, parseTasks(readTasks(PID), PID));
  }

  /** Reads stat files of tasks directory of a process. */
  private static final ArrayList<String> readTasks(long pid) {
    ArrayList<String> stats = new ArrayList<String>();
    File tasks = new File(String.join(File.separator, "/proc", Long.toString(pid), "task"));
    if (!tasks.exists()) {
      return stats;
    }

    for (File task : tasks.listFiles()) {
      File statFile = new File(task, "stat");
      if (!statFile.exists()) {
        continue;
      }
      // TODO: if a task terminates while we try to read it, we hang here
      // TODO: using the traditional java method to support android
      try {
        BufferedReader reader = new BufferedReader(new FileReader(statFile));
        stats.add(reader.readLine());
        reader.close();
      } catch (Exception e) {
        System.out.println("unable to read task " + statFile + " before it terminated");
      }
    }
    return stats;
  }

  /** Turns task stat strings into {@link TaskJiffiesReadings}. */
  private static List<TaskJiffies> parseTasks(ArrayList<String> stats, long pid) {
    ArrayList<TaskJiffies> readings = new ArrayList<>();
    for (String s : stats) {
      String[] stat = s.split(" ");
      if (stat.length >= STAT_LENGTH) {
        // task name can be space-delimited, so there may be extra entries
        int offset = stat.length - STAT_LENGTH;
        readings.add(
            new TaskJiffies(
                pid,
                Long.parseLong(stat[TaskIndex.TID.index]),
                // TODO: the name is usually garbage unfortunately :/
                // getName(stat, offset),
                Integer.parseInt(stat[TaskIndex.CPU.index + offset]),
                Integer.parseInt(stat[TaskIndex.USER.index + offset]),
                Integer.parseInt(stat[TaskIndex.SYSTEM.index + offset])));
      }
    }
    return readings;
  }

  /** Extracts the name from the stat string. */
  private static final String getName(String[] stat, int offset) {
    String name = String.join(" ", Arrays.copyOfRange(stat, 1, 2 + offset));
    return name.substring(1, name.length() - 1);
  }

  private ProcTask() {}
}
