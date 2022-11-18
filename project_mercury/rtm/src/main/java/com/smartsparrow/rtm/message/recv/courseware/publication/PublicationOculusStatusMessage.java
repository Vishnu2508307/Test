package com.smartsparrow.rtm.message.recv.courseware.publication;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

public class PublicationOculusStatusMessage extends ReceivedMessage {

    private String bookId;

    public String getBookId() {
        return bookId;
    }

    public PublicationOculusStatusMessage setBookId(final String bookId) {
        this.bookId = bookId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationOculusStatusMessage that = (PublicationOculusStatusMessage) o;
        return Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return "PublicationOculusStatusMessage{" +
                "bookId=" + bookId +
                "} " + super.toString();
    }
}
