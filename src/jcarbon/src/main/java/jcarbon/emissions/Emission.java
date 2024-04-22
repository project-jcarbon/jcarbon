package jcarbon.emissions;

import jcarbon.data.Component;
import jcarbon.data.Data;
import jcarbon.data.Unit;

/** Carbon emission of some component. */
public final class Emission implements Data {
  // TODO: immutable data structures are "safe" as public
  public final Component component;
  public final double carbon;

  Emission(Component component, double carbon) {
    this.component = component;
    this.carbon = carbon;
  }

  @Override
  public Component component() {
    return component;
  }

  @Override
  public Unit unit() {
    return Unit.GRAMS_OF_CO2;
  }

  @Override
  public double value() {
    return carbon;
  }


  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format("{\"component\":%s,\"emissions\":%.6f}", component, carbon);
  }
}
