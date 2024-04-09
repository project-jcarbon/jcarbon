package jcarbon.emissions;

import java.util.List;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.cpu.eflect.TaskEnergy;
import jcarbon.cpu.rapl.RaplEnergy;
import jcarbon.cpu.rapl.RaplReading;
import jcarbon.data.Interval;

/** A class that converts an interval of joules to co2 emissions. */
public final class EmissionsConverter {
  private static final double JOULE_TO_KWH = 2.77778e-7;

  private final double carbonIntensity;

  public EmissionsConverter(double carbonIntensity) {
    this.carbonIntensity = carbonIntensity;
  }

  public <T extends Interval<?>> EmissionsInterval convert(T interval) {
    double emissions = 0;
    if (interval instanceof RaplEnergy) {
      emissions = convertRaplEnergy((RaplEnergy) interval);
    } else if (interval instanceof ProcessEnergy) {
      emissions = convertProcessEnergy((ProcessEnergy) interval);
    }
    return new EmissionsInterval(interval.start(), interval.end(), interval.component(), emissions);
  }

  private double convertRaplEnergy(RaplEnergy interval) {
    double joules = 0;
    RaplReading[] readings = interval.data();
    for (RaplReading e : readings) {
      joules += e.total;
    }
    return convertJoules(joules);
  }

  private double convertProcessEnergy(ProcessEnergy interval) {
    double joules = 0;
    List<TaskEnergy> readings = interval.data();
    for (TaskEnergy e : readings) {
      joules += e.energy;
    }
    return convertJoules(joules);
  }

  private double convertJoules(double joules) {
    return carbonIntensity * joules * JOULE_TO_KWH;
  }
}
