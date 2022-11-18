package com.smartsparrow.rtm.message.handler.learner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.lang.DataValidationException;
import com.smartsparrow.learner.lang.InvalidFieldsException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.learner.SetStudentScopeMessage;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class SetStudentScopeMessageHandlerTest {

    @InjectMocks
    private SetStudentScopeMessageHandler handler;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private StudentScopeService studentScopeService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private StudentScopeProducer studentScopeProducer;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID sourceId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID timeId = UUID.randomUUID();
    private static final String data = "data";
    private SetStudentScopeMessage message;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp () {
        MockitoAnnotations.openMocks(this);

        message = mock(SetStudentScopeMessage.class);
        when(message.getData()).thenReturn(data);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(message.getSourceId()).thenReturn(sourceId);
        when(message.getStudentScopeURN()).thenReturn(studentScopeURN);
        Account account = mock(Account.class);
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(account.getId()).thenReturn(studentId);
        when(context.getAccount()).thenReturn(account);
        when(authenticationContextProvider.get()).thenReturn(context);

        RTMClientContext rtmClientContext = mock(RTMClientContext.class);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

    }

    @Test
    void validate_nullSourceId() {
        when(message.getSourceId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("sourceId is required", e.getMessage());
    }

    @Test
    void validate_nullDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("deploymentId is required", e.getMessage());
    }

    @Test
    void validate_nullScopeURN() {
        when(message.getStudentScopeURN()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("studentScopeURN is required", e.getMessage());
    }

    @Test
    void validate_nullData() {
        when(message.getData()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("data is required", e.getMessage());
    }

    @Test
    void handle_success() {
        UUID entryId = UUID.randomUUID();
        UUID scopeId = UUID.randomUUID();
        DeployedActivity deployment = new DeployedActivity();
        StudentScopeEntry studentScopeEntry = new StudentScopeEntry()
                .setData(data)
                .setSourceId(sourceId)
                .setScopeId(scopeId)
                .setId(entryId);
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(studentScopeService.setStudentScope(deployment, studentId, studentScopeURN, sourceId, data))
                .thenReturn(Mono.just(studentScopeEntry));

        when(studentScopeProducer.buildStudentScopeConsumable(studentId,
                                                                 deploymentId,
                                                                 studentScopeURN,
                                                                 studentScopeEntry))
                .thenReturn(studentScopeProducer);

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"learner.student.scope.set.ok\"," +
                            "\"response\":{" +
                                "\"studentScope\":{" +
                                    "\"id\":\""+entryId+"\"," +
                                    "\"scopeId\":\""+scopeId+"\"," +
                                    "\"sourceId\":\""+sourceId+"\"," +
                                    "\"data\":\"data\"" +
                                "}" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_successIdProvided() {
        when(message.getTimeId()).thenReturn(timeId);
        UUID scopeId = UUID.randomUUID();
        DeployedActivity deployment = new DeployedActivity();
        StudentScopeEntry studentScopeEntry = new StudentScopeEntry()
                .setData(data)
                .setSourceId(sourceId)
                .setScopeId(scopeId)
                .setId(timeId);
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(studentScopeService.setStudentScope(deployment, studentId, studentScopeURN, sourceId, data, timeId))
                .thenReturn(Mono.just(studentScopeEntry));

        when(studentScopeProducer.buildStudentScopeConsumable(studentId,
                                                              deploymentId,
                                                              studentScopeURN,
                                                              studentScopeEntry))
                .thenReturn(studentScopeProducer);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"learner.student.scope.set.ok\"," +
                "\"response\":{" +
                "\"studentScope\":{" +
                "\"id\":\""+timeId+"\"," +
                "\"scopeId\":\""+scopeId+"\"," +
                "\"sourceId\":\""+sourceId+"\"," +
                "\"data\":\"data\"" +
                "}" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_dataValidationError() {
        InvalidFieldsException ife = new InvalidFieldsException("ops", Lists.newArrayList("foo"));
        DeployedActivity deployment = new DeployedActivity();
        TestPublisher<StudentScopeEntry> publisher = TestPublisher.create();
        publisher.error(new DataValidationException("Invalid data", ife));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(studentScopeService.setStudentScope(deployment, studentId, studentScopeURN, sourceId, data))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"learner.student.scope.set.error\",\"code\":422,\"message\":\"data has invalid fields: [foo]\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_genericError() {
        DeployedActivity deployment = new DeployedActivity();
        TestPublisher<StudentScopeEntry> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("ops"));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployment));
        when(studentScopeService.setStudentScope(deployment, studentId, studentScopeURN, sourceId, data))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"learner.student.scope.set.error\",\"code\":500,\"message\":\"ops\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
