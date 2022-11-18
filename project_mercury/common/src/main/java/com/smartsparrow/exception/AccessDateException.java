package com.smartsparrow.exception;

import javax.ws.rs.core.Response.Status;

public class AccessDateException extends RuntimeException implements ErrorResponseType {

    private static final long serialVersionUID = -1296123372501101953L;

    private int offset;

    private long startDate;

    public AccessDateException(final String message, final int offset, final long startDate) {
        super(message);
        this.offset = offset;
        this.startDate = startDate;
    }

    public AccessDateException(final String message, final int offset) {
        super(message);
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public long getStartDate() {
        return startDate;
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
