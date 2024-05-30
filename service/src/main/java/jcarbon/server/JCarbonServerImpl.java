package jcarbon.server;

import static java.nio.file.Files.newOutputStream;
import static java.util.stream.Collectors.toList;
import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.stub.StreamObserver;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jcarbon.JCarbon;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Data;
import jcarbon.data.Interval;
import jcarbon.data.Unit;
import jcarbon.emissions.Emissions;
import jcarbon.emissions.JoulesEmissionsConverter;
import jcarbon.emissions.LocaleEmissionsConverters;
import jcarbon.service.DumpRequest;
import jcarbon.service.DumpResponse;
import jcarbon.service.JCarbonReport;
import jcarbon.service.JCarbonServiceGrpc;
import jcarbon.service.JCarbonSignal;
import jcarbon.service.PurgeRequest;
import jcarbon.service.PurgeResponse;
import jcarbon.service.ReadRequest;
import jcarbon.service.ReadResponse;
import jcarbon.service.Signal;
import jcarbon.service.StartRequest;
import jcarbon.service.StartResponse;
import jcarbon.service.StopRequest;
import jcarbon.service.StopResponse;
import jcarbon.util.SamplingFuture;

final class JCarbonServerImpl extends JCarbonServiceGrpc.JCarbonServiceImplBase {
  private static final Logger logger = getLogger();
  private static final JoulesEmissionsConverter converter =
      LocaleEmissionsConverters.forDefaultLocale();

  private final Optional<JCarbonServiceGrpc.JCarbonServiceBlockingStub> nvmlClient;

