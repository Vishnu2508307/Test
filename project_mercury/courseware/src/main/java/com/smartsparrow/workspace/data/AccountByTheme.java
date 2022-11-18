package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountByTheme extends ThemePermission {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountByTheme setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public AccountByTheme setThemeId(UUID themeId) {
        super.setThemeId(themeId);
        return this;
    }

    @Override
    public AccountByTheme setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountByTheme that = (AccountByTheme) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "AccountByTheme{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
