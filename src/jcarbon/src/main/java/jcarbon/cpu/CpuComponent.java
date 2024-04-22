package jcarbon.cpu;

import jcarbon.data.Component;

public final class CpuComponent implements Component {
  private static final int[] CPU_TO_SOCKETS = CpuInfo.getCpuSocketMapping();

  public final int cpu;
  public final String component;

  public CpuComponent(int cpu) {
    this.cpu = cpu;
    this.component = String.format("cpu-%d", cpu);
  }

  @Override
  public String toString() {
    return component;
  }

  public SocketComponent asSocket() {
    return new SocketComponent(CPU_TO_SOCKETS[this.cpu]);
  }
}
