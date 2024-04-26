package jcarbon.cpu.jiffies;

import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** Fractional activity (i.e. cpu utilization) of a task. */
public final class TaskActivity implements Data {
  // TODO: immutable data structures are "safe" as public
  public final long processId;
  public final long taskId;
  public final int cpu;
  public final double activity;

  private final String component;

  TaskActivity(long processId, long taskId, int cpu, double activity) {
    this.processId = processId;
    this.taskId = taskId;
    this.cpu = cpu;
    this.component = LinuxComponents.taskComponent(processId, taskId);
    this.activity = activity;
  }

  @Override
  public String component() {
    return component;
  }

  @Override
  public Unit unit() {
    return Unit.ACTIVITY;
  }

  @Override
  public double value() {
    return activity;
  }

  @Override
  public String toString() {
    return toJson();
  }
}
