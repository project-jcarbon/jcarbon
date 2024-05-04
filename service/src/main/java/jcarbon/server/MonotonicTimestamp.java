package jcarbon.server;

import jcarbon.util.NativeUtils;

/** Class that provides a singleton interface to c's monotonic timestamp. */
public final class MonotonicTimestamp {
  private static MonotonicTimestamp instance;

  /** Returns the rapl instance, creating a new one if necessary. */
  public static MonotonicTimestamp getInstance() {
    synchronized (MonotonicTimestamp.class) {
      if (instance != null) {
        return instance;
      }
      try {
        NativeUtils.loadLibraryFromJar("/native/libjrapl.so");
      } catch (Exception e) {
      }
      try {
        System.loadLibrary("jrapl");
      } catch (UnsatisfiedLinkError e) {
      }
      instance = new MonotonicTimestamp();
      return instance;
    }
  }

  public native long getMonotonicTimestamp();

  public static void main(String[] args) {
    System.out.println("The time is " + getInstance().getMonotonicTimestamp() + "!");
  }
}
