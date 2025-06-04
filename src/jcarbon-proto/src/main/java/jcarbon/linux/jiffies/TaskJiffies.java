package jcarbon.linux.jiffies;


/** Jiffies from proc/<pid>/task/<tid>/stat. */
public final class TaskJiffies {
  // TODO: immutable data structures are "safe" as public
  public final long processId;
  public final long taskId;
  public final int cpu;
  public final int userJiffies;
  public final int systemJiffies;
  public final int totalJiffies;

  TaskJiffies(long processId, long taskId, int cpu, int userJiffies, int systemJiffies) {
    this.processId = processId;
    this.taskId = taskId;
    this.cpu = cpu;
    this.userJiffies = userJiffies;
    this.systemJiffies = systemJiffies;
    this.totalJiffies = userJiffies + systemJiffies;
  }
}
