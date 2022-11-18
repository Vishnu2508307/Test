package com.smartsparrow.util.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.google.common.collect.Lists;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.UUIDs;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test class to ensure MDC traceability is available in reactive programming
 */
class ReactiveMdcTest {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ReactiveMdcTest.class);

    private final Mono<String> aTestingMono = Mono.just("keep it cool");
    private final Flux<Integer> aTestingFlux = Flux.just(1, 2, 3, 4, 5);
    private static final UUID clientId = UUID.randomUUID();
    private static final String traceId = UUIDs.timeBased().toString();

    @Test
    void withMdcTraceability_mono () {
        // prepare the list appender so that later on the log events can be verified
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingMono
                // log a simple message on each
                .doOnEach(logger.reactiveInfo("I am a message with mdc"))
                // log a simple message on each that includes the signal value
                .doOnEach(logger.reactiveInfoSignal("here is the message with mdc > "))
                // manipulate the signal value to print whichever value you like
                .doOnEach(logger.reactiveInfoSignal("here is the message with mdc > ", s -> new HashMap<String, Object>(){{put("value", s);}}))
                // define the mdc properties to the reactive context
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId.toString()))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            assertEquals(clientId.toString(), logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertEquals(traceId, logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });
    }

    @Test
    void noMdcTraceability_mono() {
        // prepare the list appender so that later on the log events can be verified
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingMono
                // log a simple message on each
                .doOnEach(logger.reactiveInfo("I am a message"))
                // log a simple message on each that includes the signal value
                .doOnEach(logger.reactiveInfoSignal("here is the message > "))
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            // none of the lines logged contain the request context traceability
            assertNull(logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertNull(logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });
    }

    @Test
    void withMdcTraceability_flux() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingFlux
                .doOnEach(logger.reactiveInfo("I am a message with mdc"))
                .doOnEach(logger.reactiveInfoSignal("here is the message with mdc > "))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId.toString()))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                .collectList()
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            assertEquals(clientId.toString(), logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertEquals(traceId, logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });
    }

    @Test
    void noMdcTraceability_flux() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingFlux
                .doOnEach(logger.reactiveInfo("I am a message"))
                .doOnEach(logger.reactiveInfoSignal("here is the message > "))
                .collectList()
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            assertNull(logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertNull(logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });
    }

    @Test
    void withMdcTraceability_nested() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingFlux
                // log on each signal with value
                .doOnEach(logger.reactiveInfoSignal("here is the message with mdc > "))
                .flatMap(number -> aTestingMono
                        .map(text -> String.format("%s {%s}", text, number))
                        // log on each signal with value
                        .doOnEach(logger.reactiveInfoSignal("mutated obj with mdc > ")))
                // define the reactive mdc properties in the context
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId.toString()))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                // log on each signal but with no MDC since this is out of the subscriber context
                .doOnEach(logger.reactiveInfoSignal("this has no mdc because is an outer stream"))
                .collectList()
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            final String requestContext = logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT));
            final String traceIdContext = logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID));

            if (logItem.getFormattedMessage().contains("has no mdc")) {
                assertNull(requestContext);
            } else {
                assertEquals(clientId.toString(), requestContext);
                assertEquals(traceId, traceIdContext);
            }

        });
    }

    @Test
    void withMdcTraceability_nested_error() {
        // prepare the error message
        final String errorMessage = "BLOWING UP!";

        // prepare the list appender so that later on the log events can be verified
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingFlux
                // log a simple line with the signal
                .doOnEach(logger.reactiveInfoSignal("here is the message with mdc > "))
                // simulate something goes wrong when the number is equal 3 and throw the exception
                .map(number -> {
                    if (number.equals(3)) {
                        throw new RuntimeException("Testing this blowing up");
                    }
                    return number;
                })
                // log the following line (this will happen only when the error is thrown)
                .doOnEach(logger.reactiveErrorThrowable(errorMessage))
                .flatMap(number -> aTestingMono
                        .map(text -> String.format("%s {%s}", text, number))
                        // log a line for each signal in a nested publisher
                        .doOnEach(logger.reactiveInfoSignal("mutated obj with mdc > ")))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId.toString()))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                // recover from the error in the outer stream so the test can be completed
                .onErrorResume((ex) -> Flux.just("test interrupted"))
                .collectList()
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            // ensure all logged lines have traceable context
            assertEquals(clientId.toString(), logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertEquals(traceId, logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });

        // ensure the last line is the error message
        assertTrue(logsList.get(logsList.size() - 1).getFormattedMessage().contains(errorMessage));
    }

    @Test
    void withMdcTraceability_complete() {

        String message = "I will only be called at the end of the sequence";

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingFlux
                // this line will be logged on each on each signal
                .doOnEach(logger.reactiveInfoSignal("here is the message with mdc > "))
                // this line will be logged only on each complete signal
                .doOnEach(logger.reactiveInfoComplete(message))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId.toString()))
                .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                .collectList()
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            assertEquals(clientId.toString(), logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertEquals(traceId, logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });

        // ensure the last line is infoComplete message
        assertTrue(logsList.get(logsList.size() - 1).getFormattedMessage().contains(message));
    }

    @Test
    @DisplayName("Logging traceability should work in a multi-threaded fashion")
    void parallel() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        // create thread ONE with a clientId of ONE and log some lines
        Thread one = new Thread(() -> {
            final String clientId = "ONE";
            final String traceId = clientId;
            aTestingFlux
                    // this line will be logged on each on each signal
                    .doOnEach(logger.reactiveInfoSignal("thread ONE log > "))
                    // this line will be logged only on each complete signal
                    .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId))
                    .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                    .collectList()
                    .block();
        });

        // create thread TWO with a clientId of TWO and log some lines
        Thread two =  new Thread(() -> {
            final String clientId = "TWO";
            final String traceId = clientId;
            aTestingFlux
                    // this line will be logged on each on each signal
                    .doOnEach(logger.reactiveInfoSignal("thread TWO log > "))
                    // this line will be logged only on each complete signal
                    .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.REQUEST_CONTEXT, clientId))
                    .subscriberContext(ReactiveMdc.withOrDefault(ReactiveMdc.Property.TRACE_ID, traceId))
                    .collectList()
                    .block();
        });

        List<Thread> threads = Lists.newArrayList(one, two);

        // start the thread in parrallel and join them to the main thread to get the logs printed
        threads.parallelStream()
                .forEach(thread -> {
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        List<ILoggingEvent> logsList = listAppender.list;

        // check that each thread log has the proper request context id
        logsList.forEach(logItem -> {
            String formattedMessage = logItem.getFormattedMessage();

            if (formattedMessage.contains("ONE")) {
                assertEquals("ONE", logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
                assertEquals("ONE", logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
            } else {
                assertEquals("TWO", logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
                assertEquals("TWO", logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
                assertTrue(formattedMessage.contains("TWO"));
            }
        });
    }

    @Test
    void debug_logging() {
        // put the value to the MDC
        MDC.put(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT), clientId.toString());
        // prepare the list appender so that later on the log events can be verified
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingMono
                // log a simple message on each
                .doOnEach(logger.reactiveDebug("I am a DEBUG message with mdc"))
                // log a simple message on each that includes the signal value
                .doOnEach(logger.reactiveDebugSignal("here is the DEBUG message with mdc > "))
                // define the mdc properties to the reactive context
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.TRACE_ID))
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            assertEquals(clientId.toString(), logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertEquals(traceId, logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });
    }

    @Test
    void warn_logging() {
        // put the value to the MDC
        MDC.put(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT), clientId.toString());

        MDC.put(Enums.asString(ReactiveMdc.Property.TRACE_ID), traceId);
        // prepare the list appender so that later on the log events can be verified
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        listAppender.start();

        logger.addAppender(listAppender);

        aTestingMono
                // log a simple message on each
                .doOnEach(logger.reactiveWarn("I am a WARN message with mdc"))
                // log a simple message on each that includes the signal value
                .doOnEach(logger.reactiveWarnSignal("here is the WARN message with mdc > "))
                // define the mdc properties to the reactive context
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.TRACE_ID))
                .block();

        List<ILoggingEvent> logsList = listAppender.list;

        logsList.forEach(logItem -> {
            assertEquals(clientId.toString(), logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT)));
            assertEquals(traceId, logItem.getMDCPropertyMap().get(Enums.asString(ReactiveMdc.Property.TRACE_ID)));
        });
    }
}
