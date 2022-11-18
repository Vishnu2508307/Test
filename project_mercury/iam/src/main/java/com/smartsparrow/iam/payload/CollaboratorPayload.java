package com.smartsparrow.iam.payload;

import java.util.Objects;

import com.smartsparrow.iam.service.PermissionLevel;

public abstract class CollaboratorPayload {

    private PermissionLevel permissionLevel;

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollaboratorPayload that = (CollaboratorPayload) o;
        return permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionLevel);
    }

    @Override
    public String toString() {
        return "CollaboratorPayload{" +
                "permissionLevel=" + permissionLevel +
                '}';
    }
}
