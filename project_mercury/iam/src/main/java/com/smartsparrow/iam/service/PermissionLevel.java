package com.smartsparrow.iam.service;

public enum PermissionLevel {

    OWNER(30),

    CONTRIBUTOR(20),

    REVIEWER(10);

    private int level;

    PermissionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isLowerThan(PermissionLevel permissionLevel) {
        return permissionLevel != null && level < permissionLevel.getLevel();
    }

    public boolean isEqualOrLowerThan(PermissionLevel permissionLevel) {
        return permissionLevel != null && level <= permissionLevel.getLevel();
    }

    public boolean isEqualOrHigherThan(PermissionLevel permissionLevel) {
        return permissionLevel != null && level >= permissionLevel.getLevel();
    }
}
