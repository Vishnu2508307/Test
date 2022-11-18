package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamByTheme extends ThemePermission {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamByTheme setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public TeamByTheme setThemeId(UUID themeId) {
        super.setThemeId(themeId);
        return this;
    }

    @Override
    public TeamByTheme setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamByTheme that = (TeamByTheme) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamId);
    }

    @Override
    public String toString() {
        return "TeamByTheme{" +
                "teamId=" + teamId +
                "} " + super.toString();
    }
}
