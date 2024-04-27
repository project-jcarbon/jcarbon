package jcarbon.emissions;

import jcarbon.data.Data;

/** Carbon emission of some component. */
public final class Emission implements Data {
  // TODO: immutable data structures are "safe" as public
  public final double carbon;

  private final String component;

  Emission(String component, double carbon) {
    this.carbon = carbon;
    this.component = component;
  }

  @Override
  public String component() {
    return component;
  }

  @Override
  public double value() {
    return carbon;
  }

  @Override
  public String toString() {
    return toJson();
  }
}
