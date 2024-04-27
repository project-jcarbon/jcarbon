package jcarbon.cpu;

public final class LinuxComponents {
  public static final String OS_COMPONENT =
      String.format("os=%s", System.getProperty("os.name", "unknown"));

  public static String socketComponent(int socket) {
    return String.format("socket=%d", socket);
  }

  public static String cpuComponent(int cpu) {
    return String.format("cpu=%d", cpu);
  }

  public static String processComponent(long processId) {
    return String.format("process=%d", processId);
  }

  public static String taskComponent(long processId, long taskId) {
    return String.format("process=%d,task=%d", processId, taskId);
  }

  private LinuxComponents() {}
}
