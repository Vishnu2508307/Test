package com.smartsparrow.user_content.service;

import static com.smartsparrow.user_content.route.UserContentRoute.USER_CONTENT_REQUEST;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.slf4j.Logger;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.user_content.data.Favorite;
import com.smartsparrow.user_content.data.FavoriteGateway;
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.eventmessage.UserContentNotificationMessage;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class FavoriteService {

    private static final Logger logger = MercuryLoggerFactory.getLogger(FavoriteService.class);
    private static final int MAX_ENTRIES = 25;
    private static final String FAVORITE_KEY = "FAVORITE";
    private final FavoriteGateway favoriteGateway;
    private final CamelReactiveStreamsService camelReactiveStreams;
    private final CacheService cacheService;
    @Inject
    public FavoriteService(final FavoriteGateway favoriteGateway,
                           final CamelReactiveStreamsService camelReactiveStreams,
                           final CacheService cacheService) {
        this.favoriteGateway = favoriteGateway;
        this.camelReactiveStreams = camelReactiveStreams;
        this.cacheService = cacheService;
    }

    /**
     * Persist users favorite details
     * @param activityId activityId
     * @param workspaceId workspace id
     * @param accountId account id
     * @param projectId project id
     * @param rootElementId rootElement id
     * @param documentId document id
     * @param resourceType resource type
     * @return void flux
     */
    @Trace(async = true)
    public Mono<Favorite> createFavorite(final UUID activityId,
                                     final UUID workspaceId,
                                     final UUID accountId,
                                     final UUID projectId,
                                     final UUID rootElementId,
                                     final UUID documentId,
                                     final ResourceType resourceType) {

        affirmNotNull(accountId, "accountId is required");
        affirmNotNull(workspaceId, "workspaceId is required");
        affirmNotNull(rootElementId, "rootElementId is required");
        affirmNotNull(resourceType, "resourceType is required");
        affirmNotNull(projectId, "projectId is required");
        if(resourceType.equals(ResourceType.LESSON)) {
            affirmNotNull(activityId, "activityId is required");
        }
        Favorite favorite = new Favorite()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setWorkspaceId(workspaceId)
                .setAccountId(accountId)
                .setProjectId(projectId)
                .setRootElementId(rootElementId)
                .setDocumentId(documentId)
                .setCreatedAt(UUIDs.timeBased())
                .setResourceType(resourceType);
        return favoriteGateway.persist(favorite).singleOrEmpty()
                .then(sendNotificationToSNS(favorite))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(favorite);
    }

    private Mono<Void> sendNotificationToSNS(final Favorite favorite) {

        UserContentNotificationMessage userContentNotificationMessage = new UserContentNotificationMessage()
                .setType(FAVORITE_KEY)
                .setReferenceType("ACCOUNT_ID")
                .setReferenceId(favorite.getAccountId());
        return Mono.just(camelReactiveStreams.toStream(USER_CONTENT_REQUEST, userContentNotificationMessage, String.class))
                .doOnError(throwable -> {
                    logger.error(String.format("error while adding course/lesson to favorite %s", favorite), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .thenReturn(Mono.empty()).then();
    }

    /**
     * Get Flux of Favorite
     * @param accountId account id
     * @return flux of favorite
     */
    @Trace(async = true)
    public Flux<Favorite> getListOfFavorites(final UUID accountId) {
        affirmNotNull(accountId, "accountId is required");
        return favoriteGateway.getByAccountId(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Get list of favorites for the account ID
     * @param accountId user account id
     * @return list
     */
    @Trace(async = true)
    public Mono<List<Favorite>> getList(final UUID accountId) {
        affirmNotNull(accountId, "accountId is required");
        String cacheKey = String.format("user:content:/%s/%s",
                                        FAVORITE_KEY,
                                        accountId);
        Flux<Favorite> favorites = getListOfFavorites(accountId);
        return  cacheService.computeIfAbsent(cacheKey, (Class<List<Favorite>>)(Class<?>) List.class, Mono.empty())
                .switchIfEmpty(favorites.collectList())
                .doOnError(throwable -> {
                    sendNotificationToSNS(new Favorite().setAccountId(accountId));
                }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    public Mono<Void> remove(final Favorite favorite) {
        affirmNotNull(favorite.getAccountId(), "accountId is required");
        affirmNotNull(favorite.getId(), "favoriteId is required");
        return favoriteGateway.removeFavorite(favorite)
                .then(sendNotificationToSNS(favorite));
    }
}
