package com.smartsparrow.rtm.message.handler.user_content;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.user_content.FavoriteMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.user_content.service.FavoriteService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UserFavoriteGetMessageHandler implements MessageHandler<FavoriteMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(UserFavoriteGetMessageHandler.class);

    public static final String USER_CONTENT_FAVORITE_GET = "user.content.favorite.get";
    public static final String USER_CONTENT_FAVORITE_GET_OK = "user.content.favorite.get.ok";
    public static final String USER_CONTENT_FAVORITE_GET_ERROR = "user.content.favorite.get.error";

    private final FavoriteService favoriteService;

    @Inject
    public UserFavoriteGetMessageHandler(final FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Override
    public void validate(final FavoriteMessage message) throws RTMValidationException {
        affirmArgument(message.getAccountId() != null, "accountId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = USER_CONTENT_FAVORITE_GET)
    @Override
    public void handle(final Session session, final FavoriteMessage message) throws WriteResponseException {
        favoriteService.getList(message.getAccountId())
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(favoriteList -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(USER_CONTENT_FAVORITE_GET_OK, message.getId())
                                                    .addField("favorites", favoriteList));
                }, ex -> {
                    logger.jsonDebug("Unable to get favorite list", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });

                    if (ex instanceof ConflictFault) {
                        Responses.errorReactive(session, message.getId(), USER_CONTENT_FAVORITE_GET_ERROR,
                                                HttpStatus.SC_CONFLICT, "error getting the user favorite.");
                    } else {
                        Responses.errorReactive(session, message.getId(), USER_CONTENT_FAVORITE_GET_ERROR,
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error getting the user favorite");
                    }
                });
    }

}
