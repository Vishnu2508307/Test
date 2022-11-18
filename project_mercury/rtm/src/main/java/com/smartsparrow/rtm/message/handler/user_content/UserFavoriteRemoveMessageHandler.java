package com.smartsparrow.rtm.message.handler.user_content;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.user_content.FavoriteMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.user_content.data.Favorite;
import com.smartsparrow.user_content.service.FavoriteService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UserFavoriteRemoveMessageHandler implements MessageHandler<FavoriteMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(UserFavoriteRemoveMessageHandler.class);

    public static final String USER_CONTENT_FAVORITE_REMOVE = "user.content.favorite.remove";
    public static final String USER_CONTENT_FAVORITE_REMOVE_OK = "user.content.favorite.remove.ok";
    public static final String USER_CONTENT_FAVORITE_REMOVE_ERROR = "user.content.favorite.remove.error";

    private final FavoriteService favoriteService;

    @Inject
    public UserFavoriteRemoveMessageHandler(final FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }


    @Override
    public void validate(final FavoriteMessage message) throws RTMValidationException {
        affirmArgument(message.getFavoriteId() != null, "favoriteId is required");
        affirmArgument(message.getAccountId() != null, "accountId is required");
        affirmArgument(message.getRootElementId() != null, "rootElementId is required");
    }

    @Override
    public void handle(final Session session, final FavoriteMessage message) throws WriteResponseException {

        favoriteService.remove(new Favorite().setId(message.getFavoriteId())
                                       .setAccountId(message.getAccountId()))
                .doOnEach(logger.reactiveErrorThrowable("error removing favorite",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("favoriteId", message.getFavoriteId());
                                                             put("accountId", message.getAccountId());
                                                             put("rootElementId", message.getRootElementId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ignore -> {
                               // nothing here, never executed
                           }, ex -> {
                               logger.jsonDebug("Unable to remove favorite", new HashMap<String, Object>() {
                                   {
                                       put("favoriteId", message.getFavoriteId());
                                       put("accountId", message.getAccountId());
                                       put("rootElementId", message.getRootElementId());
                                   }
                               });
                               Responses.errorReactive(session, message.getFavoriteId().toString(), USER_CONTENT_FAVORITE_REMOVE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting theme");
                           },
                           () -> Responses.writeReactive(session,
                                                         new BasicResponseMessage(USER_CONTENT_FAVORITE_REMOVE_OK,
                                                                                  message.getFavoriteId().toString())));
    }
}
