package com.smartsparrow.user_content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.smartsparrow.user_content.data.SharedResource;
import com.smartsparrow.user_content.data.SharedResourceGateway;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;

class SharedResourceServiceTest {

    @InjectMocks
    private SharedResourceService sharedResourceService;

    @Mock
    private SharedResourceGateway sharedResourceGateway;

    final UUID sharedAccountId = UUIDs.timeBased();
    final UUID accountId = UUIDs.timeBased();
    final UUID resourceId = UUIDs.timeBased();
    final ResourceType resourceType = ResourceType.COURSE;

    SharedResource sharedResource = new SharedResource();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sharedResource.setAccountId(accountId)
                .setResourceType(resourceType)
                .setSharedAccountId(UUIDs.random())
                .setSharedAt(UUIDs.random());
    }


    @Test
    void validateAndCreateSharedResource() {
        ArgumentCaptor<SharedResource> captor = ArgumentCaptor.forClass(SharedResource.class);
        Mockito.when(sharedResourceGateway.persist(any())).thenReturn(Flux.just(new Void[]{}));
        sharedResourceService.create(resourceId,
                                     sharedAccountId,
                                     accountId,
                                     resourceType).block();
        verify(sharedResourceGateway).persist(captor.capture());
        List<SharedResource> all = captor.getAllValues();
        assertNotNull(all);
        assertEquals(1, all.size());
    }

    @Test
    void validateAndSharedResource_verifyAccountId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> sharedResourceService.create(resourceId,
                                                                                                             sharedAccountId,
                                                                                                             null,
                                                                                                             resourceType)
                .block());
        assertNotNull(f);
        assertEquals("accountId is required", f.getMessage());
    }

    @Test
    void validateAndSharedResource_verifySharedAccountId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> sharedResourceService.create(resourceId,
                                                                                                             null,
                                                                                                             accountId,
                                                                                                             resourceType)
                .block());
        assertNotNull(f);
        assertEquals("sharedAccountId is required", f.getMessage());
    }

    @Test
    void validateAndSharedResource_verifyResourceType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> sharedResourceService.create(resourceId,
                                                                                                             sharedAccountId,
                                                                                                             accountId,
                                                                                                             null)
                .block());
        assertNotNull(f);
        assertEquals("resourceType is required", f.getMessage());
    }
}