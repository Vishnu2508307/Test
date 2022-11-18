package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ThemeByTeam {

    private UUID teamId;
    private UUID themeId;

    public UUID getTeamId() {
        return teamId;
    }

    public ThemeByTeam setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getThemeId() {
        return themeId;
    }

    public ThemeByTeam setThemeId(UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeByTeam that = (ThemeByTeam) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(themeId, that.themeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, themeId);
    }

    @Override
    public String toString() {
        return "ThemeByTeam{" +
                "teamId=" + teamId +
                ", themeId=" + themeId +
                '}';
    }
}
