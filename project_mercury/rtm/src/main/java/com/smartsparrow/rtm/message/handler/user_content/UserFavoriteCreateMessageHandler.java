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
import com.smartsparrow.user_content.data.ResourceType;
import com.smartsparrow.user_content.service.FavoriteService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UserFavoriteCreateMessageHandler implements MessageHandler<FavoriteMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(UserFavoriteCreateMessageHandler.class);

    public static final String USER_CONTENT_FAVORITE_CREATE = "user.content.favorite.create";
    public static final String USER_CONTENT_FAVORITE_CREATE_OK = "user.content.favorite.create.ok";
    public static final String USER_CONTENT_FAVORITE_CREATE_ERROR = "user.content.favorite.create.error";

    private final FavoriteService favoriteService;

    @Inject
    public UserFavoriteCreateMessageHandler(final FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Override
    public void validate(final FavoriteMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "RootElementId is required");
        affirmArgument(message.getAccountId() != null, "accountId is required");
        affirmArgument(message.getResourceType() != null, "resourceType is required");
        affirmArgument(message.getWorkspaceId() != null, "workspaceId is required");
        affirmArgument(message.getProjectId() != null, "projectId is required");
        if(message.getResourceType().equals(ResourceType.LESSON)) {
            affirmArgument(message.getActivityId() != null, "activityId is required");
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = USER_CONTENT_FAVORITE_CREATE)
    @Override
    public void handle(final Session session, final FavoriteMessage message) throws WriteResponseException {
        favoriteService.createFavorite(message.getActivityId(),
                                       message.getWorkspaceId(),
                                       message.getAccountId(),
                                       message.getProjectId(),
                                       message.getRootElementId(),
                                       message.getDocumentId(),
                                       message.getResourceType())
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(favorite -> {
                    BasicResponseMessage response = new BasicResponseMessage(USER_CONTENT_FAVORITE_CREATE_OK,
                                                                             message.getId());
                    response.addField("favoriteId", favorite.getId());
                    Responses.writeReactive(session, response);
                }, ex -> {
                    logger.jsonDebug("Unable to create favorite", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });

                    if (ex instanceof ConflictFault) {
                        Responses.errorReactive(session, message.getId(), USER_CONTENT_FAVORITE_CREATE_ERROR,
                                                HttpStatus.SC_CONFLICT, "error creating the user favorite.");
                    } else {
                        Responses.errorReactive(session, message.getId(), USER_CONTENT_FAVORITE_CREATE_ERROR,
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error creating the user favorite");
                    }
                });
    }

}
