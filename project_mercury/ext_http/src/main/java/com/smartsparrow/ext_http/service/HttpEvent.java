package com.smartsparrow.ext_http.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.ext_http.service.timing.Timing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The inner object of the main http request/response flow.
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson using field reflection, obj is immutable conceptually")
public class HttpEvent {

    /**
     * The operation of the request-response cycle; the following attributes are available based on this enum
     *  > request - uri, method, headers, [body]
     *  > redirect - uri, headers, statusCode
     *  > response - uri, headers, statusCode, body, time
     *  > error - body
     */
    public enum Operation {
        request,
        redirect,
        response,
        error
    }

    private Operation operation;
    private String uri;
    private String method;

    // for cases where the response contains repeated headers, the request framework reduces them per spec.
    // e.g. if the http response looks like:
    //  < set-cookie: bar
    //  < set-cookie: baz
    // then it is represented as this in the returned data:
    //  {
    //    'x-foo': ['bar', 'baz']
    //  }
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private Map<String, List<String>> headers;
    private String body;
    private Integer statusCode;
    private Timing time;

    public HttpEvent() {
    }

    public Operation getOperation() {
        return operation;
    }

    public HttpEvent setOperation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public HttpEvent setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public HttpEvent setMethod(String method) {
        this.method = method;
        return this;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public HttpEvent setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public String getBody() {
        return body;
    }

    public HttpEvent setBody(JsonNode body) {
        this.body = body.toString();
        return this;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public HttpEvent setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public Timing getTime() {
        return time;
    }

    public HttpEvent setTime(Timing time) {
        this.time = time;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HttpEvent httpEvent = (HttpEvent) o;
        return operation == httpEvent.operation && Objects.equals(uri, httpEvent.uri) && Objects.equals(method,
                                                                                                        httpEvent.method)
                && Objects.equals(headers, httpEvent.headers) && Objects.equals(body, httpEvent.body) && Objects.equals(
                statusCode, httpEvent.statusCode) && Objects.equals(time, httpEvent.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, uri, method, headers, body, statusCode, time);
    }

    @Override
    public String toString() {
        return "HttpEvent{" + "operation=" + operation + ", uri='" + uri + '\'' + ", method='" + method + '\''
                + ", headers=" + headers + ", body='" + body + '\'' + ", statusCode=" + statusCode + ", time=" + time
                + '}';
    }
}
