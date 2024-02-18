package jrapl;

public final class EnergyReading {
  public final int socket;
  public final double pkg;
  public final double dram;
  public final double core;
  public final double gpu;
  public final double total;

  EnergyReading(int socket, double pkg, double dram, double core, double gpu) {
    this.socket = socket;
    this.pkg = pkg;
    this.dram = dram;
    this.core = core;
    this.gpu = gpu;
    this.total = pkg + dram + core + gpu;
  }
}
