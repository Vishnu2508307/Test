package com.smartsparrow.iam.util;

import com.smartsparrow.iam.exception.PermissionFault;

/**
 * Helper class to evaluate permission expressions and throw permission errors.
 */
public class Permissions {

    public static final String DEFAULT_ERROR_MESSAGE = "Unauthorized";

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws PermissionFault if {@code expression} is false
     */
    public static void affirmPermission(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new PermissionFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     */
    public static void affirmPermission(boolean expression) {
        affirmPermission(expression, DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Generate a failure permission fault
     *
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     */
    public static void failPermission(Object errorMessage) {
        throw new PermissionFault(String.valueOf(errorMessage));
    }

    /**
     * Generate a failure permission fault using the default error message.
     */
    public static void failPermission() {
        failPermission(DEFAULT_ERROR_MESSAGE);
    }
}
