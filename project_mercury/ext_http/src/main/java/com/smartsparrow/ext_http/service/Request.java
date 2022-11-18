package com.smartsparrow.ext_http.service;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartsparrow.exception.IllegalStateFault;

/**
 * Object to create an external http request process.
 * <p>
 * The params of the object are the attributes & capabilities as defined here:
 * https://github.com/request/request#requestoptions-callback
 * <p>
 * Additional convenience set methods have been added to assist with readability.
 * <p>
 * Example usage:
 * <pre>
 *         Request r = new Request() //
 *                 .setUri("https://images.google.com") //
 *                 .addField("qs", ImmutableMap.of("q", "homer+simpson"));
 * </pre>
 */
public class Request {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ObjectNode params = mapper.createObjectNode();

    public Request() {
    }

    /**
     * Get the request parameters which are used to perform the request.
     *
     * @return an ObjectNode containing the request parameters
     * @see <a href="https://github.com/request/request#requestoptions-callback">https://github.com/request/request#requestoptions-callback"</a>
     */
    public ObjectNode getParams() {
        return params;
    }

    /**
     * Set the raw parameters of the request
     *
     * @param params the raw params of the request
     * @return this
     * @see <a href="https://github.com/request/request#requestoptions-callback">https://github.com/request/request#requestoptions-callback"</a>
     */
    Request setParams(ObjectNode params) {
        this.params = params;
        return this;
    }

    /*
     * Convenience methods
     */

    /**
     * Set the URI of the request
     *
     * @param uri the URI of the request
     * @return this
     */
    @JsonIgnore
    public Request setUri(String uri) {
        params.put("uri", uri);
        return this;
    }

    /**
     * Set the Method of the request
     *
     * @param method the URI of the request
     * @return this
     */
    @JsonIgnore
    public Request setMethod(String method) {
        params.put("method", method);
        return this;
    }

    /**
     * Set body to JSON representation of value and adds Content-type: application/json header. Additionally,
     * parses the response body as JSON.
     * @param json
     * @return
     */
    @JsonIgnore
    public Request setJson(boolean json) {
        addField("json", json);;
        return this;
    }

    /**
     * Add a field to the request parameters
     *
     * @param fieldName the name of the field
     * @param value the value of the field
     * @return this
     */
    @JsonIgnore
    public Request addField(String fieldName, Object value) {
        params.putPOJO(fieldName, value);
        return this;
    }

    /**
     * Get the params as Json
     *
     * @return the params as Json
     * @throws IllegalStateFault raised when converting the params to JSON fails
     */
    @JsonIgnore
    public String getParamsAsJson() {
        try {
            return mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new IllegalStateFault(e.getMessage());
        }
    }

    /**
     * Set the raw internal parameters
     *
     * @param paramsJson the string representation of the params
     * @return this
     */
    @JsonIgnore
    public Request setParamsFromJson(final String paramsJson) {
        try {
            setParams((ObjectNode) mapper.readTree(paramsJson));
        } catch (IOException e) {
            throw new IllegalStateFault(e.getMessage());
        }
        return this;
    }

    /*
     *
     */

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(params, request.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }

    @Override
    public String toString() {
        return "Request{" + "params=" + params + '}';
    }

}
