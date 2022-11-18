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
import com.smartsparrow.rtm.message.recv.courseware.theme.CreateThemeVariantMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class CreateThemeVariantMessageHandler implements MessageHandler<CreateThemeVariantMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateThemeVariantMessageHandler.class);

    public static final String AUTHOR_THEME_VARIANT_CREATE = "author.theme.variant.create";
    public static final String AUTHOR_THEME_VARIANT_CREATE_OK = "author.theme.variant.create.ok";
    public static final String AUTHOR_THEME_VARIANT_CREATE_ERROR = "author.theme.variant.create.error";

    private final ThemeService themeService;

    @Inject
    public CreateThemeVariantMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(CreateThemeVariantMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(message.getVariantName() != null, "missing variant name");
        affirmArgument(message.getConfig() != null, "missing config");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_VARIANT_CREATE)
    @Override
    public void handle(Session session,
                       CreateThemeVariantMessage message) throws WriteResponseException {
        themeService.createThemeVariant(message.getThemeId(),
                                        message.getVariantName(),
                                        message.getConfig(),
                                        message.getState())
                .doOnEach(log.reactiveErrorThrowable("error creating a theme variant",
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
                                       AUTHOR_THEME_VARIANT_CREATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("themeVariant", themeVariant);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to create a theme variant {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_THEME_VARIANT_CREATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to create a theme variant");
                           }
                );

    }
}
