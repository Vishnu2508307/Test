package com.smartsparrow.rtm.message.recv.team;

import com.google.common.base.Objects;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateTeamMessage extends ReceivedMessage {

    private String name;
    private String description;
    private String thumbnail;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public String toString() {
        return "CreateTeamMessage{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", thumbnail='"
                + thumbnail + '\'' + "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CreateTeamMessage that = (CreateTeamMessage) o;
        return Objects.equal(getName(), that.getName()) && Objects.equal(getDescription(), that.getDescription())
                && Objects.equal(getThumbnail(), that.getThumbnail());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getDescription(), getThumbnail());
    }
}
