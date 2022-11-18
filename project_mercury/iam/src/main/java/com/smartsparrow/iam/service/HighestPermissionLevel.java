package com.smartsparrow.iam.service;

import java.util.function.BiFunction;

/**
 * An implementation of a {@link BiFunction} that takes two {@link PermissionLevel} objects as arguments and return the
 * highest permission level between the two. The permission level hierarchy is checked by invoking
 * {@link PermissionLevel#isEqualOrHigherThan(PermissionLevel)}
 */
public class HighestPermissionLevel implements BiFunction<PermissionLevel, PermissionLevel, PermissionLevel> {

    @Override
    public PermissionLevel apply(PermissionLevel left, PermissionLevel right) {
        if (left.isEqualOrHigherThan(right)) {
            return left;
        }
        return right;
    }
}
