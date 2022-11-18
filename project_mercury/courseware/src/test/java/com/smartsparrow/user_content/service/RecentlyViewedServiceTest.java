package com.smartsparrow.user_content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.data.RecentlyViewedGateway;
import com.smartsparrow.user_content.data.RecentlyViewed;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;

class RecentlyViewedServiceTest {

    @InjectMocks
    private RecentlyViewedService recentlyViewedService;

    @Mock
    private RecentlyViewedGateway recentlyViewedGateway;

    final UUID activityId = UUIDs.timeBased();
    final UUID workspaceId = UUIDs.timeBased();
    final UUID accountId = UUIDs.timeBased();
    final UUID projectId = UUIDs.timeBased();
    final UUID rootElementId = UUIDs.timeBased();
    final UUID documentId = UUIDs.timeBased();
    final ResourceType resourceType = ResourceType.COURSE;
    private static final String RECENTLY_VIEWED_KEY = "RECENTLY_VIEWED";

    RecentlyViewed recentlyViewed = new RecentlyViewed();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        recentlyViewed.setAccountId(accountId)
                .setActivityId(activityId)
                .setDocumentId(documentId)
                .setWorkspaceId(workspaceId)
                .setResourceType(resourceType)
                .setRootElementId(rootElementId)
                .setResourceType(resourceType);
    }


    @Test
    void validateAndCreate() {
        ArgumentCaptor<RecentlyViewed> captor = ArgumentCaptor.forClass(RecentlyViewed.class);
        Mockito.when(recentlyViewedGateway.persist(any())).thenReturn(Flux.just(new Void[]{}));
        recentlyViewedService.create(activityId,
                                             workspaceId,
                                             accountId,
                                             projectId,
                                             rootElementId,
                                             documentId,
                                             resourceType).block();
        verify(recentlyViewedGateway).persist(captor.capture());
        List<RecentlyViewed> all = captor.getAllValues();
        assertNotNull(all);
        assertEquals(1, all.size());
    }

    @Test
    void validateAndCreateRecentlyViewed_verifyAccountId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> recentlyViewedService.create(activityId,
                                                                                                                     workspaceId,
                                                                                                                     null,
                                                                                                                     projectId,
                                                                                                                     rootElementId,
                                                                                                                     documentId,
                                                                                                                     resourceType)
                .block());
        assertNotNull(f);
        assertEquals("accountId is required", f.getMessage());
    }

    @Test
    void validateAndCreateRecentlyViewed_verifyWorkspaceId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> recentlyViewedService.create(activityId,
                                                                                                                     null,
                                                                                                                     accountId,
                                                                                                                     projectId,
                                                                                                                     rootElementId,
                                                                                                                     documentId,
                                                                                                                     resourceType)
                .block());
        assertNotNull(f);
        assertEquals("workspaceId is required", f.getMessage());
    }
    @Test
    void validateAndCreateRecentlyViewed_verifyRootElementId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> recentlyViewedService.create(activityId,
                                                                                                                     workspaceId,
                                                                                                                     accountId,
                                                                                                                     projectId,
                                                                                                                     null,
                                                                                                                     documentId,
                                                                                                                     resourceType)
                .block());
        assertNotNull(f);
        assertEquals("rootElementId is required", f.getMessage());
    }
    @Test
    void validateAndCreateRecentlyViewed_verifyResourceType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> recentlyViewedService.create(activityId,
                                                                                                                     workspaceId,
                                                                                                                     accountId,
                                                                                                                     projectId,
                                                                                                                     rootElementId,
                                                                                                                     documentId,
                                                                                                                     null)
                .block());
        assertNotNull(f);
        assertEquals("resourceType is required", f.getMessage());
    }

    @Test
    void validateAndCreateRecentlyViewed_verifyActivityIdForResourceLesson() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> recentlyViewedService.create(null,
                                                                                                             workspaceId,
                                                                                                             accountId,
                                                                                                             projectId,
                                                                                                             rootElementId,
                                                                                                             documentId,
                                                                                                             ResourceType.LESSON)
                .block());
        assertNotNull(f);
        assertEquals("activityId is required", f.getMessage());
    }
}