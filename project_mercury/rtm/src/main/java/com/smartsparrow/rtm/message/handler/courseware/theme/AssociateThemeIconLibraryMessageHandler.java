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
import com.smartsparrow.rtm.message.recv.courseware.theme.AssociateThemeIconLibraryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class AssociateThemeIconLibraryMessageHandler implements MessageHandler<AssociateThemeIconLibraryMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AssociateThemeIconLibraryMessageHandler.class);

    public static final String AUTHOR_THEME_ICON_LIBRARY_ASSOCIATE = "author.theme.icon.library.associate";
    public static final String AUTHOR_THEME_ICON_LIBRARY_ASSOCIATE_OK = "author.theme.icon.library.associate.ok";
    public static final String AUTHOR_THEME_ICON_LIBRARY_ASSOCIATE_ERROR = "author.theme.icon.library.associate.error";

    private final ThemeService themeService;

    @Inject
    public AssociateThemeIconLibraryMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(final AssociateThemeIconLibraryMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(themeService.fetchThemeById(message.getThemeId()).block() != null,
                       String.format("theme %s not found", message.getThemeId()));
        affirmArgumentNotNullOrEmpty(message.getIconLibraries(), "missing icon library info");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_ICON_LIBRARY_ASSOCIATE)
    @Override
    public void handle(final Session session,
                       final AssociateThemeIconLibraryMessage message) throws WriteResponseException {
        themeService.saveThemeIconLibraries(message.getThemeId(), message.getIconLibraries())
                .doOnEach(log.reactiveErrorThrowable("error saving a theme and icon library association"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(iconLibrariesByTheme -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_THEME_ICON_LIBRARY_ASSOCIATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("iconLibrariesByTheme", iconLibrariesByTheme);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to save theme and icon library association {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_THEME_ICON_LIBRARY_ASSOCIATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to save theme and icon library association");
                           }
                );

    }
}
