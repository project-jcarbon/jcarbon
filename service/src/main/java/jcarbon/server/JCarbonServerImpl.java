package jcarbon.server;

import static jcarbon.JCarbonReporting.writeReport;
import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.stub.StreamObserver;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import jcarbon.data.Data;
import jcarbon.data.Interval;
import jcarbon.service.DumpRequest;
import jcarbon.service.DumpResponse;
import jcarbon.service.JCarbonServiceGrpc;
import jcarbon.service.JCarbonSignal;
import jcarbon.service.ReadRequest;
import jcarbon.service.ReadResponse;
import jcarbon.service.Signal;
import jcarbon.service.StartRequest;
import jcarbon.service.StartResponse;
import jcarbon.service.StopRequest;
import jcarbon.service.StopResponse;

final class JCarbonServerImpl extends JCarbonServiceGrpc.JCarbonServiceImplBase {
  private static final Logger logger = getLogger();

  private final HashMap<Long, JCarbon> jcarbons = new HashMap<>();
  private final HashMap<Long, JCarbonReport> data = new HashMap<>();

  @Override
  public void start(StartRequest request, StreamObserver<StartResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    if (!jcarbons.containsKey(processId)) {
      logger.info(String.format("creating jcarbon for %d", processId));
      JCarbon jcarbon = new JCarbon(request.getPeriodMillis(), processId);
      jcarbon.start();
      jcarbons.put(processId, jcarbon);
    } else {
      logger.info(
          String.format(
              "ignoring request to create jcarbon for %d since it already exists", processId));
    }
    resultObserver.onNext(StartResponse.getDefaultInstance());
    resultObserver.onCompleted();
  }

  @Override
  public void stop(StopRequest request, StreamObserver<StopResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    if (jcarbons.containsKey(processId)) {
      logger.info(String.format("stopping jcarbon for %d", processId));
      JCarbon jcarbon = jcarbons.get(processId);
      jcarbons.remove(processId);
      JCarbonReport report = jcarbon.stop().get();
      // TODO: need to be able to combine/delete reports
      logger.info(String.format("storing jcarbon report for %d", processId));
      data.put(processId, report);
    } else {
      logger.info(
          String.format(
              "ignoring request to stop jcarbon for %d since it does not exist", processId));
    }
    resultObserver.onNext(StopResponse.getDefaultInstance());
    resultObserver.onCompleted();
  }

  @Override
  public void dump(DumpRequest request, StreamObserver<DumpResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    if (data.containsKey(processId)) {
      String outputPath = request.getOutputPath();
      logger.info(String.format("dumping jcarbon report for %d at %s", processId, outputPath));
      writeReport(data.get(processId), Path.of(outputPath));
    } else {
      logger.info(
          String.format(
              "ignoring request to dump jcarbon report for %d since it does not exist", processId));
    }
    resultObserver.onNext(DumpResponse.getDefaultInstance());
    resultObserver.onCompleted();
  }

  // TODO: DO NOT USE THIS METHOD!!!! IT IS NOT FULLY IMPLEMENTED!
  @Override
  public void read(ReadRequest request, StreamObserver<ReadResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    ReadResponse.Builder response = ReadResponse.newBuilder();
    logger.info(String.format("reading jcarbon report for %d", processId));
    if (data.containsKey(processId)) {
      JCarbonReport report = data.get(processId);
      jcarbon.service.JCarbonReport.Builder reportBuilder =
          jcarbon.service.JCarbonReport.newBuilder();
      for (String signalName : request.getSignalsList()) {
        try {
          Class<?> signal = Class.forName(signalName);
          if (signal == null) {
            logger.info(
                String.format(
                    "ignoring request to read jcarbon report for %d since no signal class could be"
                        + " found for '%s'",
                    processId, signal));
          } else if (report.hasSignal(signal)) {
            logger.info(
                String.format("reading signal %s from jcarbon report for %d", signal, processId));
            JCarbonSignal.Builder signalBuilder =
                JCarbonSignal.newBuilder().setSignalName(signal.getName());
            for (Object signalData : report.getSignal(signal)) {
              signalBuilder.addSignal(
                  toProtoSignal((Interval<? extends Iterable<? extends Data>>) signalData));
            }
            reportBuilder.addSignal(signalBuilder);
          } else {
            logger.info(
                String.format(
                    "ignoring request to read jcarbon report for %d since it does not have a %s"
                        + " signal",
                    processId, signal));
          }
        } catch (ClassNotFoundException e) {
          logger.info(
              String.format(
                  "ignoring request to read jcarbon report for %d since no signal class could be"
                      + " found for '%s'",
                  processId, signalName));
        }
      }
      response.setReport(reportBuilder);
    } else {
      logger.info(
          String.format(
              "ignoring request to read jcarbon report for %d since it does not exist", processId));
    }
    resultObserver.onNext(response.build());
    resultObserver.onCompleted();
  }

  private static <T extends Interval<? extends Iterable<? extends Data>>> Signal toProtoSignal(
      T interval) {
    Signal.Builder signal =
        Signal.newBuilder()
            .setComponent(interval.component().toString())
            .setUnit(interval.unit().toString());
    signal
        .getStartBuilder()
        .setSecs(interval.start().getEpochSecond())
        .setNanos(interval.start().getNano());
    signal
        .getEndBuilder()
        .setSecs(interval.end().getEpochSecond())
        .setNanos(interval.end().getNano());
    for (Data data : interval.data()) {
      signal.addData(
          Signal.Data.newBuilder()
              .setComponent(data.component().toString())
              .setValue(data.value()));
    }
    return signal.build();
  }
}
