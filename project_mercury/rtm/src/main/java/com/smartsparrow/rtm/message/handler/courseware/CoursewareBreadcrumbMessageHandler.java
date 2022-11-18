package com.smartsparrow.rtm.message.handler.courseware;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareBreadcrumbMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import reactor.core.publisher.Mono;

public class CoursewareBreadcrumbMessageHandler implements MessageHandler<CoursewareBreadcrumbMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareBreadcrumbMessageHandler.class);

    public static final String AUTHOR_COURSEWARE_BREADCRUMB = "author.courseware.breadcrumb";
    public static final String AUTHOR_COURSEWARE_BREADCRUMB_OK = "author.courseware.breadcrumb.ok";
    public static final String AUTHOR_COURSEWARE_BREADCRUMB_ERROR = "author.courseware.breadcrumb.error";

    private final CoursewareService coursewareService;

    @Inject
    public CoursewareBreadcrumbMessageHandler(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    @Override
    public void validate(CoursewareBreadcrumbMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getElementId() != null, "elementId is required");
            checkArgument(message.getElementType() != null, "elementType is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_COURSEWARE_BREADCRUMB_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COURSEWARE_BREADCRUMB)
    @Override
    public void handle(Session session, CoursewareBreadcrumbMessage message) {

        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ELEMENT_ID.getValue(), message.getElementId().toString(), log);
        Mono<UUID> workspaceIdMono = coursewareService.getWorkspaceIdByProject(message.getElementId(), message.getElementType());
        Mono<UUID> projectIdMono = coursewareService.getProjectId(message.getElementId(), message.getElementType());
        Mono<List<CoursewareElement>> pathMono = coursewareService.getPath(message.getElementId(), message.getElementType());

        Mono.zip(workspaceIdMono, projectIdMono, pathMono)
                .doOnEach(log.reactiveErrorThrowableIf("Error while fetching breadcrumb", throwable ->
                        !(throwable instanceof ParentActivityNotFoundException || throwable instanceof ParentPathwayNotFoundException)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(
                        tuple3 -> {
                            BasicResponseMessage response = new BasicResponseMessage(AUTHOR_COURSEWARE_BREADCRUMB_OK, message.getId());
                            response.addField("workspaceId", tuple3.getT1());
                            response.addField("projectId", tuple3.getT2());
                            response.addField("breadcrumb", tuple3.getT3());
                            Responses.writeReactive(session, response);
                        },
                        ex -> {
                            log.jsonDebug("Unable to fetch breadcrumb for element", new HashMap<String, Object>() {
                                {
                                    put("elementId", message.getElementId());
                                    put("elementType", message.getElementType());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            String errorMessage;
                            if (ex instanceof ParentActivityNotFoundException || ex instanceof ParentPathwayNotFoundException) {
                                errorMessage = ex.getMessage();
                            } else {
                                errorMessage = "Unable to fetch breadcrumb";
                            }
                            Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_BREADCRUMB_ERROR,
                                    HttpStatus.SC_UNPROCESSABLE_ENTITY, errorMessage);
                        }
                );
    }
}
