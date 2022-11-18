package com.smartsparrow.sso.service;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.smartsparrow.sso.lang.OAuthHandlerException;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.SimpleOAuthValidator;

public class LTIMessageSignatures {

    public static final String CALLBACK_URL = "about:blank";

    /**
     * Validate the signature of a message.
     *
     * @param oauthMessage the message
     * @param oauthKey the oauth signing key
     * @param sharedSecret the oauth signing shared secret
     * @throws OAuthProblemException when the message has issues, such as invalid_timestamp or signature_invalid
     * @throws RuntimeException in other cases which can not be handled by the client.
     */
    void validate(OAuthMessage oauthMessage, String oauthKey, String sharedSecret) throws OAuthProblemException {
        SimpleOAuthValidator oAuthValidator = new SimpleOAuthValidator();
        OAuthConsumer consumer = new OAuthConsumer(CALLBACK_URL, oauthKey, sharedSecret, null);
        OAuthAccessor accessor = new OAuthAccessor(consumer);

        try {
            oAuthValidator.validateMessage(oauthMessage, accessor);
        } catch (OAuthProblemException ope) {
            // the most common thrown exception in this exception, enable callers to handle it...
            throw ope;
        } catch (OAuthException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sign an OAuth Message.
     *
     * @param oAuthMessage the message
     * @param oauthKey the signing key
     * @param sharedSecret the signing shared secret
     * @return an updated parameter map contiaing a signature property
     * @throws OAuthHandlerException when something goes wrong.
     */
    public static Map<String, String> sign(OAuthMessage oAuthMessage, String oauthKey, String sharedSecret) throws OAuthHandlerException {
        affirmNotNull(oAuthMessage, "oAuthMessage required");

        OAuthConsumer consumer = new OAuthConsumer(CALLBACK_URL, oauthKey, sharedSecret, null);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        try {
            oAuthMessage.addRequiredParameters(accessor);

            Map<String, String> params = new HashMap<>();

            for (Map.Entry<String, String> param : oAuthMessage.getParameters()){
                params.put(param.getKey(), param.getValue());
            }

            return params;
        } catch (OAuthException | IOException | URISyntaxException e) {
            throw new OAuthHandlerException(String.format("Error signing the message: %s", e.getMessage()));
        }
    }
}
