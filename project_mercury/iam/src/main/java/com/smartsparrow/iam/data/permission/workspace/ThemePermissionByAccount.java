package com.smartsparrow.iam.data.permission.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class ThemePermissionByAccount {

    private UUID accountId;
    private UUID themeId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public ThemePermissionByAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getThemeId() {
        return themeId;
    }

    public ThemePermissionByAccount setThemeId(UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public ThemePermissionByAccount setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemePermissionByAccount that = (ThemePermissionByAccount) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(themeId, that.themeId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, themeId, permissionLevel);
    }

    @Override
    public String toString() {
        return "ThemePermissionByAccount{" +
                "accountId=" + accountId +
                ", themeId=" + themeId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
