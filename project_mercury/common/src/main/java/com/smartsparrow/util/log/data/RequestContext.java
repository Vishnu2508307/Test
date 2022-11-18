package com.smartsparrow.util.log.data;

import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestContext {

    private String clientId;
    private String accountId;
    private Request request;

    public String getClientId() {
        return clientId;
    }

    public RequestContext setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Nullable
    public String getAccountId() {
        return accountId;
    }

    public RequestContext setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public Request getRequest() {
        return request;
    }

    public RequestContext setRequest(Request request) {
        this.request = request;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestContext that = (RequestContext) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, accountId, request);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
