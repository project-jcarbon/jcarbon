package jcarbon.server;

import static jcarbon.server.LoggerUtil.getLogger;

import java.util.logging.Logger;
import jcarbon.data.Data;
import jcarbon.data.Interval;
import jcarbon.service.Signal;

final class ProtoUtil {
  private static final Logger logger = getLogger();

  static <T extends Interval<? extends Iterable<? extends Data>>> Signal toProtoSignal(T interval) {
    Signal.Builder signal = Signal.newBuilder();
    signal
        .getStartBuilder()
        .setSecs(interval.start().getEpochSecond())
        .setNanos(interval.start().getNano());
    signal
        .getEndBuilder()
        .setSecs(interval.end().getEpochSecond())
        .setNanos(interval.end().getNano());
    signal.addComponent(interval.component().toString());
    for (Data data : interval.data()) {
      signal.addData(
          Signal.Data.newBuilder()
              .addComponent(data.component().toString())
              .setUnit(data.unit().toString())
              .setValue(data.value()));
    }
    return signal.build();
  }

  private ProtoUtil() {}
}
