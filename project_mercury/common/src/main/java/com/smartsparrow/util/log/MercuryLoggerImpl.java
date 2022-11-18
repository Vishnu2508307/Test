package com.smartsparrow.util.log;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.google.common.io.Closer;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.data.LogMessage;
import com.smartsparrow.util.log.lang.MercuryLoggerCloseableException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

class MercuryLoggerImpl implements MercuryLogger, Serializable {

    private final Logger logger;
    private static final Logger log = LoggerFactory.getLogger(MercuryLoggerImpl.class);

    public MercuryLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void jsonInfo(String message, Map<String, Object> fields) {
        logger.info(buildLogMessage(message, fields));
    }

    @Override
    public void jsonDebug(String message, Map<String, Object> fields) {
        if (logger.isDebugEnabled()) {
            logger.debug(buildLogMessage(message, fields));
        }
    }

    @Override
    public void jsonWarn(String message, Map<String, Object> fields) {
        logger.warn(buildLogMessage(message, fields));
    }

    @Override
    public void jsonError(String message, Map<String, Object> fields, Throwable throwable) {
        logger.error(Json.stringify(new LogMessage()
                .setFields(fields)
                .setField("content", message)
                .setField("throwable", throwable.getStackTrace())
        ));
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(Json.stringify(new LogMessage()
                .setField("content", message)
                .setField("throwable", throwable.getStackTrace())
        ));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveInfo(String message) {
        return onNextLog(ignored -> logger.info(buildLogMessage(message)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveInfoSignal(String message) {
        return onNextLog(value -> logger.info(buildLogMessage(message, value)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveInfoSignal(String message, Function<T, Map<String, Object>> function) {
        return onNextLog(value -> logger.info(buildLogMessage(message, function, value)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveInfoComplete(String message) {
        return onCompleteLog(() -> logger.info(buildLogMessage(message)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveDebug(String message) {
        if (logger.isDebugEnabled()) {
            return onNextLog(value -> logger.debug(buildLogMessage(message)));
        }
        return (Consumer<Signal<T>>) tSignal -> {
            // do nothing
        };
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveDebugSignal(String message) {
        if (logger.isDebugEnabled()) {
            return onNextLog(value -> logger.debug(buildLogMessage(message, value)));
        }
        return (Consumer<Signal<T>>) tSignal -> {
            // do nothing
        };
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveDebugSignal(String message, Function<T, Map<String, Object>> function) {
        if (logger.isDebugEnabled()) {
            return onNextLog(value -> logger.debug(buildLogMessage(message, function, value)));
        }
        return (Consumer<Signal<T>>) tSignal -> {
            // do nothing
        };
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveWarn(String message) {
        return onNextLog(ignored -> logger.warn(buildLogMessage(message)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveWarnSignal(String message) {
        return onNextLog(value -> logger.warn(buildLogMessage(message, value)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveWarnSignal(String message, Function<T, Map<String, Object>> function) {
        return onNextLog(value -> logger.warn(buildLogMessage(message, function, value)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveError(String message) {
        return onErrorLog(ignored -> logger.error(buildLogMessage(message)));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveErrorThrowable(String message) {
        return onErrorLog(throwable -> logger.error(Json.stringify(new LogMessage()
                .setField("content", message)
                .setField("throwable", throwable))));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveErrorThrowableIf(String message, Function<Throwable, Boolean> booleanFunction) {
        return onErrorLog(throwable -> {
            if(booleanFunction.apply(throwable)) {
                logger.error(Json.stringify(new LogMessage()
                        .setField("content", message)
                        .setField("throwable", throwable)));
            }
        });
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveErrorThrowable(String message, Function<Throwable, Map<String, Object>> function) {
        return onErrorLog(throwable -> logger.error(Json.stringify(new LogMessage()
                .setFields(function.apply(throwable))
                .setField("content", message)
                .setField("throwable", throwable))));
    }

    @Override
    public <T> Consumer<? super Signal<T>> reactiveErrorThrowableIf(String message, Function<Throwable, Boolean> booleanFunction,
                                                                    Function<Throwable, Map<String, Object>> function) {
        return onErrorLog(throwable -> {
            if(booleanFunction.apply(throwable)) {
                logger.error(Json.stringify(new LogMessage()
                        .setFields(function.apply(throwable)).
                                setField("content", message).
                                setField("throwable", throwable)));
            }
        });
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }

    /**
     * For each onNext the Signal context is accessed and used to put each {@link ReactiveMdc.Property} found to the
     * MDC via closeable. The consumer will accept the signal at each onNext
     *
     * @param consumer a consumer that will accept the signal
     * @return a generic signal consumer
     */
    private <T> Consumer<? super Signal<T>> onNextLog(final Consumer<T> consumer) {
        return signal -> {
            if (signal.isOnNext()) {
                Closer closer = registerCloseable(addMdc(signal.getContext()));
                consumer.accept(signal.get());
                try {
                    closer.close();
                } catch (IOException e) {
                    throw new MercuryLoggerCloseableException(e);
                }
            }
        };
    }

    /**
     * For each onError the Signal context is accessed and used to put each {@link ReactiveMdc.Property} found to the
     * MDC via closeable. The consumer will only accept the signal at onError
     *
     * @param consumer a consumer that will accept the signal
     * @return a generic signal consumer
     */
    private <T> Consumer<? super Signal<T>> onErrorLog(final Consumer<Throwable> consumer) {
        return signal -> {
            if (signal.isOnError()) {
                Closer closer = registerCloseable(addMdc(signal.getContext()));
                try {
                    consumer.accept(signal.getThrowable());
                } catch (Exception e) {
                    // the mercury logger failed (most likely due to a deserialization error) log the error
                    log.error("Error occurred during reactive logging {}", e);
                    // recover the original throwable that was intended to be logged so that it is not swallowed
                    log.error("Recovered throwable {}", signal.getThrowable());
                }
                try {
                    closer.close();
                } catch (IOException e) {
                    throw new MercuryLoggerCloseableException(e);
                }
            }
        };
    }

    /**
     * For each onComplete the Signal context is accessed and used to put each {@link ReactiveMdc.Property} found to the
     * MDC via closeable. The runnable will then be invoked
     *
     * @param runnable the runnable to execute
     * @return a generic signal consumer
     */
    private <T> Consumer<? super Signal<T>> onCompleteLog(final Runnable runnable) {
        return signal -> {
            if (signal.isOnComplete()) {
                Closer closer = registerCloseable(addMdc(signal.getContext()));
                runnable.run();
                try {
                    closer.close();
                } catch (IOException e) {
                    throw new MercuryLoggerCloseableException(e);
                }
            }
        };
    }

    /**
     * Put to the MDC each reactive Mdc property found in the context, along with the corresponding value
     *
     * @param context the context to read the mdc properties from
     * @return a list of mdc closeables
     */
    private List<MDC.MDCCloseable> addMdc(final Context context) {
        return Arrays.stream(ReactiveMdc.Property.values())
                // filter all the properties that are not found in the context to avoid null values
                .filter(property -> context.hasKey(property.name()))
                // put each existing property value in Mdc with a closeable
                .map(property -> MDC.putCloseable(property.name(), context.get(property.name())))
                // collect those closeable to a list
                .collect(Collectors.toList());
    }

    /**
     * Create a Closer and register each MDC closeable to the closer
     *
     * @param closeables the list of mdc closeable to register
     * @return the created closer
     */
    private Closer registerCloseable(final List<MDC.MDCCloseable> closeables) {
        final Closer closer = Closer.create();
        closeables.forEach(closer::register);
        return closer;
    }

    /**
     * Build a log message deserialized to string
     *
     * @param message the message text
     * @return a json string representation of a log message
     */
    private String buildLogMessage(final String message) {
        return Json.stringify(new LogMessage().setField("content", message));
    }

    /**
     * Build a log message deserialized to string with the value object in a field named value
     *
     * @param message the message text
     * @param value the value obj
     * @param <T> the value type
     * @return a json string representation of a log message
     */
    private <T> String buildLogMessage(final String message, final T value) {
        return Json.stringify(new LogMessage()
                .setField("content", message)
                .setField("value", value));
    }

    /**
     * Build a log message deserialized to a string with custom fields from the signal value
     *
     * @param message the message text
     * @param function a function that returns a map of string -> object fields
     * @param value the signal value to get the fields from
     * @param <T> the type of value
     * @return a json string representation of a log message
     */
    private <T> String buildLogMessage(final String message, final Function<T, Map<String, Object>> function,
                                       final T value) {
        return Json.stringify(new LogMessage()
                .setFields(function.apply(value))
                .setField("content", message));
    }

    /**
     * Build a log message with a given Map and content message
     * @param message the message text
     * @param fields input Map to get the fields from
     * @param <T> the type of value
     * @return a json string representation of a log message
     */
    private <T> String buildLogMessage(final String message, final Map<String, Object> fields) {
        return Json.stringify(new LogMessage()
                .setFields(fields)
                .setField("content", message));
    }

    @Override
    public void addAppender(ListAppender<ILoggingEvent> newAppender) {
        ch.qos.logback.classic.Logger loggerClassic = (ch.qos.logback.classic.Logger) logger;
        loggerClassic.addAppender(newAppender);
    }
}
