package com.smartsparrow.asset.service;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.asset.data.AkamaiTokenAuthenticationConfiguration;
import com.smartsparrow.asset.data.AssetSignatureConfiguration;
import com.smartsparrow.asset.data.AssetSignatureStrategyType;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.Enums;

public class AssetSignatureConfigurationDeserializer {

    @Inject
    public AssetSignatureConfigurationDeserializer() {
    }

    /**
     * Deserialize the asset signature configuration to the appropriate pojo
     *
     * @param type the signature strategy type
     * @param config a json string holding the configurations
     * @return the deserialized asset signature configuration object
     */
    public AssetSignatureConfiguration deserialize(final AssetSignatureStrategyType type,
                                                   final String config) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            switch (type) {
                case AKAMAI_TOKEN_AUTHENTICATION:
                    return objectMapper.readValue(config, AkamaiTokenAuthenticationConfiguration.class);
                default:
                    throw new UnsupportedOperationFault(String.format("%s is unsupported", Enums.asString(type)));
            }
        } catch (IOException e) {
            // not recoverable, re-throw as runtime
            throw new IllegalStateFault("could not deserialize asset signature configuration");
        }
    }
}
