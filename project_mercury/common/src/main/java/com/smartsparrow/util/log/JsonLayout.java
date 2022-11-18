package com.smartsparrow.util.log;

import static ch.qos.logback.core.CoreConstants.HOSTNAME_KEY;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.data.JsonLog;
import com.smartsparrow.util.log.data.EventLog;
import com.smartsparrow.util.log.data.LogMessage;
import com.smartsparrow.util.log.data.RequestContext;
import com.smartsparrow.util.log.data.ServerContext;
import com.smartsparrow.util.log.data.ThreadLog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

/**
 * Json layout definition for logs
 */
public class JsonLayout extends LayoutBase<ILoggingEvent> {

    public static final String REQUEST_CONTEXT = Enums.asString(ReactiveMdc.Property.REQUEST_CONTEXT);
    public static final String TRACE_ID = Enums.asString(ReactiveMdc.Property.TRACE_ID);
    public static final String SERVER_CONTEXT = "serverContext";

    @Override
    public String doLayout(ILoggingEvent event) {

        // prepare the message and the context information
        String formattedMessage = event.getFormattedMessage();
        RequestContext requestContext = new RequestContext();

        String requestContextString = event.getMDCPropertyMap().get(REQUEST_CONTEXT);
        String traceId = event.getMDCPropertyMap().get(TRACE_ID);

        ObjectMapper om = new ObjectMapper();

        // get the request context object when defined
        if (requestContextString != null) {
            try {
                requestContext = om.readValue(requestContextString, RequestContext.class);
            } catch (IOException e) {
                // do nothing here
            }
        }

        // build the log message
        LogMessage logMessage;

        try {
            logMessage = om.readValue(formattedMessage, LogMessage.class);
        } catch (IOException e) {
            logMessage = new LogMessage().setField("content", formattedMessage);
        }

        // Add error stacktrace to log message
        if (event.getLevel().toString().equals("ERROR")) {
            if (event.getThrowableProxy() != null) {
                String stackTrace = Arrays.toString(event.getThrowableProxy().getStackTraceElementProxyArray());
                logMessage.setField("stackTrace", stackTrace);
            }
        }

        // get the current thread
        Thread currentThread = Thread.currentThread();

        ServerContext serverContext = new ServerContext()
                .setHostName(getContext().getProperty(HOSTNAME_KEY));

        ThreadLog threadLog = new ThreadLog()
                .setName(event.getThreadName())
                .setId(currentThread.getId());

        EventLog eventLog = new EventLog()
                .setMessage(logMessage)
                .setRequestContext(requestContext)
                .setServerContext(serverContext)
                .setTraceId(traceId);

        JsonLog jsonLog = new JsonLog()
                .setRelative(event.getTimeStamp() - event.getLoggerContextVO().getBirthTime())
                .setTimestamp(event.getTimeStamp())
                .setThreadLog(threadLog)
                .setLevel(event.getLevel().toString())
                .setLogger(event.getLoggerName())
                .setEvent(eventLog);

        // return the json log string with a line separator at the end
        return jsonLog.toString() + CoreConstants.LINE_SEPARATOR;
    }
}
