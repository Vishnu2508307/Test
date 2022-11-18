package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.workspace.data.IconLibrary;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AssociateActivityIconLibraryMessage extends ReceivedMessage {

    private UUID activityId;
    private List<IconLibrary> iconLibraries;

    public UUID getActivityId() {
        return activityId;
    }

    public List<IconLibrary> getIconLibraries() {
        return iconLibraries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssociateActivityIconLibraryMessage that = (AssociateActivityIconLibraryMessage) o;
        return Objects.equals(activityId, that.activityId) && Objects.equals(iconLibraries,
                                                                             that.iconLibraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, iconLibraries);
    }

    @Override
    public String toString() {
        return "AssociateActivityIconLibraryMessage{" +
                "activityId=" + activityId +
                ", iconLibraries=" + iconLibraries +
                '}';
    }
}
