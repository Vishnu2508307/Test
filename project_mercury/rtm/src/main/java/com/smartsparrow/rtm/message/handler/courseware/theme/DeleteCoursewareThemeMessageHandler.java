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
import com.smartsparrow.rtm.message.recv.courseware.theme.GenericThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class DeleteCoursewareThemeMessageHandler implements MessageHandler<GenericThemeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteCoursewareThemeMessageHandler.class);

    public static final String AUTHOR_THEME_DELETE = "author.theme.delete";
    public static final String AUTHOR_THEME_DELETE_OK = "author.theme.delete.ok";
    public static final String AUTHOR_THEME_DELETE_ERROR = "author.theme.delete.error";

    private final ThemeService themeService;

    @Inject
    public DeleteCoursewareThemeMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(GenericThemeMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_DELETE)
    @Override
    public void handle(Session session,
                       GenericThemeMessage message) throws WriteResponseException {
        themeService.deleteTheme(message.getThemeId())
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error deleting theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", message.getThemeId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ignore -> {
                               // nothing here, never executed
                           }, ex -> {
                               log.jsonDebug("Unable to delete theme", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session, message.getId(), AUTHOR_THEME_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting theme");
                           },
                           () -> Responses.writeReactive(session,
                                                         new BasicResponseMessage(AUTHOR_THEME_DELETE_OK,
                                                                                  message.getId())));

    }
}
