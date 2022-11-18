package com.smartsparrow.rtm.message.handler.user_content;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.user_content.SharedResourceMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.user_content.service.SharedResourceService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UserSharedResourceCreateMessageHandler implements MessageHandler<SharedResourceMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(UserSharedResourceCreateMessageHandler.class);

    public static final String USER_CONTENT_SHARED_RESOURCE_CREATE = "user.content.shared.resource.create";
    public static final String USER_CONTENT_SHARED_RESOURCE_CREATE_OK = "user.content.shared.resource.create.ok";
    public static final String USER_CONTENT_SHARED_RESOURCE_CREATE_ERROR = "user.content.shared.resource.create.error";

    private final SharedResourceService sharedResourceService;

    @Inject
    public UserSharedResourceCreateMessageHandler(final SharedResourceService sharedResourceService) {
        this.sharedResourceService = sharedResourceService;
    }

    @Override
    public void validate(final SharedResourceMessage message) throws RTMValidationException {
        affirmNotNull(message.getAccountId(), "accountId is required");
        affirmNotNull(message.getSharedAccountId(), "sharedAccountId is required");
        affirmNotNull(message.getResourceType(), "resourceType is required");
        affirmNotNull(message.getResourceId(), "resourceId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = USER_CONTENT_SHARED_RESOURCE_CREATE)
    @Override
    public void handle(final Session session, final SharedResourceMessage message) throws WriteResponseException {
        sharedResourceService.create(message.getResourceId(),
                                             message.getSharedAccountId(),
                                             message.getAccountId(),
                                             message.getResourceType())
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(summary -> {
                    //Do nothing
                }, ex -> {
                    logger.jsonDebug("Unable to persist shared resource", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });

                    if (ex instanceof ConflictFault) {
                        Responses.errorReactive(session, message.getId(), USER_CONTENT_SHARED_RESOURCE_CREATE_ERROR,
                                                HttpStatus.SC_CONFLICT, "error persist shared resource.");
                    } else {
                        Responses.errorReactive(session, message.getId(), USER_CONTENT_SHARED_RESOURCE_CREATE_ERROR,
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error persist shared resource");
                    }
                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(USER_CONTENT_SHARED_RESOURCE_CREATE_OK, message.getId()));
                });
    }

}
