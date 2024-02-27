package jcarbon.cpu.jiffies;

/** Fractional activity (i.e. cpu utilization) of a task. */
public final class TaskActivity {
  // TODO: immutable data structures are "safe" as public
  public final long taskId;
  public final long processId;
  public final int cpu;
  public final double activity;

  TaskActivity(long taskId, long processId, int cpu, double activity) {
    this.taskId = taskId;
    this.processId = processId;
    this.cpu = cpu;
    this.activity = activity;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"task_id\":%d,\"process_id\":%d,\"cpu\":%d,\"activity\":%.6f}",
        taskId, processId, cpu, activity);
  }
}
