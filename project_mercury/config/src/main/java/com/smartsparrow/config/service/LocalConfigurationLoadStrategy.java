package com.smartsparrow.config.service;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class LocalConfigurationLoadStrategy<T> implements ConfigurationLoadStrategy<T> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LocalConfigurationLoadStrategy.class);

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Return response object for the local and sandbox environment for the given key
     *
     * @param context the configuration context
     * @param type the response type
     * @return return response object
     */
    @Override
    public <T> T load(ConfigurationContext context, final Class<T> type) {
        T response = null;
        String key = context.getKey();
        try {
            JsonNode jsonNode = loadFileFromResourcePath(context.getEnv(), context.getPrefix(), context.getFileName());
            // get config string of given key only
            String configString = Json.getStringValue(jsonNode.get(key));

            response = mapper.readValue(configString, type);
        } catch (IOException e) {
            log.error(String.format("Exception during loading local configuration for key=%s and env = %s"
                    , key, context.getEnv()), e);
            throw new JSONException(String.format("Exception during loading local configuration for key=%s and env = %s"
                    , key, context.getEnv()), e);
        }
        return response;
    }

    /**
     * Load file from local resource path and return json node object
     * @param env the env name
     * @param prefix the config file prefix name
     * @param fileName the file name

     * @return Json node object
     */
    public JsonNode loadFileFromResourcePath(String env, String prefix, String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(env + "/" + prefix + "/" + fileName);
        return mapper.readTree(inputStream);
    }



}
