package jcarbon.emissions;

import java.util.ArrayList;
import jcarbon.data.Data;
import jcarbon.data.Interval;
import jcarbon.data.Unit;

/** An emissions converter that converts an interval of joules to co2 emissions. */
public final class JoulesEmissionsConverter implements EmissionsConverter {
  private static final double JOULE_TO_KWH = 2.77778e-7;

  // grams of carbon per kwh
  private final double carbonIntensity;

  public JoulesEmissionsConverter(double carbonIntensity) {
    this.carbonIntensity = carbonIntensity;
  }

  @Override
  public <T extends Interval<? extends Data>> Emissions convert(T interval) {
    ArrayList<Emission> emissions = new ArrayList<>();
    if (interval.unit() == Unit.JOULES) {
      for (Data data : interval.data()) {
        emissions.add(new Emission(data.component(), convertJoules(data.value())));
      }
    }
    return new Emissions(interval.start(), interval.end(), interval.component(), emissions);
  }

  public double convertJoules(double joules) {
    return carbonIntensity * joules * JOULE_TO_KWH;
  }
}
