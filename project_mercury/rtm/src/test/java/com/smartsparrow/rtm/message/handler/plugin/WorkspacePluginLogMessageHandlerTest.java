package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.plugin.WorkspacePluginLogMessageHandler.WORKSPACE_PLUGIN_LOG_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginLogLevel;
import com.smartsparrow.plugin.data.WorkspaceLogStatement;
import com.smartsparrow.plugin.lang.PluginLogException;
import com.smartsparrow.plugin.service.PluginLogService;
import com.smartsparrow.plugin.wiring.PluginLogConfig;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.WorkspacePluginLogMessage;

import reactor.core.publisher.Mono;

class WorkspacePluginLogMessageHandlerTest {

    private static final UUID pluginId = UUID.randomUUID();
    private static final String version = "1.2.0";
    private static final PluginLogLevel infoLevel = PluginLogLevel.INFO;
    private static final PluginLogLevel errorLevel = PluginLogLevel.ERROR;
    private static final PluginLogLevel warnLevel = PluginLogLevel.WARN;
    private static final PluginLogLevel debugLevel = PluginLogLevel.DEBUG;
    private static final String message = "log message";
    private static final String args = "log args";
    private static final String pluginContext = "PREVIEW";
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID eventId = UUID.randomUUID();
    private static final UUID transactionId = UUID.randomUUID();
    private static final UUID segmentId = UUID.randomUUID();
    private static final String transactionName = "SCREEN_TO_SCREEN";
    private static final String segmentName = "CLICK_START";
    private static final String transactionSequenceStart = "START";

