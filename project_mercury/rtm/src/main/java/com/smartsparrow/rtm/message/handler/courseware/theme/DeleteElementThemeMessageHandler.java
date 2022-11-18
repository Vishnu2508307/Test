package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
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
import com.smartsparrow.rtm.message.recv.courseware.theme.DeleteElementThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.elementthemedelete.ElementThemeDeleteRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteElementThemeMessageHandler implements MessageHandler<DeleteElementThemeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteElementThemeMessageHandler.class);

    public static final String AUTHOR_ELEMENT_THEME_DELETE = "author.element.theme.delete";
    public static final String AUTHOR_ELEMENT_THEME_DELETE_OK = "author.element.theme.delete.ok";
    public static final String AUTHOR_ELEMENT_THEME_DELETE_ERROR = "author.element.theme.delete.error";

    private final ThemeService themeService;
    private final CoursewareService coursewareService;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ElementThemeDeleteRTMProducer elementThemeDeleteRTMProducer;

    @Inject
    public DeleteElementThemeMessageHandler(ThemeService themeService,
                                            CoursewareService coursewareService,
                                            Provider<AuthenticationContext> authenticationContextProvider,
                                            Provider<RTMEventBroker> rtmEventBrokerProvider,
                                            Provider<RTMClientContext> rtmClientContextProvider,
                                            ElementThemeDeleteRTMProducer elementThemeDeleteRTMProducer) {
        this.themeService = themeService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.elementThemeDeleteRTMProducer = elementThemeDeleteRTMProducer;
    }

    @Override
    public void validate(final DeleteElementThemeMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ELEMENT_THEME_DELETE)
    @Override
    public void handle(final Session session,
                       final DeleteElementThemeMessage message) throws WriteResponseException {

        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        themeService.deleteThemeByElement(message.getElementId(),
                                                                message.getElementType())
                .doOnEach(log.reactiveErrorThrowable("error deleting an element theme association",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                             put("elementType", message.getElementType());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getElementId(), message.getElementType()))
                .subscribe(rootElementId -> {
                               elementThemeDeleteRTMProducer.buildElementThemeDeleteRTMConsumable(rtmClientContext,
                                                                                                  rootElementId,
                                                                                                  message.getElementId(),
                                                                                                  message.getElementType()).produce();
                           }, ex -> {
                               log.jsonError("Unable to delete an element theme association {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             }, ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ELEMENT_THEME_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to delete an element theme association");
                           }, () -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ELEMENT_THEME_DELETE_OK,
                                       message.getId());
                               Responses.writeReactive(session, basicResponseMessage);
                               rtmEventBroker.broadcast(message.getType(), getData(message, account));
                           }
                );
    }

    /**
     * Build the data that the {@link RTMEventBroker} will send with the event message.
     *
     * @param message  the received message
     * @return the data
     */
    private CoursewareElementBroadcastMessage getData(DeleteElementThemeMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setParentElement(null)
                .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                .setAction(CoursewareAction.ELEMENT_THEME_DELETE)
                .setAccountId(account.getId());
    }
}
