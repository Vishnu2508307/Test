package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class AccessDatesException extends RuntimeException implements ErrorResponseType {

    final Long begin;
    final Long end;

    private static final long serialVersionUID = -120378456871078937L;

    public AccessDatesException(final Long begin, final Long end) {
        super();

        this.begin = begin;
        this.end = end;
    }

    public Long getBegin() {
        return begin;
    }

    public Long getEnd() {
        return end;
    }

    @Override
    public int getResponseStatusCode() {
        return Status.FORBIDDEN.getStatusCode();
    }

    @Override
    public String getType() {
        return "ACCESS_DATE";
    }
}
