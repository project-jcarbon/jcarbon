package jcarbon.data;

public interface Data {
  /** Where the data comes from. */
  String component();

  /** What is in the data. */
  double value();

  public default String toJson() {
    return String.format("{\"component\":\"%s\",\"value\":%f}", component(), value());
  }
}
