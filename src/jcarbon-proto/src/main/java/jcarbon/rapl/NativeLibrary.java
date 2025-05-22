package jcarbon.rapl;

import java.io.File;
import jcarbon.util.NativeUtils;

/**
 * Simple wrapper around the native backend management. You should never need to access this; the
 * other classes in the library will provide the right behavior
 */
final class NativeLibrary {
  private static boolean IS_AVAILABLE = loadLibrary();
  private static boolean IS_INITIALIZED = false;

  /** Synchronized helper for {@code initializeNative}. */
  static boolean initialize() {
    synchronized (NativeLibrary.class) {
      if (IS_AVAILABLE && !IS_INITIALIZED) {
        IS_INITIALIZED = true;
        initializeNative();
      }
    }
    return IS_INITIALIZED;
  }

  /** Synchronized helper for {@code deallocateNative}. */
  static void deallocate() {
    synchronized (NativeLibrary.class) {
      if (IS_AVAILABLE && IS_INITIALIZED) {
        IS_INITIALIZED = false;
        deallocateNative();
      }
    }
  }

  /** Makes a safe attempt to load the library. */
  private static boolean loadLibrary() {
    if (new File("/dev/cpu/0/msr").exists()) {
      try {
        NativeUtils.loadLibraryFromJar("/native/libjrapl.so");
        return true;
      } catch (Exception e) {
      }
      try {
        System.loadLibrary("jrapl");
        return true;
      } catch (UnsatisfiedLinkError e) {
      }
    }
    return false;
  }

  /** Initializes a reference to the rapl registers. Must call before doing anything else. */
  private static native void initializeNative();

  /** Deallocates the reference to the rapl registers. Should call when done. */
  public static native void deallocateNative();

  private NativeLibrary() {}
}
