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
    this.component = String.format("process-%d:task-%d@cpu-%d", processId, taskId, cpu);
  }

  @Override
  public String toString() {
    return component;
  }
}
