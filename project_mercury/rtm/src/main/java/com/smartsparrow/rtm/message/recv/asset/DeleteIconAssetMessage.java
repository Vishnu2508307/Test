package com.smartsparrow.rtm.message.recv.asset;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteIconAssetMessage extends ReceivedMessage {

    private String iconLibrary;

    public String getIconLibrary() {
        return iconLibrary;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteIconAssetMessage that = (DeleteIconAssetMessage) o;
        return Objects.equals(iconLibrary, that.iconLibrary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconLibrary);
    }

    @Override
    public String toString() {
        return "DeleteIconAssetMessage{" +
                "iconLibrary='" + iconLibrary + '\'' +
                '}';
    }
}
