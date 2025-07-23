package jcarbon.server;

import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import jcarbon.service.JCarbonServiceGrpc;
import jcarbon.service.StopRequest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

final class JCarbonServer {
  private static final Logger logger = getLogger();

  private final int port;
  private final Server server;

  private int MAX_MESSAGE_LENGTH = 20 * 1024 * 1024;

  private JCarbonServer(ServerArgs args) throws IOException {
    this.port = args.port;
    ServerBuilder serverBuilder =
        Grpc.newServerBuilderForPort(args.port, InsecureServerCredentials.create());
    if (args.useNvml) {
      try {
        JCarbonServiceGrpc.JCarbonServiceBlockingStub stub =
            JCarbonServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("localhost", 8981)
                .usePlaintext()
                .maxInboundMessageSize(MAX_MESSAGE_LENGTH)
                .build());
        stub.stop(StopRequest.getDefaultInstance());
        serverBuilder.addService(new JCarbonServerImpl(Optional.of(stub))).build();
      } catch (Exception e) {
        logger.info("could not connect to nvml server...ignoring it");
        serverBuilder.addService(new JCarbonServerImpl(Optional.empty()));
      }
    } else {
      serverBuilder.addService(new JCarbonServerImpl(Optional.empty()));
    }
    this.server = serverBuilder.build();
  }

  /** Start serving requests. */
  public void start() throws IOException {
    server.start();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread("jcarbon-shutdown") {
              @Override
              public void run() {
                // TODO: i locked up this logger to here. is that good enough?
                logger.info("shutting down jcarbon server since the JVM is shutting down");
                try {
                  JCarbonServer.this.stop();
                } catch (InterruptedException e) {
                  e.printStackTrace(System.err);
                }
                logger.info("server shutdown...");
              }
            });
  }

  /** Stop serving requests and shutdown resources. */
  public void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  private static class ServerArgs {
    private final int port;
    private final boolean useNvml;

    private ServerArgs(int port, boolean useNvml) {
      this.port = port;
      this.useNvml = useNvml;
    }
  }

  private static final Integer DEFAULT_PORT = Integer.valueOf(8980);

  private static ServerArgs getServerArgs(String[] args) throws Exception {
    Option portOption =
        Option.builder("p")
            .hasArg(true)
            .longOpt("port")
            .desc("port to host the server")
            .type(Integer.class)
            .build();
    Options options =
        new Options()
            .addOption(portOption)
            .addOption("nvml", false, "create a client to the nvml server");
    CommandLine cmd = new DefaultParser().parse(options, args);
    return new ServerArgs(
        cmd.getParsedOptionValue(portOption, DEFAULT_PORT).intValue(), cmd.hasOption("nvml"));
  }

  /** Spins up the server. */
  public static void main(String[] args) throws Exception {
    ServerArgs serverArgs = getServerArgs(args);

    logger.info(String.format("starting new jcarbon server at localhost:%d", serverArgs.port));
    if (serverArgs.useNvml) {
      logger.info(String.format("creating client to nvml server at localhost:8981"));
    }
    JCarbonServer server = new JCarbonServer(serverArgs);
    server.start();
    server.blockUntilShutdown();
    logger.info(String.format("terminating jcarbon server at localhost:%d", serverArgs.port));
  }
}
