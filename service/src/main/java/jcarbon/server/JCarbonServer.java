package jcarbon.server;

import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

final class JCarbonServer {
  private static final Logger logger = getLogger();

  private final int port;
  private final Server server;

  private JCarbonServer(int port) throws IOException {
    this.port = port;
    this.server =
        Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
            .addService(new JCarbonServerImpl())
            .build();
  }

  /** Start serving requests. */
  public void start() throws IOException {
    server.start();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
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

  private static final int DEFAULT_PORT = 8980;

  /** Spins up the server. */
  public static void main(String[] args) throws Exception {
    int port = DEFAULT_PORT;
    logger.info(String.format("starting new jcarbon server at localhost:%d", port));
    JCarbonServer server = new JCarbonServer(port);
    server.start();
    server.blockUntilShutdown();
    logger.info(String.format("terminating jcarbon server at localhost:%d", port));
  }
}
