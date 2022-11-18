package com.smartsparrow.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartsparrow.exception.InvalidJWTException;

public class JWT {

    private static final String SUBJECT_CLAIM = "sub";
    private static final String EXPIRATION_TIME_CLAIM = "exp";

    /**
     * Extract the expiration DateTime expressed in seconds from the ies JWT
     *
     * @param jwt the jwt to extract the expTime from
     * @return the exp date time
     * @throws InvalidJWTException when the token is invalid
     */
    @SuppressWarnings("Duplicates")
    public static long getExpDateTime(String jwt) throws InvalidJWTException {
        String[] splitJWT = jwt.split("\\.");
        if (splitJWT.length < 2) {
            throw new InvalidJWTException("invalid jwt");
        }
        Base64 base64Url = new Base64(true);
        String encPayloadStr = splitJWT[1];
        String decPayloadStr = new String(base64Url.decode(encPayloadStr), StandardCharsets.UTF_8);
        try {
            JSONObject jsonPayload = new JSONObject(decPayloadStr);
            if (jsonPayload.has(EXPIRATION_TIME_CLAIM)) {
                return jsonPayload.getLong(EXPIRATION_TIME_CLAIM);
            }
        } catch (JSONException e) {
            throw new InvalidJWTException("invalid jwt payload");
        }

        throw new InvalidJWTException(String.format("%s not found", EXPIRATION_TIME_CLAIM));
    }

    /**
     * Extract the expiration DateTime from the JWT and returns the duration of the token before expiration, expressed
     * in seconds.
     *
     * @param jwt the token to extract the duration in seconds for
     * @return a long representing the duration in seconds before the token expires
     * @throws InvalidJWTException when the value cannot be extracted from the JWT
     */
    public static long getSecondsExp(String jwt) throws InvalidJWTException {
        long expDateTime = JWT.getExpDateTime(jwt);
        return TimeUnit.MILLISECONDS
                .toSeconds(Instant.ofEpochSecond(expDateTime).toEpochMilli() - Instant.now().toEpochMilli());
    }

    /**
     * Extract the userId from the ies JWT
     *
     * @param jwt the jwt to extract the userId from
     * @return the userId or null when not found
     * @throws InvalidJWTException when the token is invalid
     */
    @SuppressWarnings("Duplicates")
    public static String getUserId(String jwt) throws InvalidJWTException {

        String[] splitJWT = jwt.split("\\.");
        if (splitJWT.length < 2) {
            throw new InvalidJWTException("invalid jwt");
        }
        Base64 base64Url = new Base64(true);
        String encPayloadStr = splitJWT[1];
        String decPayloadStr = new String(base64Url.decode(encPayloadStr), StandardCharsets.UTF_8);

        try {
            JSONObject jsonPayload = new JSONObject(decPayloadStr);
            if (jsonPayload.has(SUBJECT_CLAIM)) {
                return jsonPayload.getString(SUBJECT_CLAIM);
            }
        } catch (JSONException e) {
            throw new InvalidJWTException("invalid jwt payload");
        }

        throw new InvalidJWTException(String.format("%s not found", SUBJECT_CLAIM));
    }
}
