package com.smartsparrow.config.service;

import java.io.IOException;

import org.json.JSONException;

import com.amazonaws.services.acmpca.model.ResourceNotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;


public class S3ConfigurationLoadStrategy<T> implements ConfigurationLoadStrategy<T> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(S3ConfigurationLoadStrategy.class);

    private final AmazonS3 s3client;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public S3ConfigurationLoadStrategy(final AmazonS3 s3client) {
        this.s3client = s3client;
    }

    /**
     * Load configuration from S3 bucket for provided env, region and domain name
     * Return config response for provided request key
     * @param context the configuration context
     * @param type the response type object
     * @param <T> the class type
     * @return the response object
     */
    @Override
    public <T> T load(final ConfigurationContext context, final Class<T> type) {
        T response = null;
        String key = context.getKey();
        String fileName = context.getFileName();
        String prefix = context.getPrefix();
        String env = context.getEnv();
        String region = context.getRegion();

        String bucketName = getBucketName(region, env);

        if (!s3client.doesObjectExist(bucketName, prefix + "/" + fileName)) {
            log.error(String.format("S3 Configuration doesn't exist for key=%s and env = %s and region=%s.",
                                    key,
                                    env,
                                    region));
            throw new ResourceNotFoundException(String.format(
                    "S3 Configuration doesn't exist for bucket=%s and fileName=%s and env = %s and region=%s.",
                    bucketName,
                    fileName,
                    env,
                    region));
        }
        // fetch s3 object from given bucket, folder and file name
        S3Object s3Object = s3client.getObject(bucketName,
                                               prefix + "/" + fileName);
        try {
            JsonNode jsonNode = mapper.readTree(s3Object.getObjectContent());
            // get config value of provided key only
            String configString = Json.getStringValue(jsonNode.get(key));

            response = mapper.readValue(configString, type);
        } catch (IOException e) {
            log.error(String.format(
                    "Exception during loading s3 configuration for key=%s and env = %s and region=%s.",
                    key,
                    env,
                    region), e);
            throw new JSONException(String.format(
                    "Exception during loading s3 configuration for key=%s and env = %s and region=%s.",
                    key,
                    env,
                    region), e);
        }
        return response;
    }

    /**
     * Return s3 bucket name
     * @param region the region
     * @param env the environment
     * @return bucket name
     */
    private String getBucketName(String region, String env) {
        return String.format("mercury-config.%s.bronte.pearson" + "%s.tech", region, env);
    }
}