    @InjectMocks
    private WorkspacePluginLogMessageHandler handler;
    @Mock
    private PluginLogService pluginLogService;
    @Mock
    private PluginLogConfig pluginLogConfig;
    @Mock
    private WorkspacePluginLogMessage pluginLogMessage;
    @Mock
    private WorkspaceLogStatement workspaceLogStatement;
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(pluginLogMessage.getPluginId()).thenReturn(pluginId);
        when(pluginLogMessage.getVersion()).thenReturn(version);
        when(pluginLogMessage.getLevel()).thenReturn(infoLevel);
        when(pluginLogMessage.getMessage()).thenReturn(message);
        when(pluginLogMessage.getArgs()).thenReturn(args);
        when(pluginLogMessage.getPluginContext()).thenReturn(pluginContext);
        when(pluginLogMessage.getElementId()).thenReturn(elementId);
        when(pluginLogMessage.getElementType()).thenReturn(elementType);
        when(pluginLogMessage.getProjectId()).thenReturn(projectId);
        when(pluginLogConfig.getEnabled()).thenReturn(true);
        when(pluginLogMessage.getEventId()).thenReturn(eventId);
        when(pluginLogMessage.getEventId()).thenReturn(eventId);
        when(pluginLogMessage.getTransactionId()).thenReturn(transactionId);
        when(pluginLogMessage.getSegmentId()).thenReturn(segmentId);
        when(pluginLogMessage.getSegmentName()).thenReturn(segmentName);
        when(pluginLogMessage.getTransactionName()).thenReturn(transactionName);
        when(pluginLogMessage.getTransactionSequence()).thenReturn(transactionSequenceStart);
    }

    @Test
    void validate_noPluginId() {
        when(pluginLogMessage.getPluginId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing plugin id", ex.getMessage());
    }

    @Test
    void validate_noVersion() {
        when(pluginLogMessage.getVersion()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing version", ex.getMessage());
    }

    @Test
    void validate_noLevel() {
        when(pluginLogMessage.getLevel()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing level", ex.getMessage());
    }

    @Test
    void validate_noMessage() {
        when(pluginLogMessage.getMessage()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing message", ex.getMessage());
    }

    @Test
    void validate_noArgs() {
        when(pluginLogMessage.getArgs()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing args", ex.getMessage());
    }

    @Test
    void validate_noPluginContext() {
        when(pluginLogMessage.getPluginContext()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing plugin context", ex.getMessage());
    }

    @Test
    void validate_noElementId() {
        when(pluginLogMessage.getElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing element id", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(pluginLogMessage.getElementType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing element type", ex.getMessage());
    }

    @Test
    void validate_noProjectId() {
        when(pluginLogMessage.getProjectId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing project id", ex.getMessage());
    }

    @Test
    void validate_noEventIdId() {
        when(pluginLogMessage.getEventId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(pluginLogMessage));
        assertEquals("missing event id", ex.getMessage());
    }

    @Test
    void handle_notEnabled() throws IOException, PluginLogException {
        when(pluginLogConfig.getEnabled()).thenReturn(false);

        handler.handle(session, pluginLogMessage);

        verify(pluginLogService, never()).logWorkspacePluginStatement(pluginLogMessage.getPluginId(),
                                                                       pluginLogMessage.getVersion(),
                                                                       pluginLogMessage.getLevel(),
                                                                       pluginLogMessage.getMessage(),
                                                                       pluginLogMessage.getArgs(),
                                                                       pluginLogMessage.getPluginContext(),
                                                                       pluginLogMessage.getElementId(),
                                                                       pluginLogMessage.getElementType(),
                                                                       pluginLogMessage.getProjectId());
    }

    @Test
    void handle_info() throws IOException, PluginLogException {
        when(pluginLogService.logWorkspacePluginStatement(pluginLogMessage.getPluginId(),
                                                           pluginLogMessage.getVersion(),
                                                           pluginLogMessage.getLevel(),
                                                           pluginLogMessage.getMessage(),
                                                           pluginLogMessage.getArgs(),
                                                           pluginLogMessage.getPluginContext(),
                                                           pluginLogMessage.getElementId(),
                                                           pluginLogMessage.getElementType(),
                                                           pluginLogMessage.getProjectId()))
                .thenReturn(Mono.just(workspaceLogStatement));

        handler.handle(session, pluginLogMessage);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_LOG_OK, response.getType());
            });
        });
    }

    @Test
    void handle_error() throws IOException, PluginLogException {
        when(pluginLogMessage.getLevel()).thenReturn(errorLevel);
        when(pluginLogService.logWorkspacePluginStatement(pluginLogMessage.getPluginId(),
                                                          pluginLogMessage.getVersion(),
                                                          pluginLogMessage.getLevel(),
                                                          pluginLogMessage.getMessage(),
                                                          pluginLogMessage.getArgs(),
                                                          pluginLogMessage.getPluginContext(),
                                                          pluginLogMessage.getElementId(),
                                                          pluginLogMessage.getElementType(),
                                                          pluginLogMessage.getProjectId()))
                .thenReturn(Mono.just(workspaceLogStatement));

        handler.handle(session, pluginLogMessage);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_LOG_OK, response.getType());
            });
        });
    }

    @Test
    void handle_warn() throws IOException, PluginLogException {
        when(pluginLogMessage.getLevel()).thenReturn(warnLevel);
        when(pluginLogService.logWorkspacePluginStatement(pluginLogMessage.getPluginId(),
                                                          pluginLogMessage.getVersion(),
                                                          pluginLogMessage.getLevel(),
                                                          pluginLogMessage.getMessage(),
                                                          pluginLogMessage.getArgs(),
                                                          pluginLogMessage.getPluginContext(),
                                                          pluginLogMessage.getElementId(),
                                                          pluginLogMessage.getElementType(),
                                                          pluginLogMessage.getProjectId()))
                .thenReturn(Mono.just(workspaceLogStatement));

        handler.handle(session, pluginLogMessage);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_LOG_OK, response.getType());
            });
        });
    }

    @Test
    void handle_debug() throws IOException, PluginLogException {
        when(pluginLogMessage.getLevel()).thenReturn(debugLevel);
        when(pluginLogService.logWorkspacePluginStatement(pluginLogMessage.getPluginId(),
                                                          pluginLogMessage.getVersion(),
                                                          pluginLogMessage.getLevel(),
                                                          pluginLogMessage.getMessage(),
                                                          pluginLogMessage.getArgs(),
                                                          pluginLogMessage.getPluginContext(),
                                                          pluginLogMessage.getElementId(),
                                                          pluginLogMessage.getElementType(),
                                                          pluginLogMessage.getProjectId()))
                .thenReturn(Mono.just(workspaceLogStatement));

        handler.handle(session, pluginLogMessage);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_LOG_OK, response.getType());
            });
        });
    }

//    @Test
//    void handle_exception() throws IOException, PluginLogException {
//        TestPublisher<WorkspaceLogStatement> error = TestPublisher.create();
//        error.error(new RuntimeException("Unable to log the WorkspacePluginStatement"));
//        when(pluginLogService.logWorkspacePluginStatement(pluginLogMessage.getPluginId(),
//                                                           pluginLogMessage.getVersion(),
//                                                           pluginLogMessage.getLevel(),
//                                                           pluginLogMessage.getMessage(),
//                                                           pluginLogMessage.getArgs(),
//                                                           pluginLogMessage.getPluginContext(),
//                                                           pluginLogMessage.getElementId(),
//                                                           pluginLogMessage.getElementType(),
//                                                           pluginLogMessage.getProjectId())).thenReturn(error.mono());
//
//        handler.handle(session, pluginLogMessage);
//
//        MessageHandlerTestUtils.verifySentMessage(session,
//                                                  "{\"type\":\"" + WORKSPACE_PLUGIN_LOG_ERROR + "\",\"code\":422," +
//                                                          "\"message\":\"Error logging the WorkspacePluginStatement: java.lang.RuntimeException: Unable to log the WorkspacePluginStatement\"}");
//    }

}
