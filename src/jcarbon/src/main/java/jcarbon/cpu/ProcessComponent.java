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
}
