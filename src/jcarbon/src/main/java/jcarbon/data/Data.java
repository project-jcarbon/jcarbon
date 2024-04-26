package jcarbon.data;

public interface Data {
  /** Where the data comes from. */
  String component();

  /** What the data is. */
  Unit unit();

  /** What is in the data. */
  double value();

  public default String toJson() {
    return String.format(
        "{\"component\":\"%s\",\"unit\":\"%s\",\"value\":%f}", component(), unit(), value());
  }
}
