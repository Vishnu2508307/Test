package com.smartsparrow.rtm.message.handler.cohort;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;


public class CohortSubscribeMessageHandler implements MessageHandler<CohortGenericMessage> {

    public static final String WORKSPACE_COHORT_SUBSCRIBE = "workspace.cohort.subscribe";
    public static final String WORKSPACE_COHORT_SUBSCRIBE_OK = "workspace.cohort.subscribe.ok";
    public static final String WORKSPACE_COHORT_SUBSCRIBE_ERROR = "workspace.cohort.subscribe.error";
    public static final String WORKSPACE_COHORT_SUBSCRIBE_BROADCAST = "workspace.cohort.broadcast";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory;

    @Inject
    public CohortSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                         CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.cohortRTMSubscriptionFactory = cohortRTMSubscriptionFactory;
    }

    @Override
    public void validate(CohortGenericMessage message) throws RTMValidationException {
        if (message.getCohortId() == null) {
            throw new RTMValidationException("cohortId is required", message.getId(), WORKSPACE_COHORT_SUBSCRIBE_ERROR);
        }
    }

    @Override
    public void handle(Session session, CohortGenericMessage message) throws WriteResponseException {
        CohortRTMSubscription cohortRTMSubscription = cohortRTMSubscriptionFactory.create(message.getCohortId());
        rtmSubscriptionManagerProvider.get().add(cohortRTMSubscription)
                .subscribe(
                        listenerId -> {
                        },
                        this::subscriptionOnErrorHandler,
                        () -> {
                            BasicResponseMessage responseMessage = new BasicResponseMessage(
                                    WORKSPACE_COHORT_SUBSCRIBE_OK,
                                    message.getId());
                            responseMessage.addField("rtmSubscriptionId", cohortRTMSubscription.getId());
                            Responses.writeReactive(session, responseMessage);
                        }
                );

    }
}
