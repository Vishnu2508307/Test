package com.smartsparrow.util;

import com.smartsparrow.exception.InvalidCTSException;

public class CTS {

    /**
     * Validate the format of a CTS session token
     *
     * @param token the session token to validate
     * @throws InvalidCTSException when the token format is invalid
     */
    public static void validateFormatting(String token) throws InvalidCTSException {
        String[] splitCTS = token.split("\\.", 2);
        if (splitCTS.length < 2) {
            throw new InvalidCTSException("invalid cts session token");
        }

        String encSessionKeyStr = splitCTS[1];
        if (!(encSessionKeyStr.startsWith("*") && encSessionKeyStr.endsWith("..*"))) {
            throw new InvalidCTSException("invalid cts session key");
        }
    }
}
