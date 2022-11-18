package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.DeveloperKeyGateway;

import reactor.core.publisher.Flux;

class DeveloperKeyServiceTest {

    @Mock
    private DeveloperKeyGateway developerKeyGateway;

    private DeveloperKeyService developerKeyService;
    private final UUID subscriptionId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        developerKeyService = new DeveloperKeyService(developerKeyGateway);
    }

    @Test
    void createKey() {
        when(developerKeyGateway.persist(any(DeveloperKey.class))).thenReturn(Flux.just(new Void[]{}));
        DeveloperKey devKey = developerKeyService.createKey(subscriptionId, accountId);
        verify(developerKeyGateway, atLeastOnce()).persist(any(DeveloperKey.class));
        assertNotNull(devKey.getKey());
    }
}
