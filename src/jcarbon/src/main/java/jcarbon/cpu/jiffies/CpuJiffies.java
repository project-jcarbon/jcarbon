package jcarbon.cpu.jiffies;

/** A reading of jiffies from /proc/stat. */
public final class CpuJiffies {
  // TODO: immutable data structures are "safe" as public
  public final int cpu;
  public final int user;
  public final int nice;
  public final int system;
  public final int idle;
  public final int iowait;
  public final int irq;
  public final int softirq;
  public final int steal;
  public final int guest;
  public final int guestNice;
  public final int activeJiffies;

  CpuJiffies(
      int cpu,
      int user,
      int nice,
      int system,
      int idle,
      int iowait,
      int irq,
      int softirq,
      int steal,
      int guest,
      int guestNice) {
    this.cpu = cpu;
    this.user = user;
    this.nice = nice;
    this.system = system;
    this.idle = idle;
    this.iowait = iowait;
    this.irq = irq;
    this.softirq = softirq;
    this.steal = steal;
    this.guest = guest;
    this.guestNice = guestNice;
    // TODO: confirm which of these are meaningful; at least user and system are, but what else?
    this.activeJiffies = user + nice + system + iowait + irq + softirq + steal + guest + guestNice;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"cpu\":%d, \"user\":%d, \"nice\":%d, \"system\":%d, \"idle\":%d, \"iowait\":%d,"
            + " \"irq\":%d, \"softirq\":%d, \"steal\":%d, \"guest\":%d, \"guest_nice\":%d}",
        cpu, user, nice, system, idle, iowait, irq, softirq, steal, guest, guestNice);
  }
}
