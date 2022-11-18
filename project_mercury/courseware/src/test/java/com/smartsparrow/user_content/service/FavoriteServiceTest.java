package com.smartsparrow.user_content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.user_content.eventmessage.UserContentNotificationMessage;
import com.smartsparrow.user_content.data.Favorite;
import com.smartsparrow.user_content.data.FavoriteGateway;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.route.UserContentRoute;
import com.smartsparrow.util.UUIDs;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteGateway favoriteGateway;

    @Mock
    private CacheService cacheService;

    @Mock
    private CamelReactiveStreamsService camelReactiveStreamsService;

    final UUID activityId = UUIDs.timeBased();
    final UUID workspaceId = UUIDs.timeBased();
    final UUID accountId = UUIDs.timeBased();
    final UUID projectId = UUIDs.timeBased();
    final UUID rootElementId = UUIDs.timeBased();
    final UUID documentId = UUIDs.timeBased();
    final ResourceType resourceType = ResourceType.COURSE;
    private static final String FAVORITE_KEY = "FAVORITE";

    Favorite favoriteSaved = new Favorite()
            .setId(UUIDs.random())
            .setAccountId(UUIDs.random())
            .setActivityId(UUIDs.random())
            .setDocumentId(UUIDs.random())
            .setWorkspaceId(UUIDs.random())
            .setResourceType(ResourceType.COURSE)
            .setRootElementId(UUIDs.random())
            .setProjectId(UUIDs.random());
    Favorite favorite = new Favorite();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        favorite.setAccountId(accountId)
                .setActivityId(activityId)
                .setDocumentId(documentId)
                .setWorkspaceId(workspaceId)
                .setResourceType(resourceType)
                .setRootElementId(rootElementId)
                .setResourceType(resourceType);
    }


    //TODO max limit favorites test case needs to be incorporated as part of favorites get as there is dependency to assert
    @Test
    void validateAndCreateFavorite() {
        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        TestPublisher<String> requestPublisher = TestPublisher.create();
        when(camelReactiveStreamsService.toStream(
                eq(UserContentRoute.USER_CONTENT_REQUEST),
                any(UserContentNotificationMessage.class),
                eq(String.class))).thenReturn(requestPublisher.mono());
        Mockito.when(favoriteGateway.getByAccountId(accountId)).thenReturn(Flux.just(favoriteSaved));
        Mockito.when(favoriteGateway.persist(any())).thenReturn(Flux.just(new Void[]{}));
        favoriteService.createFavorite(activityId,
                                       workspaceId,
                                       accountId,
                                       projectId,
                                       rootElementId,
                                       documentId,
                                       resourceType).block();
        verify(favoriteGateway).persist(captor.capture());
        List<Favorite> all = captor.getAllValues();
        assertNotNull(all);
        assertEquals(1, all.size());
    }

    @Test
    void validateAndCreateFavorite_verifyAccountId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.createFavorite(activityId,
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
    void validateAndCreateFavorite_verifyWorkspaceId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.createFavorite(activityId,
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
    void validateAndCreateFavorite_verifyRootElementId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.createFavorite(activityId,
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
    void validateAndCreateFavorite_verifyResourceType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.createFavorite(activityId,
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
    void getListOfFavorites() {
        when(favoriteGateway.getByAccountId(accountId)).thenReturn(Flux.just(favorite));
        List<Favorite> listOfFavorites = favoriteService.getListOfFavorites(accountId).collectList().block();
        assertNotNull(listOfFavorites);
        assertEquals(1, listOfFavorites.size());
    }

    @Test
    void getListOfFavorites_userIdNull() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.getListOfFavorites(null).collectList().block());
        assertNotNull(f);
        assertEquals("accountId is required", f.getMessage());
    }

    @Test
    void getList() {
        String cacheKey = String.format("user:content:/%s/%s",
                                        FAVORITE_KEY,
                                        accountId);
        List<Favorite> listOfFavoritesDetails = new ArrayList<>();
        listOfFavoritesDetails.add(favorite);
        when(cacheService.computeIfAbsent(cacheKey, (Class<List<Favorite>>)(Class<?>) List.class, Mono.empty())).thenReturn(Mono.just(listOfFavoritesDetails));
        when(favoriteGateway.getByAccountId(accountId)).thenReturn(Flux.just(favorite));
        List<Favorite> listOfFavorites = favoriteService.getList(accountId).block();
        assertNotNull(listOfFavorites);
        assertEquals(1, listOfFavorites.size());
    }

    @Test
    void getList_userIdNull() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.getList(null).block());        assertNotNull(f);
        assertEquals("accountId is required", f.getMessage());
    }

    @Test
    void remove_accountIdNull() {
        favorite.setId(UUID.randomUUID());
        favorite.setAccountId(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.remove(favorite).block());
        assertNotNull(f);
        assertEquals("accountId is required", f.getMessage());
    }
    @Test
    void remove_IdNull() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> favoriteService.remove(favorite).block());
        assertNotNull(f);
        assertEquals("favoriteId is required", f.getMessage());
    }

    @Test
    void remove() {
        favorite.setId(UUID.randomUUID());
        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        TestPublisher<String> requestPublisher = TestPublisher.create();
        when(camelReactiveStreamsService.toStream(
                eq(UserContentRoute.USER_CONTENT_REQUEST),
                any(UserContentNotificationMessage.class),
                eq(String.class))).thenReturn(requestPublisher.mono());
        Mockito.when(favoriteGateway.removeFavorite(any())).thenReturn(Mono.empty());
        favoriteService.remove(favorite).block();
        verify(favoriteGateway).removeFavorite(captor.capture());
        Favorite captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(favorite.getId(), captured.getId());
    }
}