package com.smartsparrow.asset.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AkamaiTokenAuthenticationConfiguration;
import com.smartsparrow.asset.data.AssetSignatureConfiguration;
import com.smartsparrow.asset.data.AssetSignatureStrategyType;
import com.smartsparrow.exception.IllegalStateFault;

class AssetSignatureConfigurationDeserializerTest {

    @InjectMocks
    private AssetSignatureConfigurationDeserializer deserializer;

    private static final UUID signatureId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void deserialize_akamaiTokenAuth_unsupportedFormat() {
        final String config = "{\"foo\":\"bar\"}";

        IllegalStateFault f = assertThrows(IllegalStateFault.class,
                () -> deserializer.deserialize(AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION, config));

        assertNotNull(f);
        assertEquals("could not deserialize asset signature configuration", f.getMessage());
    }

    @Test
    void deserialize_akamaiTokenAuth() {
        final String config = "{\"tokenName\":\"blah\", \"key\":\"secret\", \"windowSeconds\": 500}";

        AssetSignatureConfiguration deserialized = deserializer.deserialize(AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION, config);

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof AkamaiTokenAuthenticationConfiguration);

        AkamaiTokenAuthenticationConfiguration akamaiconfig = (AkamaiTokenAuthenticationConfiguration) deserialized;

        assertAll(() -> {
            assertNotNull(akamaiconfig);
            assertEquals("blah", akamaiconfig.getTokenName());
            assertEquals(Long.valueOf(500), akamaiconfig.getWindowSeconds());
            assertEquals("secret", akamaiconfig.getKey());
        });
    }
}