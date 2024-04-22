package jcarbon.data;

public interface Data {
  /** Where the data comes from. */
  Component component();

  /** What the data is. */
  Unit unit();

  /** What is in the data. */
  double value();
}
