package com.smartsparrow.rtm.message.handler.courseware.scope;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.scope.ListSourcesRegisteredToScopeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class ListSourcesRegisteredToScopeMessageHandler implements MessageHandler<ListSourcesRegisteredToScopeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListSourcesRegisteredToScopeMessageHandler.class);

    public static final String AUTHOR_SOURCE_SCOPE_LIST = "author.source.scope.list";
    public static final String AUTHOR_SOURCE_SCOPE_LIST_OK = "author.source.scope.list.ok";
    public static final String AUTHOR_SOURCE_SCOPE_LIST_ERROR = "author.source.scope.list.error";

    private final CoursewareService coursewareService;

    @Inject
    public ListSourcesRegisteredToScopeMessageHandler(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    @Override
    public void validate(ListSourcesRegisteredToScopeMessage message) throws RTMValidationException {
        affirmArgument(message.getScopeURN() != null, "scopeURN is required");
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_SOURCE_SCOPE_LIST)
    public void handle(Session session, ListSourcesRegisteredToScopeMessage message) throws WriteResponseException {

        coursewareService.fetchSourcesByScopeUrn(message.getScopeURN(), Lists.newArrayList(
                ConfigurationField.TITLE))
                .doOnEach(log.reactiveErrorThrowable("error fetching all sources by scope urn",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("scopeUrn", message.getScopeURN());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(registeredScopeReference -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_SOURCE_SCOPE_LIST_OK,
                                       message.getId());
                               basicResponseMessage.addField("registeredScopeReference", registeredScopeReference);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch sources registered to a scope", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_SOURCE_SCOPE_LIST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch sources registered to a scope");
                           }
                );
    }
}
