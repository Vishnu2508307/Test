package com.smartsparrow.rtm.message.handler.courseware.scope;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.scope.ListSourcesRegisteredToScopeMessageHandler.AUTHOR_SOURCE_SCOPE_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.scope.ListSourcesRegisteredToScopeMessageHandler.AUTHOR_SOURCE_SCOPE_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.RegisteredScopeReference;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.scope.ListSourcesRegisteredToScopeMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ListSourcesRegisteredToScopeMessageHandlerTest {

    private Session session;

    @InjectMocks
    ListSourcesRegisteredToScopeMessageHandler handler;

    @Mock
    CoursewareService coursewareService;

    @Mock
    private ListSourcesRegisteredToScopeMessage message;

    private static final UUID studentScopeUrn = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.2.0";
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getScopeURN()).thenReturn(studentScopeUrn);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);

        session = mockSession();
    }

    @Test
    void validate_noStudentScopeId() {
        when(message.getScopeURN()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("scopeURN is required", ex.getMessage());
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementType is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        List<ConfigurationField> configFields = new ArrayList<>();
        configFields.add(new ConfigurationField()
                                 .setFieldName("title").setFieldValue("titleValue"));
        when(coursewareService.fetchSourcesByScopeUrn(any(), any()))
                .thenReturn(Flux.just(new RegisteredScopeReference()
                                              .setStudentScopeUrn(studentScopeUrn)
                                              .setElementId(elementId)
                                              .setElementType(elementType)
                                              .setPluginId(pluginId)
                                              .setPluginVersion(pluginVersion)
                                              .setConfigurationFields(configFields)
                                              .setConfigSchema("configSchema")));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_SOURCE_SCOPE_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("registeredScopeReference"));
                assertEquals(elementId.toString(), ((LinkedHashMap) responseList.get(0)).get("elementId"));
                assertEquals(elementType.toString(), ((LinkedHashMap) responseList.get(0)).get("elementType"));
                assertEquals(pluginId.toString(), ((LinkedHashMap) responseList.get(0)).get("pluginId"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<RegisteredScopeReference> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(coursewareService.fetchSourcesByScopeUrn(any(), any()))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_SOURCE_SCOPE_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch sources registered to a scope\"}");
    }
}
