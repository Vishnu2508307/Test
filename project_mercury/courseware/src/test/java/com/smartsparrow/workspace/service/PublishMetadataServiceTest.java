package com.smartsparrow.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import static com.smartsparrow.data.Headers.PI_AUTHORIZATION_HEADER;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.pearson.autobahn.common.exception.AutobahnIdentityProviderException;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.NotificationState;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.data.PublicationSettings;

import reactor.core.publisher.Mono;

public class PublishMetadataServiceTest {

    @InjectMocks
    private PublishMetadataService publishMetadataService;

    @Mock
    private ExternalHttpRequestService externalHttpRequestService;

    @Mock
    private IesSystemToSystemIdentityProvider iesSystemToSystemIdentityProvider;

    private static final String productId = UUID.randomUUID().toString();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final String labId = "ondemand/product/" + productId;
    private static final String title = "My iLab";
    private static final String description = "This is my iLab";
    private static final String discipline = "MasteringBiology";
    private static final String estimatedTime = "1.5 Hours";
    private static final String previewUrl = "" + cohortId + "/" + deploymentId;
    private static final int status = 1;
    private static final UUID referenceId = UUID.randomUUID();
    private static final String token = "piToken";

    private static String mxServiceUrl = "https://mastering/lab/details";
    private static final PublicationSettings publicationSettings = new PublicationSettings()
            .setLabId(labId)
            .setTitle(title)
            .setDescription(description)
            .setDiscipline(discipline)
            .setEstimatedTime(estimatedTime)
            .setPreviewUrl(previewUrl)
            .setStatus(status);

    private static UUID notificationId = UUID.randomUUID();
    private static UUID notificationRefId = UUID.randomUUID();
    private static NotificationState notificationState = new NotificationState().setNotificationId(notificationId).setReferenceId(notificationRefId);
    private static RequestNotification requestNotification = new RequestNotification().setState(notificationState);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(externalHttpRequestService.submit(eq(RequestPurpose.PUBLISH_METADATA), any(Request.class), eq(referenceId)))
                .thenReturn(Mono.just(requestNotification));

        try {
            when(iesSystemToSystemIdentityProvider.getPiToken()).thenReturn(token);
        } catch (UnsupportedEncodingException | AutobahnIdentityProviderException ex) {
            throw new RuntimeException("PublishMetadata Service - Unable to set IES system token");
        }
    }

    @Test
    void publish() {
        final String expected = "{" +
                "\"uri\":\"" + mxServiceUrl + "\"," +
                "\"method\":\"POST\"," +
                "\"headers\":{\"" + PI_AUTHORIZATION_HEADER + "\":\"" + token + "\",\"Content-Type\":\"application/json\"}," +
                "\"body\":\"{" +
                    "\\\"labId\\\":\\\"" + labId + "\\\"" +
                    ",\\\"title\\\":\\\"" + title + "\\\"" +
                    ",\\\"description\\\":\\\"" + description + "\\\"" +
                    ",\\\"discipline\\\":\\\"" + discipline + "\\\"" +
                    ",\\\"estimatedTime\\\":\\\"" + estimatedTime + "\\\"" +
                    ",\\\"previewUrl\\\":\\\"" + previewUrl + "\\\"" +
                    ",\\\"status\\\":" + status +
                    "}\"" +
                "}";

        ArgumentCaptor<Request> requestArgumentCaptor = ArgumentCaptor.forClass(Request.class);

        try (MockedStatic<UUIDs> mockedUUIDs = Mockito.mockStatic(com.smartsparrow.util.UUIDs.class)) {
            // this is control the newCohortId generated in LTIv11Resource:fetchOnDemandCohortId(...)
            mockedUUIDs.when(com.smartsparrow.util.UUIDs::timeBased).thenReturn(referenceId);
            final RequestNotification notification = publishMetadataService.publish(mxServiceUrl, publicationSettings)
                    .block();

            assertNotNull(notification);
        }

        verify(externalHttpRequestService).submit(eq(RequestPurpose.PUBLISH_METADATA), requestArgumentCaptor.capture(),
                                                  eq(referenceId));

        final Request request = requestArgumentCaptor.getValue();

        assertNotNull(request);
        assertEquals(expected, request.getParamsAsJson());
    }
}
