package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmDoesNotThrow;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;

import com.smartsparrow.courseware.data.DeletedActivity;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class GetActivityMessageHandler implements MessageHandler<ActivityGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetActivityMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_GET = "author.activity.get";
    public static final String AUTHOR_ACTIVITY_GET_OK = "author.activity.get.ok";
    public static final String AUTHOR_ACTIVITY_GET_ERROR = "author.activity.get.error";

    private ActivityService activityService;

    @Inject
    public GetActivityMessageHandler(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Override
    public void validate(ActivityGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activityId is missing");
        affirmDoesNotThrow(() -> activityService.findById(message.getActivityId()).block(), new NotFoundFault("Activity not found"));
        DeletedActivity deletedActivity = activityService.fetchDeletedActivityById(message.getActivityId()).block();
        if (deletedActivity != null) {
            throw new NotFoundFault("Activity not found");
        }
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, ActivityGenericMessage message) throws WriteResponseException {

        activityService.getActivityPayload(message.getActivityId())
                .doOnEach(log.reactiveErrorThrowable("error fetching activity payload", throwable -> new HashMap<String, Object>() {
                    {
                        put("activityId", message.getActivityId());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(payload -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_ACTIVITY_GET_OK, message.getId());
                    basicResponseMessage.addField("activity", payload);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_GET_ERROR, HttpStatus.SC_NOT_FOUND,
                            "Can't find activity");
                });
    }
}
