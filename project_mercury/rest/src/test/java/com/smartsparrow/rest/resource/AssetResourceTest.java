package com.smartsparrow.rest.resource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.lang.AssetUploadException;
import com.smartsparrow.asset.lang.AssetUploadValidationException;
import com.smartsparrow.asset.lang.UnsupportedAssetException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetUploadService;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.exception.InternalServerError;
import com.smartsparrow.exception.UnprocessableEntityException;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rest.resource.r.AssetResource;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AssetResourceTest {

    @InjectMocks
    private AssetResource assetResource;

    @Mock
    private FormDataContentDisposition contentDisposition;

    @Mock
    private FileInputStream fileInputStream;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private AssetUploadService assetUploadService;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final Map<String, String> metadata = new HashMap<String, String>(){
        {put("foo","bar");}
    };

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(context.getAccount()).thenReturn(account);
        when(authenticationContextProvider.get()).thenReturn(context);

        when(contentDisposition.getFileName()).thenReturn("TestFileName.extension");
    }

    @Test
    void save_nullFileInputStream() {
        BadRequestException e = assertThrows(BadRequestException.class,
                ()-> assetResource.save(null, contentDisposition, AssetVisibility.GLOBAL, subscriptionId,
                        metadata));

        assertEquals("File is required", e.getMessage());
    }

    @Test
    void save_nullAssetVisibility() {
        BadRequestException e = assertThrows(BadRequestException.class,
                ()-> assetResource.save(fileInputStream, contentDisposition, null, subscriptionId,
                        metadata));

        assertEquals("asset visibility param is required", e.getMessage());
    }

    @Test
    void save_nullSubscriptionId() {
        BadRequestException e = assertThrows(BadRequestException.class,
                ()-> assetResource.save(fileInputStream, contentDisposition, AssetVisibility.GLOBAL, null,
                        metadata));

        assertEquals("subscriptionId is required", e.getMessage());
    }

    @Test
    void save_saveUnsupportedAsset() {
        TestPublisher<AssetPayload> publisher = TestPublisher.create();
        publisher.error(new UnsupportedAssetException("face_palm"));

        when(assetUploadService.save(any(AssetTemplate.class))).thenReturn(publisher.mono());

        BadRequestException e = assertThrows(BadRequestException.class,
                ()-> assetResource.save(fileInputStream, contentDisposition, AssetVisibility.SUBSCRIPTION,
                        subscriptionId, metadata));

        assertEquals("face_palm", e.getMessage());

        verify(assetUploadService).save(any(AssetTemplate.class));
    }

    @Test
    void save_validationException() {
        TestPublisher<AssetPayload> publisher = TestPublisher.create();
        publisher.error(new AssetUploadValidationException("face_palm"));

        when(assetUploadService.save(any(AssetTemplate.class))).thenReturn(publisher.mono());

        BadRequestException e = assertThrows(BadRequestException.class,
                ()-> assetResource.save(fileInputStream, contentDisposition, AssetVisibility.SUBSCRIPTION,
                        subscriptionId, metadata));

        assertEquals("face_palm", e.getMessage());

        verify(assetUploadService).save(any(AssetTemplate.class));
    }

    @Test
    void save_assetUploadException() {
        TestPublisher<AssetPayload> publisher = TestPublisher.create();
        publisher.error(new AssetUploadException("face_palm"));

        when(assetUploadService.save(any(AssetTemplate.class))).thenReturn(publisher.mono());

        UnprocessableEntityException e = assertThrows(UnprocessableEntityException.class,
                ()-> assetResource.save(fileInputStream, contentDisposition, AssetVisibility.SUBSCRIPTION,
                        subscriptionId, metadata));

        assertEquals("face_palm", e.getMessage());

        verify(assetUploadService).save(any(AssetTemplate.class));
    }

    @Test
    void save_anyOtherException() {
        TestPublisher<AssetPayload> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("mvagusta"));

        when(assetUploadService.save(any(AssetTemplate.class))).thenReturn(publisher.mono());

        InternalServerError e = assertThrows(InternalServerError.class,
                ()-> assetResource.save(fileInputStream, contentDisposition, AssetVisibility.SUBSCRIPTION,
                        subscriptionId, metadata));

        assertEquals("An internal server error has occurred", e.getMessage());

        verify(assetUploadService).save(any(AssetTemplate.class));
    }

    @Test
    void save_success() {
        ArgumentCaptor<AssetTemplate> captor = ArgumentCaptor.forClass(AssetTemplate.class);

        when(assetUploadService.save(any(AssetTemplate.class))).thenReturn(Mono.just(new AssetPayload()));

        Response response = assetResource.save(fileInputStream, contentDisposition, AssetVisibility.SUBSCRIPTION,
                subscriptionId, metadata);

        verify(assetUploadService).save(captor.capture());

        AssetTemplate template = captor.getValue();

        assertNotNull(template);

        assertAll(()->{
            assertEquals(fileInputStream, template.getInputStream());
            assertEquals(".extension", template.getFileExtension());
            assertEquals(AssetProvider.AERO, template.getProvider());
            assertEquals("TestFileName.extension", template.getOriginalFileName());
            assertEquals(accountId, template.getOwnerId());
            assertEquals(subscriptionId, template.getSubscriptionId());
            assertEquals(AssetVisibility.SUBSCRIPTION, template.getVisibility());
        });

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
