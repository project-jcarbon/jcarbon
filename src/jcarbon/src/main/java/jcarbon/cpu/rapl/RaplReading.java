package jcarbon.cpu.rapl;

import jcarbon.cpu.SocketComponent;
import jcarbon.data.Component;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** A reading from a rapl energy system. */
public final class RaplReading implements Data {
  // TODO: immutable data structures are "safe" as public
  // energy domain
  public final SocketComponent component;
  // energy readings by component in joules
  public final double pkg;
  public final double dram;
  public final double core;
  public final double gpu;
  // convenience value
  public final double energy;

  RaplReading(int socket, double pkg, double dram, double core, double gpu) {
    this.component = new SocketComponent(socket);
    this.pkg = pkg;
    this.dram = dram;
    this.core = core;
    this.gpu = gpu;
    this.energy = pkg + dram + core + gpu;
  }

  RaplReading(SocketComponent socketComponent, double pkg, double dram, double core, double gpu) {
    this.component = socketComponent;
    this.pkg = pkg;
    this.dram = dram;
    this.core = core;
    this.gpu = gpu;
    this.energy = pkg + dram + core + gpu;
  }

  @Override
  public Component component() {
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
    // TODO: temporarily using json
    return String.format(
        "{\"socket\":%d,\"package\":%.6f,\"dram\":%.6f,\"core\":%.6f,\"gpu\":%.6f}",
        component.socket, pkg, dram, core, gpu);
  }
}
