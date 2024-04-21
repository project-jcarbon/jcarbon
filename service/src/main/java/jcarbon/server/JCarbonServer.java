package jcarbon.server;

import static jcarbon.server.LoggerUtil.getLogger;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

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

    private ServerArgs(int port) {
      this.port = port;
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
    Options options = new Options().addOption(portOption);
    CommandLine cmd = new DefaultParser().parse(options, args);
    return new ServerArgs(cmd.getParsedOptionValue(portOption, DEFAULT_PORT).intValue());
  }

  /** Spins up the server. */
  public static void main(String[] args) throws Exception {
    ServerArgs serverArgs = getServerArgs(args);

    logger.info(String.format("starting new jcarbon server at localhost:%d", serverArgs.port));
    JCarbonServer server = new JCarbonServer(serverArgs.port);
    server.start();
    server.blockUntilShutdown();
    logger.info(String.format("terminating jcarbon server at localhost:%d", serverArgs.port));
  }
}
