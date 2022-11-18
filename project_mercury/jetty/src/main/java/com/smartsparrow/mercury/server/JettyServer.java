package com.smartsparrow.mercury.server;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.data.InstanceType;
import com.smartsparrow.rtm.ws.RTMWebSocketManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class functions as a wrapper for {@link Server}. It should be initialised only via the
 * {@link JettyServerBuilder} class.
 */
public class JettyServer {

    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

    private int port;
    private Server server;
    private long startupTimeout;
    private final InstanceType instanceType;

    /**
     * Package private constructor to limit its access to the {@link JettyServerBuilder} class.
     *
     * @param port the port number on which the jetty server will run
     * @param server the jetty server object. Null value not permitted
     * @param startupTimeout milliseconds to wait for the server to startup
     */
    JettyServer(int port, @Nonnull Server server, long startupTimeout, InstanceType instanceType) {
        this.port = port;
        this.server = server;
        this.startupTimeout = startupTimeout;
        this.instanceType = instanceType;
    }

    /**
     * Starts the jetty server and does not wait for the server to be in a running state
     */
    public void start() {
        startupServer();
    }

    /**
     * Starts the jetty server in a new Thread
     */
    private void startupServer() {
        new Thread(()->{
            try {
                server.start();
                // do not dump the server if the logging has json layout because that messes up the output
                if (!"json".equals(System.getProperty("logging"))) {
                    String lineSeparator = System.getProperty("line.separator");
                    log.info("dump >>>{}{}{}<<< dump", lineSeparator, server.dump(), lineSeparator);
                    log.info("--------------------------------------------");
                }
                HandlerList handlerList = (HandlerList) server.getHandler();

                Handler[] handlers = handlerList.getHandlers();

                for (Handler handler : handlers) {
                    log.info(handler.toString());
                }

                log.info("--------------------------------------------");

                server.join();
            } catch (Throwable t) {
                throw new JettyServerBuilderException("Error starting jetty server", t);
            }
        }).start();
    }

    /**
     * Starts the jetty server and blocks waiting for the server to be in a started state
     */
    public void startNow() {
        startupServer();
        try {
            awaitServerStartup();
        } catch (InterruptedException e) {
            log.error("Error waiting for the server to startup", e);
        }
    }

    /**
     * Attempts to gracefully shutdown the jetty server by invoking the
     * {@link RTMWebSocketManager#closeAllConnections(long, TimeUnit)} method. Then it waits until either all the
     * connections are closed or the timeout expires whichever happen first.
     *
     * @throws Exception when the shutdown process fails
     */
    public void stop() throws Exception {
        String message = "Gracefully shutting down the jetty server with a {} seconds timeout for outstanding messages.";
        log.info(message, server.getStopTimeout() / 1000);

        RTMWebSocketManager rtmWebSocketManager = RTMWebSocketManager.getInstance();

        long timeout = server.getStopTimeout();

        rtmWebSocketManager.closeAllConnections(timeout, TimeUnit.MILLISECONDS);

        awaitTermination(timeout);

        // The following line will wait the stop timeout again to sort out all the outstanding rest requests
        server.stop();
    }

    public int getPort() {
        return port;
    }

    public URI getURI() {
        return server.getURI();
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    public long getStopTimeout() {
        return server.getStopTimeout();
    }

    public boolean isStopped() {
        return server.isStopped();
    }

    public InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * Exposes the server object.
     *
     * The {@link ServletContextHandler} is accessible via {@link Server#getHandler()}
     * @return {@link Server}
     */
    public Server getServer() {
        return server;
    }

    /**
     * Keeps waiting until either all the connections are closed or the timeout expires, whichever happens first
     * @param timeout the waiting time value expressed in milliseconds
     */
    private void awaitTermination(long timeout) throws InterruptedException {
        while (RTMWebSocketManager.getInstance().hasConnections() && timeout > 0) {
            Thread.sleep(1);
            timeout--;
        }
    }

    /**
     * Waits until the server is started or the startup timeout expires, whichever happen first
     */
    @SuppressFBWarnings (value = "DM_EXIT", justification = "Shut down the application if Jetty server can not start")
    private void awaitServerStartup() throws InterruptedException {
        long timeout = startupTimeout;
        while (!this.isStarted()) {
            Thread.sleep(1);
            timeout--;
            if (timeout == 0) {
                log.warn("The jetty server was not started after {} seconds", startupTimeout / 1000);
                System.exit(1);
            }
        }
    }
}
