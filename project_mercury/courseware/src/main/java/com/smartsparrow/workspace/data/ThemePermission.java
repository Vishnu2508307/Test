package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class ThemePermission {

    private UUID themeId;
    private PermissionLevel permissionLevel;

    public UUID getThemeId() {
        return themeId;
    }

    public ThemePermission setThemeId(UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public ThemePermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemePermission that = (ThemePermission) o;
        return Objects.equals(themeId, that.themeId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, permissionLevel);
    }

    @Override
    public String toString() {
        return "ThemePermission{" +
                "themeId=" + themeId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
