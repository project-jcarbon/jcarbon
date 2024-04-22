package jcarbon.cpu.jiffies;

import jcarbon.cpu.TaskComponent;
import jcarbon.data.Component;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** Fractional activity (i.e. cpu utilization) of a task. */
public final class TaskActivity implements Data {
  // TODO: immutable data structures are "safe" as public
  public final TaskComponent component;
  public final double activity;

  TaskActivity(TaskComponent taskComponent, double activity) {
    this.component = taskComponent;
    this.activity = activity;
  }

  @Override
  public Component component() {
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
    // TODO: temporarily using json
    return String.format(
        "{\"task_id\":%d,\"process_id\":%d,\"cpu\":%d,\"activity\":%.6f}",
        component.taskId, component.processId, component.cpu, activity);
  }
}
