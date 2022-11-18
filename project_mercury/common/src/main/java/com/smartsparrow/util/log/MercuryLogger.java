package com.smartsparrow.util.log;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Signal;

/**
 * A logger that enables traceability in reactive streams keeping the functionalities of the {@link org.slf4j.Logger}
 * interface. For the traceability to be enabled the subscriberContext must be set before the stream is consumed,
 * for example:<br/>
 * <pre>subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT, clientId.toString()))</pre>
 * For more examples on usage see the tests for {@link ReactiveMdc}
 */
public interface MercuryLogger extends Logger {

    /**
     * INFO level log that accepts a map of fields to include in the json log
     *
     * @param message the log message
     * @param fields the fields to include in the json log
     */
    void jsonInfo(final String message, final Map<String, Object> fields);

    /**
     * DEBUG level log that accepts a map of fields to include in the json log
     *
     * @param message the log message
     * @param fields the fields to include in the json log
     */
    void jsonDebug(final String message, final Map<String, Object> fields);

    /**
     * WARN level log that accepts a map of fields to include in the json log
     *
     * @param message the log message
     * @param fields the fields to include in the json log
     */
    void jsonWarn(final String message, final Map<String, Object> fields);

    /**
     * ERROR level log that accepts a map of fields and a throwable to include in the json log
     *
     * @param message the log message
     * @param fields the fields to include in the json log
     * @param throwable the throwable to include in the json log
     */
    void jsonError(final String message, final Map<String, Object> fields, Throwable throwable);

    //
    // Reactive log methods below
    //

    /**
     * INFO level log that executes on each signal
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveInfo(final String message);

    /**
     * INFO level log that executes on each signal and includes the signal value
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveInfoSignal(final String message);

    /**
     * INFO level log that executes on each signal, includes the signal value and accepts a function that returns a map
     * of fields to include in the json log
     *
     * @param message the log message
     * @param function a function that returns the map of fields
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveInfoSignal(final String message, final Function<T, Map<String, Object>> function);

    /**
     * INFO level log that executes on completion signal
     *
     * @param message the log messasge
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveInfoComplete(final String message);

    /**
     * DEBUG level log that executes on each signal
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveDebug(final String message);

    /**
     * DEBUG level log that executes on each signal and include the signal value
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveDebugSignal(final String message);

    /**
     * DEBUG level log that executes on each signal, include the signal value and accepts a function that returns a map
     * of fields to include in the json log
     *
     * @param message the log message
     * @param function a function that returns the map of fields
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveDebugSignal(final String message, final Function<T, Map<String, Object>> function);

    /**
     * WARN level log that executes on each signal
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveWarn(final String message);

    /**
     * WARN level log that executes on each signal and include the signal value
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveWarnSignal(final String message);

    /**
     * WARN level log that executes on each signal, includes the signal value and accepts a function that returns a map
     * of fields to include in the json log
     *
     * @param message the log message
     * @param function a function that returns the map of fields
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveWarnSignal(final String message, final Function<T, Map<String, Object>> function);

    /**
     * ERROR level log that executes on each error signal
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveError(final String message);

    /**
     * ERROR level log that executes on each error signal and includes the throwable in the json log
     *
     * @param message the log message
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    <T> Consumer<? super Signal<T>> reactiveErrorThrowable(final String message);

    /**
     * ERROR level log that executes on each error signal, includes the throwable in the json log and accepts a function
     * that returns a map of fields to include in the json log
     *
     * @param <T> the signal type
     * @param message the log message
     * @param booleanFunction a function that returns the boolean
     * @return a generic signal consume
     */
    <T> Consumer<? super Signal<T>> reactiveErrorThrowableIf(String message, final Function<Throwable, Boolean> booleanFunction);


    /**
     * ERROR level log that executes on each error signal, includes the throwable in the json log and accepts a function
     * that returns a map of fields to include in the json log
     *
     * @param message the log message
     * @param function a function that returns the map of fields
     * @param <T> the signal type
     * @return a generic signal consume
     */
    <T> Consumer<? super Signal<T>> reactiveErrorThrowable(final String message, final Function<Throwable, Map<String, Object>> function);

    /**
     * ERROR level log that executes on each error signal, includes the throwable in the json log and accepts a function
     * that returns a map of fields to include in the json log
     *
     * @param <T> the signal type
     * @param message the log message
     * @param booleanFunction a function that returns the boolean
     * @param function a function that returns the map of fields
     * @return a generic signal consume
     */
    <T> Consumer<? super Signal<T>> reactiveErrorThrowableIf(String message, final Function<Throwable, Boolean> booleanFunction,
                                                             final Function<Throwable, Map<String, Object>> function);


    @VisibleForTesting
    void addAppender(ListAppender<ILoggingEvent> newAppender);
}
