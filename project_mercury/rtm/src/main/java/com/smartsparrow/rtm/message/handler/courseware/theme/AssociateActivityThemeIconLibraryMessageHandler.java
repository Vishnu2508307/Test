package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.AssociateActivityIconLibraryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class AssociateActivityThemeIconLibraryMessageHandler implements MessageHandler<AssociateActivityIconLibraryMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(
            AssociateActivityThemeIconLibraryMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_ICON_LIBRARY_ASSOCIATE = "author.activity.icon.library.associate";
    public static final String AUTHOR_ACTIVITY_ICON_LIBRARY_ASSOCIATE_OK = "author.activity.icon.library.associate.ok";
    public static final String AUTHOR_ACTIVITY_ICON_LIBRARY_ASSOCIATE_ERROR = "author.activity.icon.library.associate.error";

    private final ThemeService themeService;

    @Inject
    public AssociateActivityThemeIconLibraryMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(final AssociateActivityIconLibraryMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "missing activityId");
        affirmArgumentNotNullOrEmpty(message.getIconLibraries(), "missing icon library info");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ACTIVITY_ICON_LIBRARY_ASSOCIATE)
    @Override
    public void handle(final Session session,
                       final AssociateActivityIconLibraryMessage message) throws WriteResponseException {
        themeService.saveActivityThemeIconLibraries(message.getActivityId(), message.getIconLibraries())
                .doOnEach(log.reactiveErrorThrowable("error saving an activity and icon library association"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(activityThemeIconLibraries -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ACTIVITY_ICON_LIBRARY_ASSOCIATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("activityThemeIconLibraries", activityThemeIconLibraries);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to save an activity and icon library association {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ACTIVITY_ICON_LIBRARY_ASSOCIATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to save an activity and icon library association");
                           }
                );

    }
}
