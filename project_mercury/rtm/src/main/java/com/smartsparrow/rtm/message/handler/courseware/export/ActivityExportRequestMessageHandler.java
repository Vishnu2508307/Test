package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.util.Warrants.affirmArgument;


import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.rtm.util.NewRelic;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.export.service.CoursewareElementExportService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.export.CreateCoursewareRootElementExportMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.WorkspaceProject;

public class ActivityExportRequestMessageHandler implements MessageHandler<CreateCoursewareRootElementExportMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityExportRequestMessageHandler.class);

    public static final String AUTHOR_EXPORT_REQUEST = "author.activity.export.request";
    public static final String AUTHOR_EXPORT_REQUEST_OK = "author.activity.export.request.ok";
    public static final String AUTHOR_EXPORT_REQUEST_ERROR = "author.activity.export.request.error";

    private final CoursewareElementExportService coursewareExportService;
    private final CoursewareService coursewareService;
    private final ProjectGateway projectGateway;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ActivityExportRequestMessageHandler(final CoursewareElementExportService coursewareExportService,
                                               final CoursewareService coursewareService,
                                               final Provider<AuthenticationContext> authenticationContextProvider,
                                               final ProjectGateway projectGateway) {
        this.coursewareExportService = coursewareExportService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.projectGateway = projectGateway;
    }

    @Override
    public void validate(CreateCoursewareRootElementExportMessage message) throws RTMValidationException {
        UUID elementId = message.getElementId();
        affirmArgument(elementId != null, "missing elementId");
        UUID projectId = coursewareService.getProjectId(message.getElementId(), CoursewareElementType.ACTIVITY).block();
        affirmArgument(projectId != null, "missing projectId");
        WorkspaceProject workspaceProject = projectGateway.findWorkspaceForProject(projectId).block();

        affirmArgument((workspaceProject != null && workspaceProject.getWorkspaceId() != null), "missing workspaceId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "author.activity.export.request")
    @Override
    public void handle(Session session,
                       CreateCoursewareRootElementExportMessage message) throws WriteResponseException {

        final Account account = authenticationContextProvider.get().getAccount();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ELEMENT_ID.getValue(), message.getElementId().toString(), log);

        coursewareExportService.create(message.getElementId(),
                                       CoursewareElementType.ACTIVITY,
                                       account.getId(),
                                       message.getExportType(),
                                       message.getMetadata())
                .doOnEach(log.reactiveErrorThrowable("error creating the courseware activity export",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(exportData -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_EXPORT_REQUEST_OK,
                                       message.getId());
                               basicResponseMessage.addField("exportId", exportData.getExportId());
                               basicResponseMessage.addField("exportElementsCount", exportData.getElementsExportedCount());
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to create courseware element export {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_EXPORT_REQUEST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to create courseware activity export");
                           }
                );

    }
}
