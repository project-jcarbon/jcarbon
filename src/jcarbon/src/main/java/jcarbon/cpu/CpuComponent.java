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

  @Override
  public boolean equals(Object o) {
    if (o instanceof CpuComponent) {
      CpuComponent other = (CpuComponent) o;
      return this.cpu == other.cpu;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(cpu);
  }

  public SocketComponent asSocket() {
    return new SocketComponent(CPU_TO_SOCKETS[this.cpu]);
  }
}
