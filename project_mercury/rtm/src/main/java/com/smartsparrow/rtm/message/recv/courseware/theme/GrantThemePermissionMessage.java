package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GrantThemePermissionMessage extends ReceivedMessage implements ThemeMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID themeId;
    private PermissionLevel permissionLevel;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    public List<UUID> getTeamIds() {
        return teamIds;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantThemePermissionMessage that = (GrantThemePermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(themeId, that.themeId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, themeId, permissionLevel);
    }

    @Override
    public String toString() {
        return "GrantThemePermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", themeId=" + themeId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
