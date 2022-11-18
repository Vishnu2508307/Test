package com.smartsparrow.rtm.message.handler.workspace;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.payload.ProjectChangeLogPayload;
import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.changelog.ListProjectChangeLogMessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectChangeLogListMessage;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListProjectChangeLogMessageHandlerTest {

    @InjectMocks
    private ListProjectChangeLogMessageHandler handler;

    @Mock
    private CoursewareChangeLogService coursewareChangeLogService;

    @Mock
    private ProjectChangeLogListMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private final String createdTimestamp = DateFormat.asRFC1123(UUIDs.timeBased());
    private final String updatedTimestamp = DateFormat.asRFC1123(UUIDs.timeBased());
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID onElementId = UUID.randomUUID();
    private static final Integer limit = 50;
    private final CoursewareElementType onElementType = CoursewareElementType.INTERACTIVE;
    private final CoursewareAction coursewareActionCreated = CoursewareAction.CREATED;
    private final CoursewareAction coursewareActionUpdated = CoursewareAction.UPDATED;
    private static final String familyName = "King";
    private static final String givenName = "BB";
    private static final String avatarSmall = "avatar";
    private static final String primaryEmail = "bb.king@blues.com";
    private ProjectChangeLogPayload projectChangeLogPayloadOne, projectChangeLogPayloadTwo;
    private static final UUID changeLogId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        projectChangeLogPayloadOne = new ProjectChangeLogPayload()
                .setId(changeLogId)
                .setProjectId(projectId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setAvatarSmall(avatarSmall)
                .setCoursewareAction(coursewareActionCreated)
                .setCreatedAt(createdTimestamp)
                .setFamilyName(familyName)
                .setGivenName(givenName)
                .setPrimaryEmail(primaryEmail);

        projectChangeLogPayloadTwo = new ProjectChangeLogPayload()
                .setId(changeLogId)
                .setProjectId(projectId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setAvatarSmall(avatarSmall)
                .setCoursewareAction(coursewareActionUpdated)
                .setCreatedAt(updatedTimestamp)
                .setFamilyName(familyName)
                .setGivenName(givenName)
                .setPrimaryEmail(primaryEmail);
    }

    @Test
    void validate_noProjectId() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> handler.validate(message));
        assertEquals("projectId is required", t.getMessage());
    }

    @Test
    void valid_success() throws RTMValidationException {
        when(message.getProjectId()).thenReturn(projectId);
        handler.validate(message);
    }

    @Test
    void handle_success() throws WriteResponseException {

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getLimit()).thenReturn(null);

        when(coursewareChangeLogService.fetchCoursewareChangeLogByProject(any(UUID.class), any(Integer.class)))
                .thenReturn(Flux.just(projectChangeLogPayloadOne, projectChangeLogPayloadTwo));

        handler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByProject(projectId, limit);

        String expected = "{\"type\":\"project.changelog.list.ok\"," +
                "\"response\":{\"projectchangelogs\":[" +
                "{" +
                "\"id\":\"" + changeLogId + "\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"createdAt\":\"" + createdTimestamp + "\"," +
                "\"givenName\":\"BB\"," +
                "\"familyName\":\"King\"," +
                "\"primaryEmail\":\"bb.king@blues.com\"," +
                "\"avatarSmall\":\"avatar\"," +
                "\"onElementId\":\"" + onElementId + "\"," +
                "\"accountId\":\"" + accountId + "\"," +
                "\"onElementType\":\"INTERACTIVE\"," +
                "\"coursewareAction\":\"CREATED\"" +
                "}," +
                "{" +
                "\"id\":\"" + changeLogId + "\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"createdAt\":\"" + updatedTimestamp + "\"," +
                "\"givenName\":\"BB\"," +
                "\"familyName\":\"King\"," +
                "\"primaryEmail\":\"bb.king@blues.com\"," +
                "\"avatarSmall\":\"avatar\"," +
                "\"onElementId\":\"" + onElementId + "\"," +
                "\"accountId\":\"" + accountId + "\"," +
                "\"onElementType\":\"INTERACTIVE\"," +
                "\"coursewareAction\":\"UPDATED\"" +
                "}]" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_custom_limit() throws WriteResponseException {

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getLimit()).thenReturn(1);

        when(coursewareChangeLogService.fetchCoursewareChangeLogByProject(projectId, 1))
                .thenReturn(Flux.just(projectChangeLogPayloadOne));

        handler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByProject(projectId, 1);

        String expected = "{\"type\":\"project.changelog.list.ok\"," +
                "\"response\":{\"projectchangelogs\":[" +
                "{" +
                "\"id\":\"" + changeLogId + "\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"createdAt\":\"" + createdTimestamp + "\"," +
                "\"givenName\":\"BB\"," +
                "\"familyName\":\"King\"," +
                "\"primaryEmail\":\"bb.king@blues.com\"," +
                "\"avatarSmall\":\"avatar\"," +
                "\"onElementId\":\"" + onElementId + "\"," +
                "\"accountId\":\"" + accountId + "\"," +
                "\"onElementType\":\"INTERACTIVE\"," +
                "\"coursewareAction\":\"CREATED\"" +
                "}]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_fail_default_limit() throws WriteResponseException {
        TestPublisher<ProjectChangeLogPayload> error = TestPublisher.create();

        when(coursewareChangeLogService.fetchCoursewareChangeLogByProject(projectId, limit)).thenReturn(error.flux());
        error.error(new RuntimeException());

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getLimit()).thenReturn(null);
        handler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByProject(projectId, limit);

        String expected = "{\"" +
                "type\":\"project.changelog.list.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetching courseware project change logs\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_fail_custom_limit() throws WriteResponseException {
        TestPublisher<ProjectChangeLogPayload> error = TestPublisher.create();

        when(coursewareChangeLogService.fetchCoursewareChangeLogByProject(projectId, 1)).thenReturn(error.flux());
        error.error(new RuntimeException());

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getLimit()).thenReturn(1);
        handler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByProject(projectId, 1);

        String expected = "{\"" +
                "type\":\"project.changelog.list.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetching courseware project change logs\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
