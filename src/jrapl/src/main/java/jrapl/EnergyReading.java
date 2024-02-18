package jrapl;

/** A reading from a rapl energy system. */
public final class EnergyReading {
  // TODO: immutable data structures are "safe" as public
  // energy domain
  public final int socket;
  // energy readings by component in joules
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

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"socket\":%d,\"package\":%.6f,\"dram\":%.6f,\"core\":%.6f,\"gpu\":%.6f}",
        socket, pkg, dram, core, gpu);
  }
}
