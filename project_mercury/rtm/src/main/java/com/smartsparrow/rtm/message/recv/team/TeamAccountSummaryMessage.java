package com.smartsparrow.rtm.message.recv.team;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class TeamAccountSummaryMessage extends ReceivedMessage implements TeamMessage {

    private Integer limit;
    private UUID teamId;

    public Integer getLimit() {
        return limit;
    }

    @Override
    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamAccountSummaryMessage that = (TeamAccountSummaryMessage) o;
        return Objects.equals(limit, that.limit) &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, teamId);
    }

    @Override
    public String toString() {
        return "TeamAccountSummaryMessage{" +
                "limit=" + limit +
                ", teamId=" + teamId +
                '}';
    }
}
