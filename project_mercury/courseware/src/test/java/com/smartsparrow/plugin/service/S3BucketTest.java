package com.smartsparrow.plugin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Provider;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.lang.S3BucketUploadException;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.plugin.wiring.SchemaConfig;

class S3BucketTest {

    private S3Bucket s3bucket;
    @Mock
    private Provider<PluginConfig> pluginConfigProvider;
    @Mock
    private Provider<SchemaConfig> schemaConfigProvider;
    @Mock
    private AmazonS3 s3Client;

    private static final UUID pluginId = UUID.fromString("5e796d58-072f-4ca2-acfd-9aae1bbf11ce");
    private static final String version = "1.0.0";
    private static final String zipHash = "e3b0c44298fc1c149afbf4c";
    private static final String DISTRIBUTION_BUCKET = "distribution";
    private static final String REPOSITORY_BUCKET = "repository";
    private static final File zip = mock(File.class);

    private void setUp(PluginConfig config) {
        MockitoAnnotations.openMocks(this);
        when(pluginConfigProvider.get()).thenReturn(config);
        s3bucket = new S3Bucket(pluginConfigProvider, schemaConfigProvider, s3Client);
    }

    @Test
    void uploadPlugin_NoPluginManifest() {
        setUp(null);

        assertThrows(NullPointerException.class, () -> s3bucket.uploadPlugin(null, zip, new HashMap<>()));
    }

    @Test
    void uploadPlugin_NoPluginConfig() {
        setUp(null);

        assertThrows(S3BucketUploadException.class, () -> s3bucket.uploadPlugin(new PluginManifest(), zip, new HashMap<>()));
    }

    @Test
    void uploadPlugin_UploadException() {
        PluginConfig config = new PluginConfig();
        config.setDistributionBucketName(DISTRIBUTION_BUCKET);
        config.setRepositoryBucketName(REPOSITORY_BUCKET);
        setUp(config);

        PluginManifest manifest = new PluginManifest().setPluginId(pluginId).setVersion(version).setZipHash(zipHash);

        when(s3Client.putObject(any())).thenThrow(AmazonClientException.class);

        assertThrows(S3BucketUploadException.class, () -> s3bucket.uploadPlugin(manifest, zip, new HashMap<>()));
    }

    @Test
    void uploadPlugin() throws S3BucketUploadException {
        PluginConfig config = new PluginConfig();
        config.setDistributionBucketName(DISTRIBUTION_BUCKET);
        config.setRepositoryBucketName(REPOSITORY_BUCKET);
        setUp(config);

        File file1 = mock(File.class);
        File file2 = mock(File.class);

        Map<String, File> files = new HashMap<>(2);
        files.put("file1", file1);
        files.put("file2", file2);

        PluginManifest manifest = new PluginManifest().setPluginId(pluginId).setVersion(version).setZipHash(zipHash);

        s3bucket.uploadPlugin(manifest, zip, files);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(3)).putObject(captor.capture());

        List<PutObjectRequest> reqs = captor.getAllValues();
        assertEquals(REPOSITORY_BUCKET, reqs.get(0).getBucketName());
        assertEquals(pluginId + "/" + version + "/" + zipHash + ".zip", reqs.get(0).getKey());
        assertEquals(zip, reqs.get(0).getFile());

        if (reqs.get(1).getKey().contains("file1")) {
            assertEquals(DISTRIBUTION_BUCKET, reqs.get(1).getBucketName());
            assertEquals(pluginId + "/" + zipHash + "/file1", reqs.get(1).getKey());
            assertEquals(file1, reqs.get(1).getFile());

            assertEquals(DISTRIBUTION_BUCKET, reqs.get(2).getBucketName());
            assertEquals(pluginId + "/" + zipHash + "/file2", reqs.get(2).getKey());
            assertEquals(file2, reqs.get(2).getFile());
        } else {
            assertEquals(DISTRIBUTION_BUCKET, reqs.get(1).getBucketName());
            assertEquals(pluginId + "/" + zipHash + "/file2", reqs.get(1).getKey());
            assertEquals(file2, reqs.get(1).getFile());

            assertEquals(DISTRIBUTION_BUCKET, reqs.get(2).getBucketName());
            assertEquals(pluginId + "/" + zipHash + "/file1", reqs.get(2).getKey());
            assertEquals(file1, reqs.get(2).getFile());
        }
    }

}
