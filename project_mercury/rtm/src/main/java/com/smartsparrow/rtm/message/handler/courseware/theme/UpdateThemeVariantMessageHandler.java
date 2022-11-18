package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.UpdateThemeVariantMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class UpdateThemeVariantMessageHandler implements MessageHandler<UpdateThemeVariantMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateThemeVariantMessageHandler.class);

    public static final String AUTHOR_THEME_VARIANT_UPDATE = "author.theme.variant.update";
    public static final String AUTHOR_THEME_VARIANT_UPDATE_OK = "author.theme.variant.update.ok";
    public static final String AUTHOR_THEME_VARIANT_UPDATE_ERROR = "author.theme.variant.update.error";

    private final ThemeService themeService;

    @Inject
    public UpdateThemeVariantMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(UpdateThemeVariantMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(message.getVariantId() != null, "missing variantId");
        affirmArgument(message.getVariantName() != null, "missing variant name");
        affirmArgument(message.getConfig() != null, "missing config");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_VARIANT_UPDATE)
    @Override
    public void handle(Session session,
                       UpdateThemeVariantMessage message) throws WriteResponseException {
        themeService.updateThemeVariant(message.getThemeId(),
                                        message.getVariantId(),
                                        message.getVariantName(),
                                        message.getConfig())
                .doOnEach(log.reactiveErrorThrowable("error updating a theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", message.getThemeId());
                                                             put("variantName", message.getVariantName());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(themeVariant -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_THEME_VARIANT_UPDATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("themeVariant", themeVariant);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to update a theme variant {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_THEME_VARIANT_UPDATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to update a theme variant");
                           }
                );

    }
}
