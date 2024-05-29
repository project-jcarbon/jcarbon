package jcarbon.emissions;

import static java.util.stream.Collectors.toList;

import jcarbon.signal.Signal;
import jcarbon.signal.Signal.Unit;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;

/** An emissions converter that converts an interval of joules to co2 emissions. */
public final class JoulesEmissionsConverter implements EmissionsConverter {
  private static final double JOULE_TO_KWH = 2.77778e-7;

  // grams of carbon per kwh
  private final double carbonIntensity;

  public JoulesEmissionsConverter(double carbonIntensity) {
    this.carbonIntensity = carbonIntensity;
  }

  @Override
  public Signal convert(Signal signal) {
    Signal.Builder emissionsSignal = Signal.newBuilder();
    if (signal.getUnit() == Unit.JOULES) {
      return Signal.newBuilder()
          .setUnit(Signal.Unit.GRAMS_OF_CO2)
          .addAllSource(signal.getSourceList())
          .addAllInterval(
              signal.getIntervalList().stream()
                  .map(
                      interval ->
                          SignalInterval.newBuilder()
                              .setStart(interval.getStart())
                              .setEnd(interval.getEnd())
                              .addAllData(
                                  interval.getDataList().stream()
                                      .map(
                                          data ->
                                              SignalData.newBuilder()
                                                  .addAllMetadata(data.getMetadataList())
                                                  .setValue(convertJoules(data.getValue()))
                                                  .build())
                                      .collect(toList()))
                              .build())
                  .collect(toList()))
          .build();
    }
    return Signal.getDefaultInstance();
  }

  private double convertJoules(double joules) {
    return carbonIntensity * joules * JOULE_TO_KWH;
  }
}
