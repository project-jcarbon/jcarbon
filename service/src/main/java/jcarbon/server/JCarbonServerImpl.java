package jcarbon.server;

import static java.nio.file.Files.newOutputStream;
import static java.util.stream.Collectors.toList;
import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.stub.StreamObserver;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonApplicationMonitor;
import jcarbon.service.DumpRequest;
import jcarbon.service.DumpResponse;
import jcarbon.service.JCarbonServiceGrpc;
import jcarbon.service.PurgeRequest;
import jcarbon.service.PurgeResponse;
import jcarbon.service.ReadRequest;
import jcarbon.service.ReadResponse;
import jcarbon.service.StartRequest;
import jcarbon.service.StartResponse;
import jcarbon.service.StopRequest;
import jcarbon.service.StopResponse;
import jcarbon.signal.Component;
import jcarbon.signal.Report;

final class JCarbonServerImpl extends JCarbonServiceGrpc.JCarbonServiceImplBase {
  private static final Logger logger = getLogger();

  private final Optional<JCarbonServiceGrpc.JCarbonServiceBlockingStub> nvmlClient;

  private final HashMap<Long, JCarbon> jcarbons = new HashMap<>();
  private final HashMap<Long, Report> data = new HashMap<>();
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-monotonic-time-sampling-thread");
            t.setDaemon(true);
            return t;
          });

  public JCarbonServerImpl(Optional<JCarbonServiceGrpc.JCarbonServiceBlockingStub> nvmlClient) {
    this.nvmlClient = nvmlClient;
  }

  @Override
  public void start(StartRequest request, StreamObserver<StartResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    if (!jcarbons.containsKey(processId)) {
      logger.info(String.format("creating jcarbon for %d", processId));
      JCarbon jcarbon = new JCarbonApplicationMonitor(request.getPeriodMillis(), processId);
      jcarbon.start();
      jcarbons.put(processId, jcarbon);
      nvmlClient.ifPresent(client -> client.start(request));
      resultObserver.onNext(StartResponse.getDefaultInstance());
    } else {
      String message =
          String.format(
              "ignoring request to create jcarbon for %d since it already exists", processId);
      logger.info(message);
      resultObserver.onNext(StartResponse.newBuilder().setResponse(message).build());
    }
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
      Report.Builder reportBuilder = jcarbon.stop().get().toBuilder();

      if (nvmlClient.isPresent()) {
        Report nvmlReport =
            nvmlClient
                .map(client -> client.read(ReadRequest.getDefaultInstance()))
                .get()
                .getReport();
        for (Component component : nvmlReport.getComponentList()) {
          Component.Builder componentBuilder = component.toBuilder();
          componentBuilder.addAllSignal(
              componentBuilder.getSignalList().stream()
                  .map(jcarbon::convertToEmissions)
                  .collect(toList()));
          logger.info(
              String.format(
                  "adding component %s:%s to report for %d",
                  component.getComponentType(), component.getComponentId(), processId));
          reportBuilder.addComponent(componentBuilder);
        }
      }
      // TODO: need to be able to combine/delete reports
      logger.info(String.format("storing jcarbon report for %d", processId));
      data.put(processId, reportBuilder.build());
    } else {
      String message =
          String.format(
              "ignoring request to stop jcarbon for %d since it does not exist", processId);
      logger.info(message);
      resultObserver.onNext(StopResponse.newBuilder().setResponse(message).build());
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
      try (OutputStream writer = newOutputStream(Path.of(outputPath))) {
        data.get(processId).writeTo(writer);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      String message =
          String.format(
              "ignoring request to dump jcarbon report for %d since it does not exist", processId);
      logger.info(message);
      resultObserver.onNext(DumpResponse.newBuilder().setResponse(message).build());
    }
    resultObserver.onNext(DumpResponse.getDefaultInstance());
    resultObserver.onCompleted();
  }

  @Override
  public void read(ReadRequest request, StreamObserver<ReadResponse> resultObserver) {
    Long processId = Long.valueOf(request.getProcessId());
    ReadResponse.Builder response = ReadResponse.newBuilder();
    logger.info(String.format("reading jcarbon report for %d", processId));
    if (data.containsKey(processId)) {
      response.setReport(data.get(processId));
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
}
