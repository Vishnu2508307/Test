package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.DeleteThemeVariantMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ThemeVariant;


public class DeleteThemeVariantMessageHandler implements MessageHandler<DeleteThemeVariantMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteThemeVariantMessageHandler.class);

    public static final String AUTHOR_THEME_VARIANT_DELETE = "author.theme.variant.delete";
    public static final String AUTHOR_THEME_VARIANT_DELETE_OK = "author.theme.variant.delete.ok";
    public static final String AUTHOR_THEME_VARIANT_DELETE_ERROR = "author.theme.variant.delete.error";

    private final ThemeService themeService;

    @Inject
    public DeleteThemeVariantMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(DeleteThemeVariantMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(message.getVariantId() != null, "missing variantId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_VARIANT_DELETE)
    @Override
    public void handle(Session session,
                       DeleteThemeVariantMessage message) throws WriteResponseException {

        themeService.deleteThemeVariant(message.getThemeId(),
                                        message.getVariantId())
                .doOnEach(log.reactiveErrorThrowable("error deleting a theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", message.getThemeId());
                                                             put("variantName", message.getVariantId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ignore -> {
                               // nothing here, never executed
                           }, ex -> {
                               log.jsonDebug("Unable to delete theme variant", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session, message.getId(), AUTHOR_THEME_VARIANT_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting theme variant");
                           },
                           () -> Responses.writeReactive(session,
                                                         new BasicResponseMessage(AUTHOR_THEME_VARIANT_DELETE_OK,
                                                                                  message.getId())));
    }
}
