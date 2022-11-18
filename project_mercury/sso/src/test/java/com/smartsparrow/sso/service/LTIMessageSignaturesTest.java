package com.smartsparrow.sso.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.sso.lang.OAuthHandlerException;

import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;

class LTIMessageSignaturesTest {

    @InjectMocks
    private LTIMessageSignatures ltiMessageSignatures;

    private final static String key = "key";
    private final static String secret = "secret";
    private final static String invalidSecret = "invalidSecret";
    private final static String url = "http://some.tld";
    private final static String method = "HMAC-SHA1";

    @BeforeEach
    public void setup() {
        // create any @Mock
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void sign() throws OAuthHandlerException {
        OAuthMessage oAuthMessage = new OAuthMessage(method, url, null);
        Map<String, String> signedParams = LTIMessageSignatures.sign(oAuthMessage, key, secret);

        assertTrue(signedParams.size() == 6);
    }

    @Test
    void sign_fails() {
        assertThrows(IllegalArgumentFault.class, () -> LTIMessageSignatures.sign(null, key, secret));
    }

    @Test
    void validateSignature_wrongSecret() throws OAuthHandlerException {
        OAuthMessage oAuthMessage = new OAuthMessage(method, url, null);
        LTIMessageSignatures.sign(oAuthMessage, key, secret);
        assertThrows(OAuthProblemException.class,
                     () -> ltiMessageSignatures.validate(oAuthMessage, key, invalidSecret));
    }

    @Test
    void validateSignature_success() throws Exception {
        OAuthMessage oAuthMessage = new OAuthMessage(method, url, null);
        LTIMessageSignatures.sign(oAuthMessage, key, secret);
        ltiMessageSignatures.validate(oAuthMessage, key, secret);
    }

}