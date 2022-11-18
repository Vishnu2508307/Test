package com.smartsparrow.rtm.ws;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.common.base.Strings;
import com.smartsparrow.rtm.lang.DeserializationException;
import com.smartsparrow.rtm.lang.InvalidMessageFormat;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.UnsupportedMessageType;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.ReceivedMessageDeserializer;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Tokens;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * The main RTM WebSocket broker. This broker processes the messages which arrive on the WebSocket and then handle
 * the offloading of the processing of the messages.
 *
 * This broker runs on a single server thread and should not be considered thread-safe and is stateful. It passes
 * the messages to message consumer thread workers in order to perform the processing.
 *
 * Restrictions that are applied to client connections:
 *  - Allow no more than 5 concurrent message processing; subsequent messages will be queued (blocks in the handler thread)
 *  - (TODO) If not authenticated, disconnect on any message which is not authentication related.
 *  - Submitting multiple binary messages should result in a disconnect.
 *  - (TODO) Submitting too many invalid messages should result in a disconnect.
 *  - (TODO) Better error handling if an operation fails, we should disconnect on an error?
 *
 */
@WebSocket
public class RTMWebSocketBroker extends WebSocketAdapter {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RTMWebSocketBroker.class);

    // the Executor to maintain the concurrency.
    private final RTMWebSocketExecutor rtmWebSocketExecutor;
    private final RTMWebSocketHandler rtmWebSocketHandler;
    private final ReceivedMessageDeserializer receivedMessageDeserializer;
    private final RTMWebSocketContext rtmWebSocketContext;

    private CountDownLatch countDownLatch = null;

    private final Object lock = new Object();

    @Inject
    RTMWebSocketBroker(final RTMWebSocketHandler rtmWebSocketHandler,
                       final RTMWebSocketExecutor rtmWebSocketExecutor,
                       final ReceivedMessageDeserializer receivedMessageDeserializer,
                       final RTMWebSocketContext rtmWebSocketContext) {
        this.rtmWebSocketHandler = rtmWebSocketHandler;
        this.rtmWebSocketExecutor = rtmWebSocketExecutor;
        this.receivedMessageDeserializer = receivedMessageDeserializer;
        this.rtmWebSocketContext = rtmWebSocketContext;
    }

    /**
     * A WebSocket {@link Session} has connected successfully and is ready to be used.
     *
     * Registers this {@link RTMWebSocketBroker} to the {@link RTMWebSocketManager} and initialise the connection.
     * Important note: the connection is closed immediately when the {@link RTMWebSocketManager} has triggered
     * a graceful shutdown.
     * @param session the WebSocket connection.
     */
    @Override
    public void onWebSocketConnect(Session session) {
        //
        // do not accept connection if the server is about to shutdown
        //
        if (RTMWebSocketManager.getInstance().isShuttingDown()) {
            onWebSocketClose(RTMWebSocketStatus.GOING_AWAY.getValue(), "shutdown");
            return;
        }

        super.onWebSocketConnect(session);

        //
        // register connection to the manager instance
        //
        RTMWebSocketManager.getInstance().register(this);

        //
        // no need to maintain a local copy of the session as our parent has it.
        //
        log.jsonDebug("connect from", new HashedMap<String, Object>(){
            {
                put("address", getSession().getRemoteAddress().getAddress());

            }
        });

        //
        // Initialises message broker contexts
        //
        String clientId = Tokens.generate();
        rtmWebSocketHandler.initialise(clientId, getSession(), rtmWebSocketContext);

        //
        // say hi!
        //
        BasicResponseMessage hello = new BasicResponseMessage("hello", null);
        hello.addField("clientId", clientId);
        try {
            Responses.write(getSession(), hello);
        } catch (WriteResponseException wre) {
            log.error("unable to write hello message, disconnecting.", wre);
            try {
                session.disconnect();
            } catch (IOException e) {
                log.error("error while performing hard disconnect.", e);
            }
        }
    }

    /**
     * Process an incoming text message on a WebSocket.
     *
     * This method wraps the {@link RTMWebSocketHandler#submit(ReceivedMessage)} in a {@link Runnable} and delegates
     * the message processing to the {@link RTMWebSocketExecutor}
     *
     * Important note: the message is ignored when the {@link RTMWebSocketManager} has triggered a graceful shutdown
     *
     * @param message the incoming message.
     */
    @Override
    public void onWebSocketText(String message) {
        // the super implementation does nothing.
        // super.onWebSocketText(message);
        log.jsonDebug("Message received from", new HashedMap<String, Object>() {
            {
                put("address", getSession().getRemoteAddress().getAddress());
                put("message", message);
            }
        });

        // do not process/accept the incoming message if the server is shutting down
        if (RTMWebSocketManager.getInstance().isShuttingDown()) {
            log.debug("About to close this connection");
            return;
        }

        synchronized (lock) {
            try {
                // try to deserialize the message
                ReceivedMessage receivedMessage = deserialize(message);

                log.jsonInfo("the request message", new HashedMap<String, Object>() {
                    {
                        put("id", receivedMessage.getId());
                        put("type", receivedMessage.getType());
                        put("mode", receivedMessage.getMode());
                    }
                });

                // read the mode message property
                ReceivedMessage.Mode processingMode = receivedMessage.getMode();

                switch (processingMode) {
                    case WAIT_PENDING:
                        // get the estimate number of pending tasks from the executor
                        final long pendingTasksCount = rtmWebSocketExecutor.getExecutor().getPendingTasksCount();

                        // if there are pending tasks then create the count down latch
                        // this is to avoid creating a count down latch every time because despite the pending task number
                        // count being 0 the count down latch will wait the waitFor timeout
                        if (pendingTasksCount > 0) {
                            // create the countdown latch with size = to the number of tasks
                            countDownLatch = new CountDownLatch(Math.toIntExact(pendingTasksCount));
                            // create a timer with the default timer constant
                            try {
                                countDownLatch.await(receivedMessage.getWaitFor(), TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                log.error("Interrupted exception. This might be cause by the client suddenly closing the session", e);
                            } finally {
                                // when the count down latch has released the thread set it back to null
                                countDownLatch = null;
                            }
                        }
                        break;
                    case DEFAULT:
                    default:
                        // nothing special happening here for now
                        break;
                }

                // finally process the message
                rtmWebSocketExecutor.execute(processRunnable(receivedMessage));

            } catch (DeserializationException e) {
                log.error("Exception while parsing the message", e);
                // respond with an error when deserialization failed
                ErrorMessage error = new ErrorMessage(e.getType())
                        .setReplyTo(e.getReplyTo())
                        .setCode(e.getStatusCode())
                        .setMessage(e.getErrorMessage());
                emitError(error);
            }
        }
    }

    /**
     * Process a binary message. At this time, this emits errors.
     *
     * @param payload ignored
     * @param offset ignored
     * @param len ignored
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // the super implementation does nothing.
        // super.onWebSocketBinary(payload, offset, len);

        ErrorMessage error = new ErrorMessage().setMessage("Unsupported message type").setCode(HttpStatus.SC_BAD_REQUEST);
        emitError(error);
    }

    /**
     * A Close Event was received.
     *
     * The underlying Connection will be considered closed at this point.
     *
     * @param statusCode the status code
     * @param reason the reason it was closed.
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason) {

        // Invoke cleanup on handler to clean up RTMScoped managers
        rtmWebSocketHandler.cleanup();

        //
        // remove connection from the manager instance
        //
        RTMWebSocketManager.getInstance().deregister(this);

        super.onWebSocketClose(statusCode, reason);

        log.jsonDebug("websocket closed", new HashedMap<String, Object>() {
            {
                put("statusCode", statusCode);
                put("reason", Strings.nullToEmpty(reason));
            }
        });

        // shutdown any running websocket tasks.
        rtmWebSocketExecutor.shutdownWebsocketExecutor(2, TimeUnit.SECONDS);

    }

    /**
     * Handle an Exception which occurred on the WebSocket
     *
     * This is a way for the internal implementation to notify of exceptions occurred during the processing of WebSocket.
     * Usually this occurs from bad / malformed incoming packets.
     * (example: bad UTF8 data, frames that are too big, violations of the spec)
     *
     * This will result in the {@link Session} being closed by the implementing side.
     *
     * @param cause the throwable stack containing the error.
     */
    @Override
    public void onWebSocketError(Throwable cause) {
        // the super implementation does nothing.
        // super.onWebSocketError(cause);

        ErrorMessage error = new ErrorMessage().setMessage("Error while processing request body, socket connection could be discarded.").setCode(HttpStatus.SC_BAD_REQUEST);
        emitError(error);

        log.error("WebSocket Error", cause);
    }

    /**
     * Initiate a graceful shutdown of the connection.
     *
     * First sends a message to the client that the server is about to shutdown. At this stage the client should not
     * send any message, however the shutdown state is set to true so that any eventual incoming message is ignored.
     * The executor service is then gracefully shutdown and when that's done
     * the {@link RTMWebSocketBroker#onWebSocketClose(int, String)} method is called and the connection is closed.
     *
     * @param timeout the timeout value passed to the executor
     * @param timeUnit the timeUnit value passed to the executor
     */
    void gracefulShutdown(long timeout, TimeUnit timeUnit) {
        //
        // should write a message to the client that the server is shutting down
        //
        BasicResponseMessage shutdownMessage = new BasicResponseMessage("shutdown", null);
        shutdownMessage.addField("message", "Any new incoming message will be ignored");

        try {
            Responses.write(getSession(), shutdownMessage);
        } catch (WriteResponseException e) {
            log.error("Error writing the shutdown message", e);
        }

        //
        // should shutdown gracefully the executor service
        //
        try {
            rtmWebSocketExecutor.gracefulShutdown(timeout, timeUnit);
        } catch (InterruptedException e) {
            log.error("Error stopping executor service", e);
        }

        //
        // on executorService stopped should close connection
        //
        String reason = String.format("Shutting down. Closing session for %s", getSession().getRemoteAddress().getAddress());
        getSession().close(RTMWebSocketStatus.GOING_AWAY.getValue(), reason);
    }

    /**
     * Wraps the message processing in a runnable to be executed by the ExecutorService
     *
     * @param message the message to process.
     */
    private Runnable processRunnable(ReceivedMessage message) {
        return () -> {
            try {
                rtmWebSocketHandler.submit(message);
            } catch (RTMWebSocketHandlerException e) {
                ErrorMessage error = new ErrorMessage(e.getType())
                        .setReplyTo(e.getReplyTo())
                        .setCode(e.getStatusCode())
                        .setMessage(e.getErrorMessage());
                emitError(error);
            } finally {
                // if the count down latch is active then count down!
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }
        };
    }

    /**
     * Helper to emit an error message.
     *
     * @param errorMessage the error message.
     */
    void emitError(ErrorMessage errorMessage) {
        try {
            Responses.write(getSession(), errorMessage);
        } catch (WriteResponseException wre) {
            log.error("unable to write error message", wre);
        }
    }

    /**
     * Deserialize and validate the incoming message.
     *
     * @param message a raw message from the socket
     */
    private ReceivedMessage deserialize(String message) throws DeserializationException {
        try {
            return receivedMessageDeserializer.deserialize(message);
        } catch (UnsupportedMessageType umt) {
            // log.warn on this to see if something is really broken.
            log.warn("Invalid message, could not be parsed");
            throw new DeserializationException(umt.getMessage(), umt.getReplyTo(), HttpStatus.SC_INTERNAL_SERVER_ERROR, null);

        } catch (IOException ioe) {
            // be nice and return the parsing exception.
            String errorStr = String.format("Invalid message, could not be parsed: %s", ioe.getMessage());
            // no server/verbose log needed, this is a problem in the client's message.
            log.warn("Invalid message, could not be parsed");

            throw new DeserializationException(errorStr, null, HttpStatus.SC_INTERNAL_SERVER_ERROR, null);

        } catch (InvalidMessageFormat e) {
            // no server/verbose log needed, this is a problem in the client's message.
            log.warn("Invalid message format, could not be parsed");
            throw new DeserializationException(e.getMessage(), e.getReplyTo(), HttpStatus.SC_BAD_REQUEST, e.getMessageType());

        } catch (IllegalArgumentException e) {
            // no server/verbose log needed, this is a problem in the client's message.
            log.warn("Invalid message, could not be parsed");
            throw new DeserializationException("Invalid message, could not be parsed.", null, HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
        }
    }
}
