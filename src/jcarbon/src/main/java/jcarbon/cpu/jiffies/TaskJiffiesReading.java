package jcarbon.cpu.jiffies;

/** A reading of jiffies from proc/<pid>/task/<tid>/stat. */
public final class TaskJiffiesReading {
  // TODO: immutable data structures are "safe" as public
  public final long taskId;
  public final long processId;
  public final int cpu;
  public final int userJiffies;
  public final int systemJiffies;
  public final int totalJiffies;

  TaskJiffiesReading(long taskId, long processId, int cpu, int userJiffies, int systemJiffies) {
    this.taskId = taskId;
    this.processId = processId;
    this.cpu = cpu;
    this.userJiffies = userJiffies;
    this.systemJiffies = systemJiffies;
    this.totalJiffies = userJiffies + systemJiffies;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"task_id\":%d,\"process_id\":%d,\"cpu\":%d,\"user_jiffies\":%d,\"system_jiffies\":%d}",
        taskId, processId, cpu, userJiffies, systemJiffies);
  }
}
