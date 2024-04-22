package jcarbon.cpu.jiffies;

import jcarbon.cpu.TaskComponent;
import jcarbon.data.Component;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** Jiffies from proc/<pid>/task/<tid>/stat. */
public final class TaskJiffies implements Data {
  // TODO: immutable data structures are "safe" as public
  public final int userJiffies;
  public final int systemJiffies;
  public final int totalJiffies;
  public final TaskComponent component;

  TaskJiffies(long processId, long taskId, int cpu, int userJiffies, int systemJiffies) {
    this.userJiffies = userJiffies;
    this.systemJiffies = systemJiffies;
    this.totalJiffies = userJiffies + systemJiffies;
    this.component = new TaskComponent(processId, taskId, cpu);
  }

  TaskJiffies(TaskComponent taskComponent, int userJiffies, int systemJiffies) {
    this.userJiffies = userJiffies;
    this.systemJiffies = systemJiffies;
    this.totalJiffies = userJiffies + systemJiffies;
    this.component = taskComponent;
  }

  @Override
  public Component component() {
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
    // TODO: temporarily using json
    return String.format(
        "{\"task_id\":%d,\"process_id\":%d,\"cpu\":%d,\"user_jiffies\":%d,\"system_jiffies\":%d}",
        component.taskId, component.processId, component.cpu, userJiffies, systemJiffies);
  }
}
