package com.smartsparrow.rest.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.exception.UnprocessableEntityException;
import com.smartsparrow.iam.service.DeveloperKey;
import com.smartsparrow.iam.service.DeveloperKeyService;
import com.smartsparrow.plugin.lang.PluginPublishException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.plugin.service.PublishedPlugin;
import com.smartsparrow.rest.resource.r.PluginResource;

import reactor.core.publisher.Mono;

class PluginResourceTest {
    @Mock
    private PluginService pluginService;
    @Mock
    private DeveloperKeyService developerKeyService;
    private PluginResource pluginResource;
    private InputStream inputStream;
    private FormDataContentDisposition formDataContentDisposition;
    private String developerKey;
    private String invalidDevKey;
    private String fileName;
    private static final UUID publisherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        pluginResource = new PluginResource(pluginService, developerKeyService);

        inputStream = mock(InputStream.class);
        formDataContentDisposition = mock(FormDataContentDisposition.class);
        developerKey = "someDevKey";
        invalidDevKey = "invalid";
        fileName = "file.zip";
        Mono<DeveloperKey> devKey = Mono.just(new DeveloperKey().setAccountId(publisherId));

        when(formDataContentDisposition.getFileName()).thenReturn(fileName);
        when(developerKeyService.fetchByValue(developerKey)).thenReturn(devKey);
        when(developerKeyService.fetchByValue(invalidDevKey)).thenReturn(Mono.empty());
    }

    @Test
    void publish_nullFile() {
        assertThrows(IllegalArgumentFault.class, () -> {
            pluginResource.publish(null, null, developerKey, null);
        });
    }

    @Test
    void publish_nullDevKey() {
        assertThrows(IllegalArgumentFault.class, () -> {
            pluginResource.publish(inputStream, formDataContentDisposition, null, null);
        });
    }

    @Test
    void publish_invalidDevKey() {
        assertThrows(NotAuthorizedException.class, () -> {
            pluginResource.publish(inputStream, formDataContentDisposition, invalidDevKey, null);
        });
    }

    @Test
    void publish_anyInvalidArgument() throws PluginPublishException {
        doThrow(IllegalArgumentException.class).when(pluginService).publish(inputStream, fileName, publisherId, null);
        assertThrows(BadRequestException.class, () -> {
            pluginResource.publish(inputStream, formDataContentDisposition, developerKey, null);
        });
    }

    @Test
    void publish_anyPublishException() throws PluginPublishException {
        doThrow(PluginPublishException.class).when(pluginService).publish(inputStream, fileName, publisherId, null);
        assertThrows(UnprocessableEntityException.class, () -> {
            pluginResource.publish(inputStream, formDataContentDisposition, developerKey, null);
        });
    }

    @Test
    void publish() throws PluginPublishException, NotAuthorizedException, BadRequestException,
            UnprocessableEntityException {
        when(pluginService.publish(inputStream, fileName, publisherId, null)).thenReturn(mock(PublishedPlugin.class));
        Response response = pluginResource.publish(inputStream, formDataContentDisposition, developerKey, null);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        assertTrue(entity instanceof PublishedPlugin);
    }
}
