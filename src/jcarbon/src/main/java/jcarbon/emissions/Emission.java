package jcarbon.emissions;

import jcarbon.data.Component;

/** Carbon emission of some component. */
public final class Emission {
  // TODO: immutable data structures are "safe" as public
  public final Component component;
  public final double emissions;

  Emission(Component component, double emissions) {
    this.component = component;
    this.emissions = emissions;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format("{\"component\":%s,\"emissions\":%.6f}", component, emissions);
  }
}
