package com.smartsparrow.rtm.util;

import java.util.Arrays;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.smartsparrow.exception.ErrorResponseType;
import com.smartsparrow.exception.Fault;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.ResponseMessageType;
import com.smartsparrow.rtm.message.send.ErrorMessage;

import reactor.core.Exceptions;

public class Responses {

    private static final Logger log = LoggerFactory.getLogger(Responses.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // do not include null values.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // do not include empty collections
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    /**
     * Using the mapper configuration, write this object as a string.
     *
     * @param value the value to operate on
     * @return the object serialized as a JSON String
     * @throws JsonProcessingException raised by the underlying library as necessary
     */
    public static String valueAsString(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    /**
     * Write the supplied ResponseMessages using an async mechanism. Blocks until all messages have been written.
     *
     * @param session  the websocket session
     * @param messages the messages to convert to JSON and write
     * @throws WriteResponseException when the future fails/is interrupted (see cause)
     */
    public static void write(Session session, ResponseMessageType... messages) throws
            WriteResponseException {
        Preconditions.checkArgument(session != null , "invalid session (is null)");
        Preconditions.checkArgument(messages != null, "supplied messages can not be null");
        Preconditions.checkArgument(messages.length != 0, "supplied messages can not be empty");

        // Skip trying to write to socket if its closed and bail
        if(!session.isOpen()) {
            Arrays.stream(messages)
                    .forEach(message -> log.debug("Discarded message due to socket closed: " + message.toString()));

            throw new WriteResponseException("Unable to write, socket is not in OPEN state", new WebSocketException());
        }

         Arrays.stream(messages).parallel() //
                .forEach(message -> {
                    String messageJSON = null;
                    try {
                        messageJSON = valueAsString(message);
                    } catch (JsonProcessingException e) {
                        // FIXME: handle this case better.
                        throw new RuntimeException(e);
                    }
                    // FIXME: improve error handling, perhaps use the callback method.
                    try {
                        session.getRemote().sendStringByFuture(messageJSON);
                    } catch (WebSocketException | NullPointerException ex) {
                        // Race conditions might cause the socket to be in CLOSING state despite the .isOpen() check
                        log.info("Caught and discarded exception while writing to websocket:", ex);
                    }
                }); //

    }

    /**
     * Write the supplied ResponseMessages using an async mechanism. Blocks until all messages have been written.
     * Catches and propagates WriteResponseException.
     *
     * @param session  the websocket session
     * @param messages the messages to convert to JSON and write
     */
    public static void writeReactive(Session session, ResponseMessageType... messages) {
        try {
            write(session, messages);
        } catch (WriteResponseException e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Emit an error, using a String.format(...) formatted string.
     *
     * @param session        the session to write to
     * @param replyTo        the message id to use as the replyTo field
     * @param msgType        the message type
     * @param code           the error code
     * @param errorMsgFmt    the error message, in String.format(...) style
     * @param errorMsgParams the parameters to use in the format.
     */
    public static void error(final Session session,
                             final String replyTo,
                             final String msgType,
                             final int code,
                             final String errorMsgFmt,
                             final Object... errorMsgParams) throws WriteResponseException {
        ErrorMessage e = new ErrorMessage(msgType);
        e.setCode(code);
        e.setMessage(String.format(errorMsgFmt, errorMsgParams));
        e.setReplyTo(replyTo);

        write(session, e);
    }

    /**
     * Emit an error, using a String.format(...) formatted string.
     *
     * @param session        the session to write to
     * @param replyTo        the message id to use as the replyTo field
     * @param msgType        the message type
     * @param code           the error code
     * @param errorMsgFmt    the error message, in String.format(...) style
     * @param context        a map containing error contextual information
     * @param errorMsgParams the parameters to use in the format.
     */
    public static void error(final Session session,
                             final String replyTo,
                             final String msgType,
                             final int code,
                             final String errorMsgFmt,
                             final Map<String, Object> context,
                             final Object... errorMsgParams) throws WriteResponseException {
        ErrorMessage e = new ErrorMessage(msgType);
        e.setCode(code);
        e.setMessage(String.format(errorMsgFmt, errorMsgParams));
        e.setReplyTo(replyTo);
        e.setContext(context);
        write(session, e);
    }

    /**
     * Emit an error, using a String.format(...) formatted string. Catches and propagates WriteResponseException.
     *
     * @param session        the session to write to
     * @param replyTo        the message id to use as the replyTo field
     * @param msgType        the message type
     * @param code           the error code
     * @param errorMsgFmt    the error message, in String.format(...) style
     * @param errorMsgParams the parameters to use in the format.
     */
    public static void errorReactive(final Session session,
                                     final String replyTo,
                                     final String msgType,
                                     final int code,
                                     final String errorMsgFmt,
                                     final Object... errorMsgParams) {
        try {
            error(session, replyTo, msgType, code, errorMsgFmt, errorMsgParams);
        } catch (WriteResponseException e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Emit an error, using a String.format(...) formatted string. Catches and propagates WriteResponseException.
     *
     * @param session        the session to write to
     * @param replyTo        the message id to use as the replyTo field
     * @param msgType        the message type
     * @param code           the error code
     * @param errorMsgFmt    the error message, in String.format(...) style
     * @param context        a map containing error contextual information
     * @param errorMsgParams the parameters to use in the format.
     */
    public static void errorReactive(final Session session,
                                     final String replyTo,
                                     final String msgType,
                                     final int code,
                                     final String errorMsgFmt,
                                     final Map<String, Object> context,
                                     final Object... errorMsgParams) {
        try {
            error(session, replyTo, msgType, code, errorMsgFmt, context, errorMsgParams);
        } catch (WriteResponseException e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Emit an error to user. If {@param throwable} is {@link Fault} it emits error message from Fault,
     * if {@param throwable} is {@link Exception} - emits general error and logs the stacktrace.
     * <p/>
     * This method can be used as a fault barrier in RTM handlers.
     *
     * @param session   the session to write to
     * @param replyTo   the message id to use as the replyTo field
     * @param msgType   the message type
     * @param throwable the exception to emit to user
     */
    public static void errorReactive(final Session session,
                                     final String replyTo,
                                     final String msgType,
                                     Throwable throwable) {
        Throwable unwrapped = Exceptions.unwrap(throwable);
        if (unwrapped instanceof Fault) {
            errorReactive(session, replyTo, msgType, ((ErrorResponseType) unwrapped).getResponseStatusCode(),
                    unwrapped.getMessage());
        } else {
            String errorMessage = "unhandled error occurred to message processing";
            log.error(errorMessage, unwrapped);
            errorReactive(session, replyTo, msgType, HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);
        }
    }
}
