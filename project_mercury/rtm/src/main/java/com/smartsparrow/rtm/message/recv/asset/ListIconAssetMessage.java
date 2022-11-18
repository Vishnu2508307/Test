package com.smartsparrow.rtm.message.recv.asset;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListIconAssetMessage extends ReceivedMessage {

    private List<String> iconLibraries;

    public List<String> getIconLibraries() {
        return iconLibraries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListIconAssetMessage that = (ListIconAssetMessage) o;
        return Objects.equals(iconLibraries, that.iconLibraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconLibraries);
    }

    @Override
    public String toString() {
        return "ListIconAssetMessage{" +
                "iconLibraries=" + iconLibraries +
                '}';
    }
}
