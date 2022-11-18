package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PermissionLevelTest {

    @Test
    void isLowerThan() {
        assertTrue(PermissionLevel.REVIEWER.isLowerThan(PermissionLevel.CONTRIBUTOR));
        assertTrue(PermissionLevel.REVIEWER.isLowerThan(PermissionLevel.OWNER));
        assertFalse(PermissionLevel.OWNER.isLowerThan(PermissionLevel.OWNER));
        assertFalse(PermissionLevel.OWNER.isLowerThan(PermissionLevel.CONTRIBUTOR));
        assertFalse(PermissionLevel.OWNER.isLowerThan(PermissionLevel.REVIEWER));
    }

    @Test
    void isEqualOrHigher() {
        assertTrue(PermissionLevel.REVIEWER.isEqualOrHigherThan(PermissionLevel.REVIEWER));
        assertTrue(PermissionLevel.OWNER.isEqualOrHigherThan(PermissionLevel.REVIEWER));
        assertTrue(PermissionLevel.OWNER.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR));
    }
}
