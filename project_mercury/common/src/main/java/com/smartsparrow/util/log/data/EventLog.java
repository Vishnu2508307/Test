package com.smartsparrow.util.log.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventLog {

    private RequestContext requestContext;
    private ServerContext serverContext;
    private LogMessage message;
    private String traceId;

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public EventLog setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
        return this;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public EventLog setServerContext(ServerContext serverContext) {
        this.serverContext = serverContext;
        return this;
    }

    public LogMessage getMessage() {
        return message;
    }

    public EventLog setMessage(LogMessage message) {
        this.message = message;
        return this;
    }

    public String getTraceId() {
        return traceId;
    }

    public EventLog setTraceId(final String traceId) {
        this.traceId = traceId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventLog eventLog = (EventLog) o;
        return Objects.equals(requestContext, eventLog.requestContext) &&
                Objects.equals(serverContext, eventLog.serverContext) &&
                Objects.equals(message, eventLog.message)&&
                Objects.equals(traceId, eventLog.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestContext, serverContext, message, traceId);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
