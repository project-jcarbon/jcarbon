package jrapl;

/** Simple wrapper around rapl access. */
public final class MicroArchitecture {
  public static final String UNKNOWN = "UNKNOWN_MICRO_ARCHITECTURE";

  public static final String NAME;
  public static final int SOCKETS;

  /** Returns the name of the micro-architecture. */
  private static native String name();

  /** Returns the number of sockets on the system. */
  private static native int sockets();

  static {
    if (NativeLibrary.initialize()) {
      NAME = name();
      SOCKETS = sockets();
    } else {
      LoggerUtil.LOGGER.warning(
          "native library couldn't be initialized; unable to find a micro-architecture!");
      NAME = UNKNOWN;
      SOCKETS = 0;
    }
  }

  private MicroArchitecture() {}
}
