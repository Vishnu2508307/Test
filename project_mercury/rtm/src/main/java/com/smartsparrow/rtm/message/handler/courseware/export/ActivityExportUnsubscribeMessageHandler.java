package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.export.ExportGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription.ExportEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ActivityExportUnsubscribeMessageHandler implements MessageHandler<ExportGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityExportUnsubscribeMessageHandler.class);

    public static final String AUTHOR_EXPORT_UNSUBSCRIBE = "author.export.unsubscribe";
    private static final String AUTHOR_EXPORT_UNSUBSCRIBE_OK = "author.export.unsubscribe.ok";
    private static final String AUTHOR_EXPORT_UNSUBSCRIBE_ERROR = "author.export.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory;

    @Inject
    public ActivityExportUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                   final ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.exportEventRTMSubscriptionFactory = exportEventRTMSubscriptionFactory;
    }

    @Override
    public void validate(ExportGenericMessage message) {
        affirmArgument(message.getExportId() != null, "exportId is required");
    }

    @Override
    public void handle(Session session, ExportGenericMessage message) throws WriteResponseException {
        try {
            ExportEventRTMSubscription exportEventRTMSubscription = exportEventRTMSubscriptionFactory.create(message.getExportId());
            rtmSubscriptionManagerProvider.get().unsubscribe(exportEventRTMSubscription.getName());
            Responses.write(session, new BasicResponseMessage(AUTHOR_EXPORT_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.debug("subscription not found ", subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(AUTHOR_EXPORT_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for export %s not found", message.getExportId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
