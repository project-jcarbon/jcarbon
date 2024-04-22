package jcarbon.cpu;

import jcarbon.data.Component;

public class TaskComponent implements Component {
  public final long processId;
  public final long taskId;
  public final int cpu;
  public final String component;

  public TaskComponent(long processId, long taskId, int cpu) {
    this.processId = processId;
    this.taskId = taskId;
    this.cpu = cpu;
    this.component = String.format("process-%d:task-%d", processId, taskId);
  }

  @Override
  public String toString() {
    return component;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TaskComponent) {
      TaskComponent other = (TaskComponent) o;
      return this.processId == other.processId && this.taskId == other.taskId;
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    return prime * Long.hashCode(processId) + Long.hashCode(taskId);
  }
}
