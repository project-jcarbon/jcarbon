package jcarbon.rapl;

import static jcarbon.util.LoggerUtil.getLogger;

import java.util.logging.Logger;

/** A class to expose the hardware's microarchitecture. */
public final class MicroArchitecture {
  private static final Logger logger = getLogger();

  // Taken from //src\native\src\main\c\micro_architecture.c
  public static final String UNKNOWN = "UNDEFINED_MICROARCHITECTURE";

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
      logger.warning(
          "native library couldn't be initialized; unable to find a micro-architecture!");
      NAME = UNKNOWN;
      SOCKETS = 0;
    }
  }

  private MicroArchitecture() {}
}