  private final HashMap<Long, JCarbon> jcarbons = new HashMap<>();
  private final HashMap<Long, JCarbonReport> data = new HashMap<>();
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-monotonic-time-sampling-thread");
            t.setDaemon(true);
            return t;
          });

  private SamplingFuture<List<Object>> monotonicTimeFuture;

  public JCarbonServerImpl(Optional<JCarbonServiceGrpc.JCarbonServiceBlockingStub> nvmlClient) {
    this.nvmlClient = nvmlClient;
  }

  @Override
  public void start(StartRequest request, StreamObserver<StartResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    if (!jcarbons.containsKey(processId)) {
      logger.info(String.format("creating jcarbon for %d", processId));
      JCarbon jcarbon = new JCarbon(request.getPeriodMillis(), processId);
      jcarbon.start();
      jcarbons.put(processId, jcarbon);
      nvmlClient.ifPresent(client -> client.start(request));
      final MonotonicTimestamp monotime = MonotonicTimestamp.getInstance();
      monotonicTimeFuture =
          SamplingFuture.fixedPeriodMillis(
              () -> List.of(Instant.now(), monotime.getMonotonicTimestamp()),
              request.getPeriodMillis(),
              executor);
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
      nvmlClient.ifPresent(client -> client.stop(request));
      jcarbon.JCarbonReport report = jcarbon.stop().get();

      JCarbonReport.Builder reportBuilder = toProtoReport(report).toBuilder();
      if (nvmlClient.isPresent()) {
        JCarbonReport nvmlReport =
            nvmlClient
                .map(client -> client.read(ReadRequest.getDefaultInstance()))
                .get()
                .getReport();
        reportBuilder.addAllSignal(nvmlReport.getSignalList());
        logger.info(
            String.format(
                "adding signal classes %s to report for %d",
                nvmlReport.getSignalList().stream().map(s -> s.getSignalName()).collect(toList()),
                processId));
        for (JCarbonSignal.Builder jcarbonSignal : reportBuilder.getSignalBuilderList()) {
          if (!jcarbonSignal.getSignalName().equals(Emissions.class.getName())) {
            continue;
          }
          jcarbonSignal.addAllSignal(
              nvmlReport.getSignalList().stream()
                  .flatMap(JCarbonServerImpl::convertNvmlSignals)
                  .collect(toList()));
        }
      }
      logger.info("adding signal jcarbon.server.MonotonicTimestamp");
      JCarbonSignal.Builder monotimeSignal =
          JCarbonSignal.newBuilder().setSignalName("jcarbon.server.MonotonicTimestamp");
      monotonicTimeFuture
          .get()
          .forEach(
              ts -> {
                Instant timestamp = (Instant) ts.get(0);
                Signal.Timestamp.Builder tsBuilder =
                    Signal.Timestamp.newBuilder()
                        .setSecs(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano());
                long monotime = (long) ts.get(1);
                monotimeSignal.addSignal(
                    Signal.newBuilder()
                        .setStart(tsBuilder)
                        .setEnd(tsBuilder)
                        .setComponent(LinuxComponents.OS_COMPONENT)
                        .setUnit(Unit.NANOSECONDS.name())
                        .addData(
                            Signal.Data.newBuilder()
                                .setComponent(LinuxComponents.OS_COMPONENT)
                                .setValue(monotime)));
              });
      reportBuilder.addSignal(monotimeSignal);
      // TODO: need to be able to combine/delete reports
      logger.info(String.format("storing jcarbon report for %d", processId));
      data.put(processId, reportBuilder.build());
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
      JCarbonReport report =
          JCarbonReport.newBuilder()
              .addAllSignal(
                  data.get(processId).getSignalList().stream()
                      .filter(signal -> request.getSignalsList().contains(signal.getSignalName()))
                      .collect(toList()))
              .build();
      String outputPath = request.getOutputPath();
      logger.info(String.format("dumping jcarbon report for %d at %s", processId, outputPath));
      try (OutputStream writer = newOutputStream(Path.of(outputPath)); ) {
        report.writeTo(writer);
      } catch (Exception e) {
        e.printStackTrace();
      }
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
      response.setReport(
          JCarbonReport.newBuilder()
              .addAllSignal(
                  data.get(processId).getSignalList().stream()
                      .filter(signal -> request.getSignalsList().contains(signal.getSignalName()))
                      .collect(toList())));
    } else {
      logger.info(
          String.format(
              "ignoring request to read jcarbon report for %d since it does not exist", processId));
    }
    resultObserver.onNext(response.build());
    resultObserver.onCompleted();
  }

  @Override
  public void purge(PurgeRequest request, StreamObserver<PurgeResponse> resultObserver) {
    logger.info(String.format("purging jcarbon"));

    jcarbons.forEach((pid, jcarbon) -> jcarbon.stop());
    jcarbons.clear();
    data.clear();
    nvmlClient.ifPresent(client -> client.stop(StopRequest.getDefaultInstance()));

    resultObserver.onNext(PurgeResponse.getDefaultInstance());
    resultObserver.onCompleted();
  }

  private static Stream<Signal> convertNvmlSignals(JCarbonSignal signal) {
    return signal.getSignalList().stream().map(s -> convertJoulesSignal(s, signal.getSignalName()));
  }

  private static Signal convertJoulesSignal(Signal signal, String signalName) {
    return signal.toBuilder()
        .setUnit(Unit.GRAMS_OF_CO2.name())
        .setComponent(signalName)
        .clearData()
        .addAllData(
            signal.getDataList().stream()
                .map(
                    data ->
                        data.toBuilder().setValue(converter.convertJoules(data.getValue())).build())
                .collect(toList()))
        .build();
  }

  private static <T extends Interval<? extends Iterable<? extends Data>>>
      JCarbonReport toProtoReport(jcarbon.JCarbonReport report) {
    JCarbonReport.Builder reportBuilder = JCarbonReport.newBuilder();
    for (Class<?> signal : report.getSignalTypes()) {
      logger.info(String.format("converting signal %s", signal));
      JCarbonSignal.Builder signalBuilder =
          JCarbonSignal.newBuilder().setSignalName(signal.getName());
      for (Object signalData : report.getSignal(signal)) {
        signalBuilder.addSignal(
            toProtoSignal((Interval<? extends Iterable<? extends Data>>) signalData));
      }
      reportBuilder.addSignal(signalBuilder);
    }
    return reportBuilder.build();
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
