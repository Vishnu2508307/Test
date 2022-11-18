package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerRedirectGateway;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.learner.redirect.LearnerRedirectType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerRedirectServiceTest {

    @InjectMocks
    private LearnerRedirectService learnerRedirectService;

    @Mock
    private LearnerRedirectGateway learnerRedirectGateway;

    private static final String key = "key";
    private static final String destinationPath = "destinationPath";
    private static final UUID redirectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(learnerRedirectGateway.persist(any(LearnerRedirect.class)))
                .thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void create_exists() {
        when(learnerRedirectGateway.fetch(LearnerRedirectType.PRODUCT, key))
                .thenReturn(Mono.just(new LearnerRedirect()));

        ConflictFault f = assertThrows(ConflictFault.class, () -> learnerRedirectService.create(LearnerRedirectType.PRODUCT, key, destinationPath)
                .block());

        assertNotNull(f);
        assertEquals("entry already exists", f.getMessage());

        verify(learnerRedirectGateway, never()).persist(any(LearnerRedirect.class));
    }

    @Test
    void create() {

        ArgumentCaptor<LearnerRedirect> captor = ArgumentCaptor.forClass(LearnerRedirect.class);

        when(learnerRedirectGateway.fetch(LearnerRedirectType.PRODUCT, key))
                .thenReturn(Mono.empty());

        LearnerRedirect persisted = learnerRedirectService.create(LearnerRedirectType.PRODUCT, key, destinationPath)
                .block();

        verify(learnerRedirectGateway, times(1))
                .persist(captor.capture());

        LearnerRedirect captured = captor.getValue();

        assertAll(() -> {
            assertNotNull(persisted);
            assertNotNull(persisted.getId());
            assertNotNull(persisted.getVersion());
            assertEquals(key, persisted.getKey());
            assertEquals(LearnerRedirectType.PRODUCT, persisted.getType());
            assertEquals(destinationPath, persisted.getDestinationPath());
        });

        assertAll(() -> {
            assertNotNull(captured);
            assertNotNull(captured.getId());
            assertNotNull(captured.getVersion());
            assertEquals(key, captured.getKey());
            assertEquals(LearnerRedirectType.PRODUCT, captured.getType());
            assertEquals(destinationPath, captured.getDestinationPath());
        });
    }

    @Test
    void update() {
        ArgumentCaptor<LearnerRedirect> captor = ArgumentCaptor.forClass(LearnerRedirect.class);

        LearnerRedirect persisted = learnerRedirectService.update(redirectId, LearnerRedirectType.PRODUCT, key, destinationPath)
                .block();

        verify(learnerRedirectGateway, times(1))
                .persist(captor.capture());

        LearnerRedirect captured = captor.getValue();

        assertAll(() -> {
            assertNotNull(persisted);
            assertEquals(redirectId, persisted.getId());
            assertNotNull(persisted.getVersion());
            assertEquals(key, persisted.getKey());
            assertEquals(LearnerRedirectType.PRODUCT, persisted.getType());
            assertEquals(destinationPath, persisted.getDestinationPath());
        });

        assertAll(() -> {
            assertNotNull(captured);
            assertEquals(redirectId, captured.getId());
            assertNotNull(captured.getVersion());
            assertEquals(key, captured.getKey());
            assertEquals(LearnerRedirectType.PRODUCT, captured.getType());
            assertEquals(destinationPath, captured.getDestinationPath());
        });
    }

    @Test
    void fetch() {
        when(learnerRedirectGateway.fetch(LearnerRedirectType.PRODUCT, key))
                .thenReturn(Mono.just(new LearnerRedirect()
                        .setId(redirectId)
                        .setKey(key)
                        .setDestinationPath(destinationPath)
                        .setType(LearnerRedirectType.PRODUCT)
                        .setVersion(UUID.randomUUID())));

        LearnerRedirect found = learnerRedirectService.fetch(LearnerRedirectType.PRODUCT, key)
                .block();

        assertNotNull(found);
        assertAll(() -> {
            assertNotNull(found);
            assertEquals(redirectId, found.getId());
            assertNotNull(found.getVersion());
            assertEquals(key, found.getKey());
            assertEquals(LearnerRedirectType.PRODUCT, found.getType());
            assertEquals(destinationPath, found.getDestinationPath());
        });
    }

    @Test
    void delete_noRedirectId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> learnerRedirectService.delete(null));

        assertEquals("redirectId is required", f.getMessage());
        verify(learnerRedirectGateway, never()).fetchById(any(UUID.class));
        verify(learnerRedirectGateway, never()).delete(any(LearnerRedirect.class));
    }

    @Test
    void delete_notFound() {
        when(learnerRedirectGateway.fetchById(redirectId)).thenReturn(Mono.empty());

        assertNull(learnerRedirectService.delete(redirectId).block());

        verify(learnerRedirectGateway).fetchById(redirectId);
        verify(learnerRedirectGateway, never()).delete(any(LearnerRedirect.class));
    }

    @Test
    void delete() {

        ArgumentCaptor<LearnerRedirect> captor = ArgumentCaptor.forClass(LearnerRedirect.class);

        LearnerRedirect learnerRedirect = new LearnerRedirect()
                .setId(redirectId)
                .setType(LearnerRedirectType.PRODUCT)
                .setKey(key)
                .setDestinationPath(destinationPath)
                .setVersion(UUID.randomUUID());

        when(learnerRedirectGateway.fetchById(redirectId))
                .thenReturn(Mono.just(learnerRedirect));

        when(learnerRedirectGateway.delete(learnerRedirect)).thenReturn(Flux.just(new Void[]{}));

        LearnerRedirect deleted = learnerRedirectService.delete(redirectId)
                .block();

        verify(learnerRedirectGateway).fetchById(redirectId);
        verify(learnerRedirectGateway).delete(captor.capture());

        LearnerRedirect captured = captor.getValue();

        assertAll(() -> {
            assertNotNull(deleted);
            assertNotNull(captured);
            assertEquals(deleted, captured);

            assertEquals(key, captured.getKey());
            assertEquals(LearnerRedirectType.PRODUCT, captured.getType());
            assertEquals(redirectId, captured.getId());
            assertEquals(destinationPath, captured.getDestinationPath());
            assertEquals(learnerRedirect.getVersion(), captured.getVersion());
        });
    }

}
