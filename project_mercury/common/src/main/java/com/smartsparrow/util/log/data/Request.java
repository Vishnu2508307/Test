package com.smartsparrow.util.log.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;

import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    public enum Type {
        WS,
        REST
    }

    private Type type;
    private String method;
    private String uri;
    private String requestId;
    private String messageId;

    public Type getType() {
        return type;
    }

    public Request setType(Type type) {
        this.type = type;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public Request setUri(final String uri) {
        this.uri = uri;
        return this;
    }

    //This is null when the request is WS
    @Nullable
    public String getRequestId() { return requestId; }

    public Request setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    //This is null when the request is REST
    @Nullable
    public String getMessageId() { return messageId; }

    public Request setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return type == request.type &&
                Objects.equals(method, request.method) &&
                Objects.equals(uri, request.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, method, uri);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
