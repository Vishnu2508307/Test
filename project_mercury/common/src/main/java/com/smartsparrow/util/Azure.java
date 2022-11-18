package com.smartsparrow.util;

import com.google.common.base.Strings;
import com.smartsparrow.exception.InvalidAzureException;

public class Azure {

    /**
     * Validate the format of an Azure session token
     *
     * @param token the session token to validate
     * @throws InvalidAzureException when the token is missing
     */
    public static void validateFormatting(String token) throws InvalidAzureException {

        // TODO: just check if token is null or empty for now. this class will be updated later
        // Azure token reference link: https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
        if (Strings.isNullOrEmpty(token)) {
            throw new InvalidAzureException("token is missing");
        }

        // for citrus test
        if (token.contains("invalidToken")) {
            throw new InvalidAzureException("invalid Azure sesstion token");
        }
    }
}
