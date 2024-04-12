package jcarbon.server;

import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.logging.Logger;
import jcarbon.JCarbon;
import jcarbon.JCarbonReport;
import jcarbon.service.DumpRequest;
import jcarbon.service.DumpResponse;
import jcarbon.service.JCarbonServiceGrpc;
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
    long processId = request.getProcessId();
    if (!jcarbons.containsKey(processId)) {
      logger.info(String.format("creating jcarbon for %d", processId));
      JCarbon jcarbon = new JCarbon(request.getPeriodMillis(), processId);
      jcarbon.start();
      jcarbons.put(Long.valueOf(processId), jcarbon);
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
    long processId = request.getProcessId();
    if (jcarbons.containsKey(processId)) {
      logger.info(String.format("stopping jcarbon for %d", processId));
      JCarbon jcarbon = jcarbons.get(Long.valueOf(processId));
      jcarbons.remove(Long.valueOf(processId));
      JCarbonReport report = jcarbon.stop().get();
      // TODO: need to be able to combine/delete reports
      logger.info(String.format("storing jcarbon report for %d", processId));
      data.put(Long.valueOf(processId), report);
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
    long processId = request.getProcessId();
    if (data.containsKey(processId)) {
      String outputPath = request.getOutputPath();
      logger.info(String.format("dumping jcarbon report for %d at %s", processId, outputPath));
      JsonUtil.dump(outputPath, data.get(Long.valueOf(processId)));
    } else {
      logger.info(
          String.format(
              "ignoring request to dump jcarbon report for %d since it does not exist", processId));
    }
    resultObserver.onNext(DumpResponse.getDefaultInstance());
    resultObserver.onCompleted();
  }
}
