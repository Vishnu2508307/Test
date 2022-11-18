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
import com.smartsparrow.rtm.message.recv.courseware.theme.GetThemeVariantMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class WorkspaceGetThemeVariantMessageHandler implements MessageHandler<GetThemeVariantMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceGetThemeVariantMessageHandler.class);

    public static final String WORKSPACE_THEME_VARIANT_GET = "workspace.theme.variant.get";
    public static final String WORKSPACE_THEME_VARIANT_GET_OK = "workspace.theme.variant.get.ok";
    public static final String WORKSPACE_THEME_VARIANT_GET_ERROR = "workspace.theme.variant.get.error";

    private final ThemeService themeService;

    @Inject
    public WorkspaceGetThemeVariantMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(GetThemeVariantMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(message.getVariantId() != null, "missing variantId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = WORKSPACE_THEME_VARIANT_GET)
    @Override
    public void handle(Session session,
                       GetThemeVariantMessage message) throws WriteResponseException {
        themeService.getThemeVariant(message.getThemeId(),
                                        message.getVariantId())
                .doOnEach(log.reactiveErrorThrowable("error getting a theme variant",
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
                                       WORKSPACE_THEME_VARIANT_GET_OK,
                                       message.getId());
                               basicResponseMessage.addField("themeVariant", themeVariant);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to get a theme variant {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       WORKSPACE_THEME_VARIANT_GET_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to get a theme variant");
                           }
                );

    }
}
