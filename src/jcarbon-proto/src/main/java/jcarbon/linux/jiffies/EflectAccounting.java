package jcarbon.linux.jiffies;

import static jcarbon.linux.CpuInfo.getCpuSocketMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.signal.SignalInterval.Timestamp;
import jcarbon.util.Timestamps;

/** Class to compute the energy consumption of tasks based on fractional consumption. */
public final class EflectAccounting {
  private static final int[] SOCKETS_MAP = getCpuSocketMapping();

  /**
   * Computes the attributed energy of all tasks in the overlapping region of two intervals by using
   * the fractional activity per socket.
   */
  public static Optional<SignalInterval> computeTaskEnergy(
      SignalInterval process, SignalInterval energy) {
    HashMap<Integer, List<SignalData>> readings = new HashMap<>();
    for (SignalData data : energy.getDataList()) {
      int socket = Integer.parseInt(data.getMetadata(0).getValue());
      readings.computeIfAbsent(socket, s -> new ArrayList<>());
      readings.get(socket).add(data);
    }
    if (readings.size() == 0) {
      return Optional.empty();
    }

    // Get the fraction of time the interval encompasses.
    Timestamp start = Timestamps.max(process.getStart(), energy.getStart());
    Timestamp end = Timestamps.min(process.getEnd(), energy.getEnd());
    double intervalFraction =
        Timestamps.divide(
            Timestamps.between(start, end), Timestamps.between(energy.getStart(), energy.getEnd()));

    ArrayList<SignalData> tasks = new ArrayList<>();
    double[] totalActivity = new double[readings.size()];
    // Set this up for the conversation to sockets.
    for (SignalData activity : process.getDataList()) {
      int cpu = Integer.parseInt(activity.getMetadata(1).getValue());
      totalActivity[SOCKETS_MAP[cpu]] += activity.getValue();
    }
    for (SignalData activity : process.getDataList()) {
      // Don't bother if there is no activity.
      if (activity.getValue() == 0) {
        continue;
      }

      int cpu = Integer.parseInt(activity.getMetadata(1).getValue());
      int socket = SOCKETS_MAP[cpu];
      // Don't bother if there is no energy.
      if (!readings.containsKey(socket)) {
        continue;
      }

      // Attribute a fraction of the total energy to the task based on its activity on the socket.
      tasks.add(
          activity.toBuilder()
              .addMetadata(
                  SignalData.Metadata.newBuilder().setName("component").setValue("package"))
              .setValue(
                  readings.get(socket).get(0).getValue()
                      * intervalFraction
                      * activity.getValue()
                      / totalActivity[socket])
              .build());
      if (readings.get(socket).size() > 1) {
        tasks.add(
            activity.toBuilder()
                .addMetadata(SignalData.Metadata.newBuilder().setName("component").setValue("dram"))
                .setValue(
                    readings.get(socket).get(1).getValue()
                        * intervalFraction
                        * activity.getValue()
                        / totalActivity[socket])
                .build());
      }
    }
    if (!tasks.isEmpty()) {
      return Optional.of(
          SignalInterval.newBuilder().setStart(start).setEnd(end).addAllData(tasks).build());
    } else {
      return Optional.empty();
    }
  }

  private EflectAccounting() {}
}
