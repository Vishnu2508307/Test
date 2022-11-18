package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RevokeThemePermissionMessage extends ReceivedMessage implements ThemeMessage {

    private UUID themeId;
    private UUID teamId;
    private UUID accountId;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokeThemePermissionMessage that = (RevokeThemePermissionMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, teamId, accountId);
    }

    @Override
    public String toString() {
        return "RevokeThemePermissionMessage{" +
                "themeId=" + themeId +
                ", teamId=" + teamId +
                ", accountId=" + accountId +
                '}';
    }
}
