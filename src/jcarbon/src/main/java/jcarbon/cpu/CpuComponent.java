package jcarbon.cpu;

import jcarbon.data.Component;

public final class CpuComponent implements Component {
  public static final CpuComponent INSTANCE = new CpuComponent();

  @Override
  public String toString() {
    return "cpu";
  }

  private CpuComponent() {}
}
