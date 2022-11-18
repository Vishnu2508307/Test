package com.smartsparrow.user_content.service;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.user_content.eventmessage.UserContentNotificationMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

/**
 * Manages to update Cache in user content for FAVORITE, RECENTLY_VIEWED and SHARED_CONTENT
 */
@Singleton
public class UserContentService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UserContentService.class);

    private final FavoriteService favoriteService;
    private final CacheService cacheService;


    @Inject
    public UserContentService(final FavoriteService favoriteService,
                              final CacheService cacheService) {
        this.favoriteService = favoriteService;
        this.cacheService = cacheService;
    }

    /**
     * Manages to write data to Redis cache
     * @param userContentNotificationMessage user content notification details to publish data to Redis
     * @return Mono of void
     */
    public Mono<Void> writeUserContentToCache(final UserContentNotificationMessage userContentNotificationMessage) {
        //Reference ID will be id which should be used to populate data in Cache
        return favoriteService.getListOfFavorites(
                        userContentNotificationMessage.getReferenceId())
                .collectList()
                .flatMap(favorites -> {

                    String cacheKey = String.format("user:content:/%s/%s",
                                                    userContentNotificationMessage.getType(),
                                                    userContentNotificationMessage.getReferenceId());

                    return cacheService.set(cacheKey, Mono.just(favorites), 12, TimeUnit.HOURS);
                }).then();

    }
}
