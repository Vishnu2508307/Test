package com.smartsparrow.plugin.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.inject.Provider;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.lang.S3BucketLoadFileException;
import com.smartsparrow.plugin.lang.S3BucketUploadException;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.plugin.wiring.SchemaConfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class S3Bucket {

    private static final Logger logger = LoggerFactory.getLogger(S3Bucket.class);
    // This is 1 year in seconds.
    private static final long PLUGIN_TTL = 31536000;

    private final PluginConfig pluginConfig;
    private final SchemaConfig schemaConfig;
    private final AmazonS3 s3client;

    @Inject
    public S3Bucket(Provider<PluginConfig> pluginConfigProvider,
                    Provider<SchemaConfig> schemaConfigProvider,
                    AmazonS3 s3client) {
        this.pluginConfig = pluginConfigProvider.get();
        this.schemaConfig = schemaConfigProvider.get();
        this.s3client = s3client;
    }

    /**
     * Uploads plugin files to S3 buckets.

     * @param pluginManifest the plugin manifest
     * @param zip zip file
     * @param entries map of all files from zip: keys are file names, values are files. Note: only files, directories are excluded
     * @throws S3BucketUploadException if plugin config is undefined, or exceptions occur during uploading
     * @throws NullPointerException if plugin manifest is null
     */
    public void uploadPlugin(PluginManifest pluginManifest, File zip, Map<String, File> entries)
            throws S3BucketUploadException {
        checkNotNull(pluginManifest);
        if (pluginConfig == null) {
            throw new S3BucketUploadException("Plugin configuration is empty");
        }

        String zipPath = pluginManifest.getBuildZipPath(pluginManifest);
        upload(pluginConfig.getRepositoryBucketName(), zipPath, zip);

        String path;
        for (Map.Entry<String, File> fileEntry : entries.entrySet()) {
            path = pluginManifest.getBuildFilePath(pluginManifest, fileEntry.getKey());
            uploadWithTTL(pluginConfig.getDistributionBucketName(), path, fileEntry.getValue());
        }
    }

    /**
     * Load the file content from the s3 schema bucket
     *
     * @param fileName the name of the schema file to open
     * @return the file content as a string
     * @throws S3BucketLoadFileException when the schema configuration is empty or when failing to read the file content
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "JDK 11 changes broke this rule")
    // https://github.com/spotbugs/spotbugs/issues/756
    public String loadSchemaFile(String fileName) throws S3BucketLoadFileException {
        if (schemaConfig == null) {
            throw new S3BucketLoadFileException("'schemas' configuration is empty");
        }

        S3Object object = s3client.getObject(schemaConfig.getBucketUrl(), fileName);
        InputStream inputStream = object.getObjectContent();

        try {
            Path tempFile = java.nio.file.Files.createTempFile(null, null);
            File file = tempFile.toFile();
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            try (Stream<String> lines = java.nio.file.Files.lines(Paths.get(file.getAbsolutePath()))) {
                return lines.collect(Collectors.joining());
            }
        } catch (IOException e) {
            throw new S3BucketLoadFileException("Error loading the file content", e);
        }
    }

    public void put(String bucketName, String key, File file) {
        try {
            s3client.putObject(new PutObjectRequest(bucketName, key, file));
        } catch (AmazonClientException e) {
            logger.error("error uploading object to bucket", e);
            throw e;
        }
    }

    private void upload(String bucketName, String filePath, File file) throws S3BucketUploadException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            String contentType = new Tika().detect(file.getPath());
            metadata.setContentType(contentType);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath, file);
            putObjectRequest.setMetadata(metadata);
            s3client.putObject(putObjectRequest);
            logger.debug("File is uploaded to bucket='{}' with key='{}', with content type='{}'",
                         bucketName,
                         filePath,
                         contentType);
        } catch (AmazonClientException e) {
            throw new S3BucketUploadException(String.format("Error when uploading file '%s' to bucket '%s': %s",
                                                            filePath, bucketName, e.getMessage()), e);
        }
    }

    private void uploadWithTTL(String bucketName, String filePath, File file) throws S3BucketUploadException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            String contentType = new Tika().detect(file.getPath());
            metadata.setContentType(contentType);
            metadata.setCacheControl("max-age=" + PLUGIN_TTL);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath, file);
            putObjectRequest.setMetadata(metadata);
            s3client.putObject(putObjectRequest);
            logger.debug("File is uploaded to bucket='{}' with key='{}', with content type='{}'",
                         bucketName,
                         filePath,
                         contentType);
        } catch (AmazonClientException e) {
            throw new S3BucketUploadException(String.format("Error when uploading file '%s' to bucket '%s': %s",
                                                            filePath, bucketName, e.getMessage()), e);
        }
    }

    /**
     * Retrieves text file contents from file in s3
     *
     * This is only used in a local development feature or for debug purposes.
     *  @param pluginId
     * @param hash
     * @param relativePath path of file relative to bucket/pluginId/hash/
     * @return
     */
    public String getPluginFileContent(UUID pluginId, String hash, String relativePath) throws S3BucketLoadFileException {
        try {
            String path = String.format("%s/%s/%s", pluginId, hash, relativePath).replace("//", "/");
            logger.info("Retrieving contents of {}/{}", pluginConfig.getDistributionBucketName(), path);
            return s3client.getObjectAsString(pluginConfig.getDistributionBucketName(), path);
        } catch (AmazonClientException e) {
            throw new S3BucketLoadFileException(String.format("Error retrieving entry point file %s/%s/%s - %s",
                    pluginId, hash, relativePath, e.getMessage()), e);
        }

    }

}
