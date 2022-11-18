package com.smartsparrow.rtm.message.handler.courseware.theme;


import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListCoursewareThemeMessageHandler implements MessageHandler<EmptyReceivedMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListCoursewareThemeMessageHandler.class);

    public static final String AUTHOR_THEME_LIST = "author.theme.list";
    public static final String AUTHOR_THEME_LIST_OK = "author.theme.list.ok";
    public static final String AUTHOR_THEME_LIST_ERROR = "author.theme.list.error";

    private final ThemeService themeService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ListCoursewareThemeMessageHandler(final ThemeService themeService,
                                             Provider<AuthenticationContext> authenticationContextProvider) {
        this.themeService = themeService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_THEME_LIST)
    @Override
    public void handle(Session session, EmptyReceivedMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        themeService.fetchThemes(account.getId())
                .doOnEach(log.reactiveErrorThrowable("error fetching theme payload ", throwable -> new HashMap<String, Object>() {
                    {
                        put("accountId", account.getId());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(themePayload -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    AUTHOR_THEME_LIST_OK,
                                    message.getId());
                            basicResponseMessage.addField("themePayload", themePayload);
                            Responses.writeReactive(session, basicResponseMessage);
                        },
                        ex -> {
                            log.jsonDebug("Unable to fetch theme payload", new HashMap<String, Object>() {
                                {
                                    put("message", message.toString());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), AUTHOR_THEME_LIST_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "Unable to fetch theme payload");
                        }
                );
    }
}
