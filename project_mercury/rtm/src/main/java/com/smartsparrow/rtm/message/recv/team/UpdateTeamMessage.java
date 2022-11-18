package com.smartsparrow.rtm.message.recv.team;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdateTeamMessage extends ReceivedMessage implements TeamMessage {

    private UUID teamId;
    private String name;
    private String description;
    private String thumbnail;

    @Override
    public UUID getTeamId() {
        return teamId;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateTeamMessage that = (UpdateTeamMessage) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(thumbnail, that.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, name, description, thumbnail);
    }

    @Override
    public String toString() {
        return "UpdateTeamMessage{" +
                "teamId=" + teamId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                "} " + super.toString();
    }
}
