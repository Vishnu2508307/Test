package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.CreateCoursewareThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class CreateCoursewareThemeMessageHandler implements MessageHandler<CreateCoursewareThemeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateCoursewareThemeMessageHandler.class);

    public static final String AUTHOR_THEME_CREATE = "author.theme.create";
    public static final String AUTHOR_THEME_CREATE_OK = "author.theme.create.ok";
    public static final String AUTHOR_THEME_CREATE_ERROR = "author.theme.create.error";

    private final ThemeService themeService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public CreateCoursewareThemeMessageHandler(ThemeService themeService,
                                               Provider<AuthenticationContext> authenticationContextProvider) {
        this.themeService = themeService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(CreateCoursewareThemeMessage message) throws RTMValidationException {
        affirmArgument(message.getWorkspaceId() != null, "missing workspaceId");
        affirmArgument(message.getName() != null, "missing theme name");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_CREATE)
    @Override
    public void handle(Session session,
                       CreateCoursewareThemeMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        UUID accountId = account.getId();
        themeService.create(accountId,
                            message.getName())
                .doOnEach(log.reactiveErrorThrowable("error creating a theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("name", message.getName());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(theme -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_THEME_CREATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("theme", theme);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to create a theme {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_THEME_CREATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to create a theme");
                           }
                );

    }
}
