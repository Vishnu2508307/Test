package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.rtm.util.NewRelic;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.export.service.CoursewareElementExportService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.export.CreateCoursewareElementExportMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.WorkspaceProject;

public class ElementExportRequestMessageHandler implements MessageHandler<CreateCoursewareElementExportMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ElementExportRequestMessageHandler.class);

    public static final String AUTHOR_ELEMENT_EXPORT_REQUEST = "author.element.export.request";
    public static final String AUTHOR_ELEMENT_EXPORT_REQUEST_OK = "author.element.export.request.ok";
    public static final String AUTHOR_ELEMENT_EXPORT_REQUEST_ERROR = "author.element.export.request.error";

    private final CoursewareElementExportService coursewareExportService;
    private final CoursewareService coursewareService;
    private final ProjectGateway projectGateway;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ElementExportRequestMessageHandler(final CoursewareElementExportService coursewareExportService,
                                              final CoursewareService coursewareService,
                                              final Provider<AuthenticationContext> authenticationContextProvider,
                                              final ProjectGateway projectGateway) {
        this.coursewareExportService = coursewareExportService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.projectGateway = projectGateway;
    }

    @Override
    public void validate(CreateCoursewareElementExportMessage message) throws RTMValidationException {
        UUID elementId = message.getElementId();
        affirmArgument(elementId != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        UUID projectId = coursewareService.getProjectId(message.getElementId(), message.getElementType()).block();
        affirmArgument(projectId != null, "missing projectId");
        WorkspaceProject workspaceProject = projectGateway.findWorkspaceForProject(projectId).block();

        affirmArgument((workspaceProject != null && workspaceProject.getWorkspaceId() != null), "missing workspaceId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "author.element.export.request")
    @Override
    public void handle(Session session, CreateCoursewareElementExportMessage message) throws WriteResponseException {

        final Account account = authenticationContextProvider.get().getAccount();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ELEMENT_ID.getValue(), message.getElementId().toString(), log);

        coursewareExportService.create(message.getElementId(),
                                       message.getElementType(),
                                       account.getId(),
                                       message.getExportType(),
                                       message.getMetadata())
                .doOnEach(log.reactiveErrorThrowable("error creating the courseware element export",
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
                                       AUTHOR_ELEMENT_EXPORT_REQUEST_OK,
                                       message.getId());
                               basicResponseMessage.addField("exportId", exportData.getExportId());
                               basicResponseMessage.addField("exportElementsCount", exportData.getElementsExportedCount());
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.debug("Unable to create courseware element export", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ELEMENT_EXPORT_REQUEST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to create courseware element export");
                           }
                );

    }
}
