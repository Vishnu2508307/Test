package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.export.ExportGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription.ExportEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ActivityExportSubscribeMessageHandler implements MessageHandler<ExportGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityExportSubscribeMessageHandler.class);

    public static final String AUTHOR_EXPORT_SUBSCRIBE = "author.export.subscribe";
    private static final String AUTHOR_EXPORT_SUBSCRIBE_OK = "author.export.subscribe.ok";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory;

    @Inject
    public ActivityExportSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                 ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.exportEventRTMSubscriptionFactory = exportEventRTMSubscriptionFactory;
    }

    @Override
    public void validate(ExportGenericMessage message) {
        affirmArgument(message.getExportId() != null, "exportId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "author.export.subscribe")
    @Override
    public void handle(Session session, ExportGenericMessage message) throws WriteResponseException {

        ExportEventRTMSubscription exportEventRTMSubscription = exportEventRTMSubscriptionFactory.create(message.getExportId());
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.EXPORT_ID.getValue(), message.getExportId().toString(), log);

        rtmSubscriptionManagerProvider.get().add(exportEventRTMSubscription)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("exception occurred while adding the subscription"))
                .subscribe(listenerId -> {},
                           this::subscriptionOnErrorHandler,
                           () -> {
                                log.jsonDebug("client subscribing to events ", new HashMap<String, Object>() {
                                    {
                                        put("exportId", message.getExportId());
                                    }
                                });
                                Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_EXPORT_SUBSCRIBE_OK, message.getId())
                                        .addField("rtmSubscriptionId", exportEventRTMSubscription.getId()));
                });
    }
}
