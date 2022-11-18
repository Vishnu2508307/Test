package com.smartsparrow.iam.service;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.smartsparrow.util.Enums;

/**
 * The roles which this account acts as.
 */
public enum AccountRole {

    // an instructor, this role is kept and provisioned for aelp compatibility
    @Deprecated
    INSTRUCTOR(10),

    // an instructor in AERO
    AERO_INSTRUCTOR(10),

    // a student (nearly everyone gets this role)
    STUDENT(0),

    // a (legacy) anonymous / guest student.
    STUDENT_GUEST(0),

    // a developer user
    DEVELOPER(10),

    // a subscription administrator user
    ADMIN(50),

    // a support user
    SUPPORT(100);

    private final int level;

    AccountRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isEqualOrHigherThan(@Nonnull AccountRole role) {
        return level >= role.getLevel();
    }

    public static final Set<AccountRole> WORKSPACE_ROLES = Sets.immutableEnumSet(
            AccountRole.AERO_INSTRUCTOR,
            AccountRole.DEVELOPER,
            AccountRole.ADMIN,
            AccountRole.SUPPORT);

    public static final Set<AccountRole> RESTRICTED_ROLES = Sets.immutableEnumSet(
            // this role is marked as restricted due to it's deprecation
            AccountRole.INSTRUCTOR,
            // this role is marked as restricted, due to the power that comes with it
            AccountRole.SUPPORT
    );

    public static boolean isRestricted(@Nonnull final String role) {
        return RESTRICTED_ROLES.stream()
                .map(Enums::asString)
                .anyMatch(restrictedRole -> restrictedRole.equals(role));
    }
}
