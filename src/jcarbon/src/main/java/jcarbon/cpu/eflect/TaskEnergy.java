package jcarbon.cpu.eflect;

import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Data;

/** Energy consumption of a task. */
public final class TaskEnergy implements Data {
  // TODO: immutable data structures are "safe" as public
  public final long processId;
  public final long taskId;
  public final int cpu;
  public final double energy;

  private final String component;

  TaskEnergy(long processId, long taskId, int cpu, double energy) {
    this.processId = processId;
    this.taskId = taskId;
    this.cpu = cpu;
    this.component = LinuxComponents.taskComponent(processId, taskId);
    this.energy = energy;
  }

  @Override
  public String component() {
    return component;
  }

  @Override
  public double value() {
    return energy;
  }

  @Override
  public String toString() {
    return toJson();
  }
}
