package com.smartsparrow.rtm.message.handler.learner.theme;


import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.learner.service.LearnerThemeService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.theme.GetSelectedThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetSelectedThemeMessageHandler implements MessageHandler<GetSelectedThemeMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetSelectedThemeMessageHandler.class);

    public static final String LEARNER_SELECTED_THEME_GET = "learner.selected.theme.get";
    public static final String LEARNER_SELECTED_THEME_GET_OK = "learner.selected.theme.get.ok";
    public static final String LEARNER_SELECTED_THEME_GET_ERROR = "learner.selected.theme.get.error";

    private final LearnerThemeService learnerThemeService;

    @Inject
    public GetSelectedThemeMessageHandler(final LearnerThemeService learnerThemeService) {
        this.learnerThemeService = learnerThemeService;
    }

    @Override
    public void validate(GetSelectedThemeMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_SELECTED_THEME_GET)
    @Override
    public void handle(Session session, GetSelectedThemeMessage message) throws WriteResponseException {

        learnerThemeService.fetchSelectedTheme(message.getElementId())
                .doOnEach(log.reactiveErrorThrowable("error fetching learner selected theme payload ",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(selectedThemePayload -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       LEARNER_SELECTED_THEME_GET_OK,
                                       message.getId());
                               basicResponseMessage.addField("selectedThemePayload", selectedThemePayload);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch learner selected theme payload",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       LEARNER_SELECTED_THEME_GET_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch learner selected theme payload");
                           }
                );
    }
}
