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
import com.smartsparrow.rtm.message.recv.courseware.theme.UpdateCoursewareThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class UpdateCoursewareThemeMessageHandler implements MessageHandler<UpdateCoursewareThemeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateCoursewareThemeMessageHandler.class);

    public static final String AUTHOR_THEME_UPDATE = "author.theme.update";
    public static final String AUTHOR_THEME_UPDATE_OK = "author.theme.update.ok";
    public static final String AUTHOR_THEME_UPDATE_ERROR = "author.theme.update.error";

    private final ThemeService themeService;

    @Inject
    public UpdateCoursewareThemeMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(UpdateCoursewareThemeMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(message.getName() != null, "missing theme name");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_UPDATE)
    @Override
    public void handle(Session session,
                       UpdateCoursewareThemeMessage message) throws WriteResponseException {

        themeService.update(message.getThemeId(),
                            message.getName())
                .doOnEach(log.reactiveErrorThrowable("error updating a theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", message.getThemeId());
                                                             put("name", message.getName());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(theme -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_THEME_UPDATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("theme", theme);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to update a theme {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_THEME_UPDATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to update a theme");
                           }
                );

    }
}
