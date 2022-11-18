package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;

public class CohortUnsubscribeMessageHandler implements MessageHandler<CohortGenericMessage> {

    public static final String WORKSPACE_COHORT_UNSUBSCRIBE = "workspace.cohort.unsubscribe";
    public static final String WORKSPACE_COHORT_UNSUBSCRIBE_OK = "workspace.cohort.unsubscribe.ok";
    public static final String WORKSPACE_COHORT_UNSUBSCRIBE_ERROR = "workspace.cohort.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory;

    @Inject
    public CohortUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                           CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.cohortRTMSubscriptionFactory = cohortRTMSubscriptionFactory;
    }

    @Override
    public void validate(CohortGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getCohortId() != null, "cohortId is required");
    }

    @Override
    public void handle(Session session, CohortGenericMessage message) throws WriteResponseException {
        try {
            CohortRTMSubscription cohortRTMSubscription = cohortRTMSubscriptionFactory.create(message.getCohortId());
            rtmSubscriptionManagerProvider.get().unsubscribe(cohortRTMSubscription.getName());

            BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_COHORT_UNSUBSCRIBE_OK, message.getId());
            Responses.write(session, responseMessage);

        } catch (SubscriptionNotFound ex) {
            ErrorMessage error = new ErrorMessage(WORKSPACE_COHORT_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for cohort %s not found", message.getCohortId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
