package jcarbon.cpu.jiffies;

import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** Jiffies from proc/<pid>/task/<tid>/stat. */
public final class TaskJiffies implements Data {
  // TODO: immutable data structures are "safe" as public
  public final long processId;
  public final long taskId;
  public final int cpu;
  public final int userJiffies;
  public final int systemJiffies;
  public final int totalJiffies;

  private final String component;

  TaskJiffies(long processId, long taskId, int cpu, int userJiffies, int systemJiffies) {
    this.processId = processId;
    this.taskId = taskId;
    this.cpu = cpu;
    this.userJiffies = userJiffies;
    this.systemJiffies = systemJiffies;
    this.totalJiffies = userJiffies + systemJiffies;
    this.component = LinuxComponents.taskComponent(processId, taskId);
  }

  @Override
  public String component() {
    return component;
  }

  @Override
  public Unit unit() {
    return Unit.JIFFIES;
  }

  @Override
  public double value() {
    return totalJiffies;
  }

  @Override
  public String toString() {
    return toJson();
  }
}
