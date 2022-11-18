package com.smartsparrow.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.entity.ContentType;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.lang.S3ReadFault;
import com.smartsparrow.lang.S3SignUrlFault;
import com.smartsparrow.lang.S3UploadFault;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class S3ClientService {

    private static final long SIGNED_URL_TIMEOUT_MINUTES = 15;
    private static final long SIGNED_URL_TIMEOUT_MILLISECONDS = 1000 * 60 * SIGNED_URL_TIMEOUT_MINUTES;

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(S3ClientService.class);

    private static final ObjectMapper om = new ObjectMapper();
    private final AmazonS3 s3Client;

    @Inject
    public S3ClientService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload a file to an s3 bucket
     *
     * @param bucketName the bucket name
     * @param key        represents the path within the bucket
     * @param file       the file to upload
     * @return the result of the upload operation
     * @throws S3UploadFault when failing to upload
     */
    public PutObjectResult upload(@Nonnull final String bucketName, @Nonnull final String key, @Nonnull final File file) {
        try {
            return s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        } catch (Throwable throwable) {
            log.jsonError("error uploading the file", new HashMap<String, Object>() {
                {put("bucketName", bucketName);}
                {put("key", key);}
            }, throwable);
            throw new S3UploadFault(throwable.getMessage());
        }
    }

    /**
     * Upload a string to an s3 bucket
     *
     * @param bucketName  the bucket name
     * @param key         represents the path within the bucket
     * @param fileName    the name to give to the file
     * @param content     the file content
     * @param contentType the file contentType
     * @return the upload operation result
     * @throws S3UploadFault when failing to upload
     */
    public PutObjectResult upload(@Nonnull final String bucketName, @Nonnull final String key,
                                  @Nonnull final String fileName, @Nonnull final String content,
                                  @Nonnull final ContentType contentType) {
        try {
            byte[] fileContentBytes = content.getBytes(StandardCharsets.UTF_8);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContentBytes);
            final ObjectMetadata objectMetadata = new ObjectMetadata();
            final String path = String.format("%s/%s", key, fileName);
            objectMetadata.setContentType(contentType.toString());
            objectMetadata.setContentLength(fileContentBytes.length);
            return s3Client.putObject(new com.amazonaws.services.s3.model.PutObjectRequest(bucketName, path, inputStream, objectMetadata));
        } catch (Throwable throwable) {
            log.jsonError("error uploading the content", new HashMap<String, Object>() {
                {put("bucketName", bucketName);}
                {put("key", key);}
            }, throwable);
            throw new S3UploadFault(throwable.getMessage());
        }
    }

    /**
     * Pre-sign a url to upload an object to an s3 bucket
     *
     * @param bucketName the bucket name
     * @param key        represents the path within the bucket
     * @return the signed url
     * @throws S3SignUrlFault when failing to create the signed url
     */
    public URL signUrl(@Nonnull final String bucketName, @Nonnull final String key) {
        try {
            Date expiration = new Date();
            // Set the presigned URL to expire after 15 minutes.
            long expTimeMillis = Instant.now().toEpochMilli();
            expTimeMillis += SIGNED_URL_TIMEOUT_MILLISECONDS;
            expiration.setTime(expTimeMillis);

            return s3Client.generatePresignedUrl(bucketName, key, expiration, HttpMethod.PUT);
        } catch (Throwable throwable) {
            log.jsonError("error creating signed url", new HashMap<String, Object>() {
                {put("bucketName", bucketName);}
                {put("key", key);}
            }, throwable);
            throw new S3SignUrlFault(throwable.getMessage());
        }
    }

    /**
     * Read a s3 object [file] and return its content as a string
     *
     * @param bucketName the bucket name the s3 object belongs to
     * @param key        the location in the bucket
     * @return a string representing the content of the s3 object
     * @throws S3ReadFault        when failing to read the content as string
     * @throws SdkClientException when failing to read the file
     */
    public String read(@Nonnull final String bucketName, @Nonnull final String key) {
        try {
            log.jsonDebug("Reading file from S3", new HashMap<String, Object>() {
                { put("bucketName", bucketName); }
                { put("key", key); }
            });
            return s3Client.getObjectAsString(bucketName, key);
        } catch (Exception e) {
            log.jsonError("error reading from s3", new HashMap<String, Object>() {
                {put("bucketName", bucketName);}
                {put("key", key);}
                {put("exception", e.getMessage());}
            }, e);
            throw new S3ReadFault("error reading the s3 object");
        }
    }

    /**
     * List all the keys in a given s3 bucket filtered by export ID as prefix.
     *
     * @param bucketName
     * @param exportId
     * @return
     */
    public List<String> listKeys(@Nonnull final String bucketName, @Nonnull final String exportId, String endsWith) {
        try {
            log.jsonInfo("Listing keys from S3", new HashMap<String, Object>() {
                {put("bucketName", bucketName);}
                {put("exportId", exportId);}
                {put("endsWith", endsWith);}
            });
            ObjectListing listings = s3Client.listObjects(bucketName, exportId);
            List<String> keyList = new ArrayList<>();
            while (true) {
                keyList.addAll(listings
                                       .getObjectSummaries()
                                       .stream()
                                       .map(S3ObjectSummary::getKey)
                                       .filter(fileName -> {
                                           if(endsWith != null){
                                               return fileName.endsWith(endsWith);
                                           }
                                           return true;
                                       })
                                       .collect(Collectors.toList())
                );

                if (listings.isTruncated()) {
                    log.jsonInfo("Listing next batch of keys from S3", new HashMap<String, Object>() {
                        {put("bucketName", bucketName);}
                        {put("exportId", exportId);}
                        {put("endsWith", endsWith);}
                    });
                    listings = s3Client.listNextBatchOfObjects(listings);
                } else {
                    break;
                }
            }
            return keyList;
        } catch (Exception ex) {
            log.jsonError("Failed to get list of keys from S3", new HashMap<String, Object>() {
                {put("bucketName", bucketName);}
                {put("exportId", exportId);}
                {put("endsWith", endsWith);}
            }, ex);
            throw ex;
        }
    }
}
