package jcarbon.cpu.eflect;

/** Energy consumption of a task. */
public final class TaskEnergy {
  // TODO: immutable data structures are "safe" as public
  public final long taskId;
  public final long processId;
  public final int cpu;
  public final double energy;

  TaskEnergy(long taskId, long processId, int cpu, double energy) {
    this.taskId = taskId;
    this.processId = processId;
    this.cpu = cpu;
    this.energy = energy;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"task_id\":%d,\"process_id\":%d,\"cpu\":%d,\"energy\":%.6f}",
        taskId, processId, cpu, energy);
  }
}
