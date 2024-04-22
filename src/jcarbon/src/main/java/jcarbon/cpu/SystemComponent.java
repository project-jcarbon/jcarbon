package jcarbon.cpu;

import jcarbon.data.Component;

public class SystemComponent implements Component {
  public static final SystemComponent INSTANCE = new SystemComponent(System.getProperty("os.name"));

  private final String osName;

  private SystemComponent(String osName) {
    this.osName = osName;
  }

  @Override
  public String toString() {
    return osName;
  }
}
