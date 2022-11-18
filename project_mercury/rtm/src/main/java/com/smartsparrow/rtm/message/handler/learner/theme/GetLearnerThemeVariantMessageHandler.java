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
import com.smartsparrow.rtm.message.recv.learner.theme.GetLearnerThemeVariantMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetLearnerThemeVariantMessageHandler implements MessageHandler<GetLearnerThemeVariantMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetLearnerThemeVariantMessageHandler.class);

    public static final String LEARNER_THEME_VARIANT_GET = "learner.theme.variant.get";
    public static final String LEARNER_THEME_VARIANT_GET_OK = "learner.theme.variant.get.ok";
    public static final String LEARNER_THEME_VARIANT_GET_ERROR = "learner.theme.variant.get.error";

    private final LearnerThemeService learnerThemeService;

    @Inject
    public GetLearnerThemeVariantMessageHandler(final LearnerThemeService learnerThemeService) {
        this.learnerThemeService = learnerThemeService;
    }

    @Override
    public void validate(GetLearnerThemeVariantMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "themeId is required");
        affirmArgument(message.getVariantId() != null, "variantId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_THEME_VARIANT_GET)
    @Override
    public void handle(Session session, GetLearnerThemeVariantMessage message) throws WriteResponseException {

        learnerThemeService.fetchThemeVariant(message.getThemeId(), message.getVariantId())
                .doOnEach(log.reactiveErrorThrowable("error fetching learner theme variant ",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", message.getThemeId());
                                                             put("variantId", message.getVariantId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(themeVariant -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       LEARNER_THEME_VARIANT_GET_OK,
                                       message.getId());
                               basicResponseMessage.addField("themeVariant", themeVariant);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch learner theme variant",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       LEARNER_THEME_VARIANT_GET_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch learner theme variant");
                           }
                );
    }
}
