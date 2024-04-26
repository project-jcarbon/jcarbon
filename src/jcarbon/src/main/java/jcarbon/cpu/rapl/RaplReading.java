package jcarbon.cpu.rapl;

import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** A reading from a rapl energy system. */
public final class RaplReading implements Data {
  // TODO: immutable data structures are "safe" as public
  // energy domain
  public final int socket;
  // energy readings by component in joules
  public final double pkg;
  public final double dram;
  public final double core;
  public final double gpu;
  // convenience value
  public final double energy;
  public final String component;

  RaplReading(int socket, double pkg, double dram, double core, double gpu) {
    this.socket = socket;
    this.pkg = pkg;
    this.dram = dram;
    this.core = core;
    this.gpu = gpu;
    this.energy = pkg + dram + core + gpu;
    this.component = LinuxComponents.socketComponent(socket);
  }

  @Override
  public String component() {
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
    return toJson();
  }
}
