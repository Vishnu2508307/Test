package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.ThemeCoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.theme.CreateElementThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.elementthemecreate.ElementThemeCreateRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;


public class CreateElementThemeMessageHandler implements MessageHandler<CreateElementThemeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateElementThemeMessageHandler.class);

    public static final String AUTHOR_ELEMENT_THEME_CREATE = "author.element.theme.create";
    public static final String AUTHOR_ELEMENT_THEME_CREATE_OK = "author.element.theme.create.ok";
    public static final String AUTHOR_ELEMENT_THEME_CREATE_ERROR = "author.element.theme.create.error";

    private final ThemeService themeService;
    private final CoursewareService coursewareService;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ElementThemeCreateRTMProducer elementThemeCreateRTMProducer;

    @Inject
    public CreateElementThemeMessageHandler(ThemeService themeService,
                                            CoursewareService coursewareService,
                                            Provider<AuthenticationContext> authenticationContextProvider,
                                            Provider<RTMEventBroker> rtmEventBrokerProvider,
                                            Provider<RTMClientContext> rtmClientContextProvider,
                                            ElementThemeCreateRTMProducer activityElementThemeCreateRTMProducer) {
        this.themeService = themeService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.elementThemeCreateRTMProducer = activityElementThemeCreateRTMProducer;
    }

    @Override
    public void validate(CreateElementThemeMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ELEMENT_THEME_CREATE)
    @Override
    public void handle(Session session, CreateElementThemeMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<ThemeCoursewareElement> themeByCoursewareElementMono = themeService.saveThemeByElement(message.getThemeId(),
                                                                                                    message.getElementId(),
                                                                                                    message.getElementType())
                .doOnEach(log.reactiveErrorThrowable("error creating an element theme association",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", message.getThemeId());
                                                             put("elementId", message.getElementId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getElementId(),
                                                                          message.getElementType());
        Mono.zip(themeByCoursewareElementMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ELEMENT_THEME_CREATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("elementTheme", tuple2.getT1());
                               Responses.writeReactive(session, basicResponseMessage);

                               rtmEventBroker.broadcast(message.getType(), getData(message, account));
                               elementThemeCreateRTMProducer.buildElementThemeCreateRTMConsumable(rtmClientContext,
                                                                                                  tuple2.getT2(),
                                                                                                  message.getElementId(),
                                                                                                  message.getElementType(),
                                                                                                  message.getThemeId()).produce();
                           },
                           ex -> {
                               log.jsonError(
                                       "Unable to create an element theme association {}",
                                       new HashMap<String, Object>() {
                                           {
                                               put("message",
                                                   message.toString());
                                               put("error",
                                                   ex.getStackTrace());
                                           }
                                       },
                                       ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ELEMENT_THEME_CREATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to create an element theme association");
                           }
                );

    }

    /**
     * Build the data that the {@link RTMEventBroker} will send with the event message.
     *
     * @param message  the received message
     * @return the data
     */
    private CoursewareElementBroadcastMessage getData(CreateElementThemeMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setParentElement(null)
                .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                .setAction(CoursewareAction.ELEMENT_THEME_CREATE)
                .setAccountId(account.getId());
    }
}
