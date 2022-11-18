package com.smartsparrow.publication.data;

import java.util.Objects;

public class PublicationOculusData {

    private String oculusStatus;
    private String oculusVersion;
    private String bookId;

    public String getOculusStatus() {
        return oculusStatus;
    }

    public void setOculusStatus(final String oculusStatus) {
        this.oculusStatus = oculusStatus;
    }

    public String getOculusVersion() {
        return oculusVersion;
    }

    public PublicationOculusData setOculusVersion(final String oculusVersion) {
        this.oculusVersion = oculusVersion;
        return this;
    }

    public String getBookId() {
        return bookId;
    }

    public PublicationOculusData setBookId(final String bookId) {
        this.bookId = bookId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationOculusData that = (PublicationOculusData) o;
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
        return "PublicationOculusData{" +
                "bookId='" + bookId + '\'' +
                ", oculusStatus='" + oculusStatus + '\'' +
                ", oculusVersion=" + oculusVersion +
                '}';
    }
}
