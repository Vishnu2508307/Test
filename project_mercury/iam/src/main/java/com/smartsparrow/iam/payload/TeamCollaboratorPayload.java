package com.smartsparrow.iam.payload;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.PermissionLevel;

public class TeamCollaboratorPayload extends CollaboratorPayload {

    private TeamSummary teamPayload;

    @JsonProperty("team")
    public TeamSummary getTeamPayload() {
        return teamPayload;
    }

    public void setTeamPayload(TeamSummary teamPayload) {
        this.teamPayload = teamPayload;
    }

    public static TeamCollaboratorPayload from(@Nonnull TeamSummary team, PermissionLevel permissionLevel) {
        TeamCollaboratorPayload payload = new TeamCollaboratorPayload();
        payload.setTeamPayload(team);
        payload.setPermissionLevel(permissionLevel);
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamCollaboratorPayload that = (TeamCollaboratorPayload) o;
        return Objects.equals(teamPayload, that.teamPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamPayload);
    }

    @Override
    public String toString() {
        return "TeamCollaboratorPayload{" +
                "teamPayload=" + teamPayload +
                "} " + super.toString();
    }
}
