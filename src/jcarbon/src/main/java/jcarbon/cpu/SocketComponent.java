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

  @Override
  public boolean equals(Object o) {
    if (o instanceof SocketComponent) {
      SocketComponent other = (SocketComponent) o;
      return this.socket == other.socket;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(socket);
  }
}
