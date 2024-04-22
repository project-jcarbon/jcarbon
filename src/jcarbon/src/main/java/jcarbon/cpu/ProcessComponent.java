package jcarbon.cpu;

import jcarbon.data.Component;

public class ProcessComponent implements Component {
  public final long processId;

  public ProcessComponent(long processId) {
    this.processId = processId;
  }

  @Override
  public String toString() {
    return String.format("process-%d", processId);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ProcessComponent) {
      ProcessComponent other = (ProcessComponent) o;
      return this.processId == other.processId;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(processId);
  }
}
