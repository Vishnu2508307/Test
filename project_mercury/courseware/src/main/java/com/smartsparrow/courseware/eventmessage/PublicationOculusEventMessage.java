package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;

public class PublicationOculusEventMessage {

    private String oculusStatus;
    private String oculusVersion;
    private String bookId;

    /**
     * Private empty constructor for serialization.
     *
     */
    @SuppressWarnings("unused")
    public PublicationOculusEventMessage() {
    }

    public PublicationOculusEventMessage(String bookId) {
        this.bookId = bookId;
    }

    public String getOculusStatus() {
        return oculusStatus;
    }

    public void setOculusStatus(final String oculusStatus) {
        this.oculusStatus = oculusStatus;
    }

    public String getOculusVersion() {
        return oculusVersion;
    }

    public PublicationOculusEventMessage setOculusVersion(final String oculusVersion) {
        this.oculusVersion = oculusVersion;
        return this;
    }

    public String getBookId() {
        return bookId;
    }

    public PublicationOculusEventMessage setBookId(final String bookId) {
        this.bookId = bookId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationOculusEventMessage that = (PublicationOculusEventMessage) o;
        return Objects.equals(bookId, that.bookId) &&
                Objects.equals(oculusStatus, that.oculusStatus) &&
                Objects.equals(oculusVersion, that.oculusVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, oculusStatus, oculusVersion);
    }

    @Override
    public String toString() {
        return "PublicationOculusEventMessage{" +
                "bookId='" + bookId + '\'' +
                ", oculusStatus='" + oculusStatus + '\'' +
                ", oculusVersion=" + oculusVersion +
                '}';
    }
}
