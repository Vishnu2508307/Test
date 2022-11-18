package com.smartsparrow.rtm.message.event.courseware;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.learner.redirect.LearnerRedirectType;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerRedirectService;
import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class UpdateProductLearnerRedirectEventPublisherTest {

    @InjectMocks
    private UpdateProductLearnerRedirectEventPublisher publisher;

    @Mock
    private LearnerRedirectService learnerRedirectService;

    @Mock
    private CohortService cohortService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private CohortBroadcastMessage message;

    @Mock
    private RTMClient rtmClient;

    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID redirectId = UUID.randomUUID();
    private static final String productId = "ABCD1234";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getCohortId()).thenReturn(cohortId);

        when(cohortService.updateLearnerRedirectId(any(UUID.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerRedirectService.delete(redirectId))
                .thenReturn(Mono.just(new LearnerRedirect()));

    }

    @Test
    void publish_noSettingsFound() {
        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.empty());

        publisher.publish(rtmClient, message);

        verify(learnerRedirectService, never())
                .create(any(LearnerRedirectType.class), anyString(), anyString());
        verify(cohortService, never()).updateLearnerRedirectId(any(UUID.class), any(UUID.class));
        verify(learnerRedirectService, never()).delete(any(UUID.class));
    }

    @Test
    void publish_noProductIdInSettings_noRedirectId() {
        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()));

        publisher.publish(rtmClient, message);

        verify(learnerRedirectService, never())
                .create(any(LearnerRedirectType.class), anyString(), anyString());
        verify(cohortService).fetchCohortSettings(cohortId);
        verify(cohortService, never()).updateLearnerRedirectId(any(UUID.class), any(UUID.class));
        verify(learnerRedirectService, never()).delete(any(UUID.class));
    }

    @Test
    void publish_noProductIdInSettings_withRedirectId() {
        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()
                        .setLearnerRedirectId(redirectId)));

        publisher.publish(rtmClient, message);

        verify(learnerRedirectService, never())
                .create(any(LearnerRedirectType.class), anyString(), anyString());
        verify(cohortService).fetchCohortSettings(cohortId);
        verify(cohortService, never()).updateLearnerRedirectId(any(UUID.class), any(UUID.class));
        verify(learnerRedirectService).delete(redirectId);
    }

    @Test
    void publish_moreThan1Deployment() {
        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()
                        .setProductId(productId)));

        when(deploymentService.findDeployments(cohortId))
                .thenReturn(Flux.just(new DeployedActivity(), new DeployedActivity()));

        publisher.publish(rtmClient, message);

        verify(learnerRedirectService, never())
                .create(any(LearnerRedirectType.class), anyString(), anyString());
        verify(deploymentService).findDeployments(cohortId);
        verify(cohortService).fetchCohortSettings(cohortId);
        verify(cohortService, never()).updateLearnerRedirectId(any(UUID.class), any(UUID.class));
    }

    @Test
    void publish_redirectExists() {
        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()
                        .setProductId(productId)
                        .setLearnerRedirectId(redirectId)));

        when(deploymentService.findDeployments(cohortId))
                .thenReturn(Flux.just(new DeployedActivity()
                        .setId(deploymentId)
                        .setCohortId(cohortId)));

        when(learnerRedirectService.update(any(UUID.class), any(LearnerRedirectType.class),
                anyString(), anyString())).thenReturn(Mono.just(new LearnerRedirect()));

        publisher.publish(rtmClient, message);

        verify(learnerRedirectService, never())
                .create(any(LearnerRedirectType.class), anyString(), anyString());

        verify(deploymentService).findDeployments(cohortId);
        verify(cohortService).fetchCohortSettings(cohortId);
        verify(learnerRedirectService, never())
                .create(any(LearnerRedirectType.class), anyString(), anyString());
        verify(cohortService, never()).updateLearnerRedirectId(any(UUID.class), any(UUID.class));
        verify(learnerRedirectService).update(redirectId, LearnerRedirectType.PRODUCT, productId,
                String.format("/%s/%s", cohortId, deploymentId));
        verify(learnerRedirectService).delete(redirectId);
    }

    @Test
    void publish_createsRedirect() {

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);

        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()
                        .setProductId(productId)));

        when(deploymentService.findDeployments(cohortId))
                .thenReturn(Flux.just(new DeployedActivity()
                        .setId(deploymentId)
                        .setCohortId(cohortId)));

        when(learnerRedirectService.fetch(LearnerRedirectType.PRODUCT, productId))
                .thenReturn(Mono.empty());

        when(learnerRedirectService
                .create(LearnerRedirectType.PRODUCT, productId, String.format("/%s/%s", cohortId, deploymentId)))
                .thenReturn(Mono.just(new LearnerRedirect()
                        .setVersion(UUID.randomUUID())
                        .setType(LearnerRedirectType.PRODUCT)
                        .setKey(productId)
                        .setDestinationPath(String.format("/%s/%s", cohortId, deploymentId))
                        .setId(redirectId)));

        publisher.publish(rtmClient, message);

        verify(learnerRedirectService)
                .create(eq(LearnerRedirectType.PRODUCT), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());

        verify(deploymentService).findDeployments(cohortId);
        verify(cohortService).fetchCohortSettings(cohortId);
        verify(cohortService).updateLearnerRedirectId(eq(cohortId), uuidArgumentCaptor.capture());
        verify(learnerRedirectService, never()).update(any(UUID.class), any(LearnerRedirectType.class),
                anyString(), anyString());
        verify(learnerRedirectService, never()).delete(redirectId);
        List<String> args = stringArgumentCaptor.getAllValues();

        assertAll(() -> {
            UUID capturedRedirectId = uuidArgumentCaptor.getValue();
            assertNotNull(capturedRedirectId);
            assertEquals(redirectId, capturedRedirectId);
            assertNotNull(args);
            assertEquals(2, args.size());

            final String key = args.get(0);
            assertNotNull(key);
            assertEquals(productId, key);

            final String destination = args.get(1);
            assertNotNull(destination);
            assertEquals(String.format("/%s/%s", cohortId, deploymentId), destination);
        });
    }
}