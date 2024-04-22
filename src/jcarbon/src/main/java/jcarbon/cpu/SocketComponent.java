package jcarbon.cpu;

import jcarbon.data.Component;

public final class SocketComponent implements Component {
  public final int socket;
  public final String component;

  public SocketComponent(int socket) {
    this.socket = socket;
    this.component = String.format("socket-%d", socket);
  }

  @Override
  public String toString() {
    return component;
  }
}
