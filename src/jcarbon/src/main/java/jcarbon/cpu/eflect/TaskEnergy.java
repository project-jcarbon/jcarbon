package jcarbon.cpu.eflect;

import jcarbon.cpu.TaskComponent;
import jcarbon.data.Component;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** Energy consumption of a task. */
public final class TaskEnergy implements Data {
  // TODO: immutable data structures are "safe" as public
  public final TaskComponent component;
  public final double energy;

  TaskEnergy(TaskComponent taskComponent, double energy) {
    this.component = taskComponent;
    this.energy = energy;
  }

  @Override
  public Component component() {
    return component;
  }

  @Override
  public Unit unit() {
    return Unit.JOULES;
  }

  @Override
  public double value() {
    return energy;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"task_id\":%d,\"process_id\":%d,\"cpu\":%d,\"energy\":%.6f}",
        component.taskId, component.processId, component.cpu, energy);
  }
}
