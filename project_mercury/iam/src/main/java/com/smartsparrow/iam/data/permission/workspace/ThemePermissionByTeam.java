package com.smartsparrow.iam.data.permission.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class ThemePermissionByTeam {

    private UUID teamId;
    private UUID themeId;
    private PermissionLevel permissionLevel;

    public UUID getTeamId() {
        return teamId;
    }

    public ThemePermissionByTeam setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getThemeId() {
        return themeId;
    }

    public ThemePermissionByTeam setThemeId(UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public ThemePermissionByTeam setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemePermissionByTeam that = (ThemePermissionByTeam) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(themeId, that.themeId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, themeId, permissionLevel);
    }

    @Override
    public String toString() {
        return "ThemePermissionByTeam{" +
                "teamId=" + teamId +
                ", themeId=" + themeId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
