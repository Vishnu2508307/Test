package com.smartsparrow.export.data;

import java.util.Objects;
import java.util.UUID;

public class AmbrosiaReducerErrorLog {

    private UUID exportId;
    private String errorMessage;
    private String cause;

    public UUID getExportId() {
        return exportId;
    }

    public AmbrosiaReducerErrorLog setExportId(final UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public AmbrosiaReducerErrorLog setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public AmbrosiaReducerErrorLog setCause(final String cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmbrosiaReducerErrorLog that = (AmbrosiaReducerErrorLog) o;
        return  Objects.equals(exportId, that.exportId) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId, errorMessage, cause);
    }

    @Override
    public String toString() {
        return "AmbrosiaReducerErrorLog{" +
                ", exportId=" + exportId +
                ", errorMessage='" + errorMessage + '\'' +
                ", cause='" + cause + '\'' +
                '}';
    }
}
