package com.smartsparrow.rtm.message.handler.courseware.changelog;

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
import com.smartsparrow.courseware.payload.ElementChangeLogPayload;
import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.ElementChangeLogListMessage;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ListElementChangeLogMessageHandlerTest {

    @Mock
    private CoursewareChangeLogService coursewareChangeLogService;

    @InjectMocks
    private ListElementChangeLogMessageHandler listElementChangeLogMessageHandler;

    @Mock
    private ElementChangeLogListMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private final String createdTimestamp = DateFormat.asRFC1123(UUIDs.timeBased());
    private final String updatedTimestamp = DateFormat.asRFC1123(UUIDs.timeBased());
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID onElementId = UUID.randomUUID();
    private static final Integer limit = 50;
    private final CoursewareElementType onElementType = CoursewareElementType.INTERACTIVE;
    private final CoursewareAction coursewareActionCreated = CoursewareAction.CREATED;
    private final CoursewareAction coursewareActionUpdated = CoursewareAction.UPDATED;
    private static final UUID parentWalkableId = UUID.randomUUID();
    private static final CoursewareElementType parentWalkableType = CoursewareElementType.ACTIVITY;
    private static final String familyName = "King";
    private static final String givenName = "BB";
    private static final String avatarSmall = "avatar";
    private static final String primaryEmail = "bb.king@blues.com";
    private ElementChangeLogPayload elementChangeLogPayloadOne, elementChangeLogPayloadTwo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        elementChangeLogPayloadOne = new ElementChangeLogPayload()
                .setElementId(elementId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setAvatarSmall(avatarSmall)
                .setCoursewareAction(coursewareActionCreated)
                .setCreatedAt(createdTimestamp)
                .setFamilyName(familyName)
                .setGivenName(givenName)
                .setOnParentWalkableId(parentWalkableId)
                .setOnParentWalkableType(parentWalkableType)
                .setPrimaryEmail(primaryEmail);

        elementChangeLogPayloadTwo = new ElementChangeLogPayload()
                .setElementId(elementId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setAvatarSmall(avatarSmall)
                .setCoursewareAction(coursewareActionUpdated)
                .setCreatedAt(updatedTimestamp)
                .setFamilyName(familyName)
                .setGivenName(givenName)
                .setOnParentWalkableId(parentWalkableId)
                .setOnParentWalkableType(parentWalkableType)
                .setPrimaryEmail(primaryEmail);
    }

    @Test
    void validate_noElementId() {
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class,
                () -> listElementChangeLogMessageHandler.validate(message));
        assertEquals("elementId is required", t.getMessage());
    }

    @Test
    void valid_success() throws RTMValidationException {
        when(message.getElementId()).thenReturn(elementId);
        listElementChangeLogMessageHandler.validate(message);
    }

    @Test
    void handle_success() throws WriteResponseException {

        when(message.getElementId()).thenReturn(elementId);
        when(message.getLimit()).thenReturn(null);

        when(coursewareChangeLogService.fetchCoursewareChangeLogByElement(any(UUID.class), any(Integer.class)))
                .thenReturn(Flux.just(elementChangeLogPayloadOne, elementChangeLogPayloadTwo));

        listElementChangeLogMessageHandler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByElement(elementId, limit);

        String expected = "{\"type\":\"project.courseware.changelog.list.ok\"," +
                "\"response\":{\"changelogs\":[" +
                "{" +
                "\"elementId\":\"" + elementId + "\"," +
                "\"createdAt\":\"" + createdTimestamp + "\"," +
                "\"givenName\":\"BB\"," +
                "\"familyName\":\"King\"," +
                "\"primaryEmail\":\"bb.king@blues.com\"," +
                "\"avatarSmall\":\"avatar\"," +
                "\"onElementId\":\"" + onElementId + "\"," +
                "\"onParentWalkableId\":\"" + parentWalkableId + "\"," +
                "\"accountId\":\"" + accountId + "\"," +
                "\"onElementType\":\"INTERACTIVE\"," +
                "\"onParentWalkableType\":\"ACTIVITY\"," +
                "\"coursewareAction\":\"CREATED\"" +
                "}," +
                "{" +
                "\"elementId\":\"" + elementId + "\"," +
                "\"createdAt\":\"" + updatedTimestamp + "\"," +
                "\"givenName\":\"BB\"," +
                "\"familyName\":\"King\"," +
                "\"primaryEmail\":\"bb.king@blues.com\"," +
                "\"avatarSmall\":\"avatar\"," +
                "\"onElementId\":\"" + onElementId + "\"," +
                "\"onParentWalkableId\":\"" + parentWalkableId + "\"," +
                "\"accountId\":\"" + accountId + "\"," +
                "\"onElementType\":\"INTERACTIVE\"," +
                "\"onParentWalkableType\":\"ACTIVITY\"," +
                "\"coursewareAction\":\"UPDATED\"" +
                "}]" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_custom_limit() throws WriteResponseException {

        when(message.getElementId()).thenReturn(elementId);
        when(message.getLimit()).thenReturn(1);

        when(coursewareChangeLogService.fetchCoursewareChangeLogByElement(elementId, 1))
                .thenReturn(Flux.just(elementChangeLogPayloadOne));

        listElementChangeLogMessageHandler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByElement(elementId, 1);

        String expected = "{\"type\":\"project.courseware.changelog.list.ok\"," +
                "\"response\":{\"changelogs\":[" +
                "{" +
                "\"elementId\":\"" + elementId + "\"," +
                "\"createdAt\":\"" + createdTimestamp + "\"," +
                "\"givenName\":\"BB\"," +
                "\"familyName\":\"King\"," +
                "\"primaryEmail\":\"bb.king@blues.com\"," +
                "\"avatarSmall\":\"avatar\"," +
                "\"onElementId\":\"" + onElementId + "\"," +
                "\"onParentWalkableId\":\"" + parentWalkableId + "\"," +
                "\"accountId\":\"" + accountId + "\"," +
                "\"onElementType\":\"INTERACTIVE\"," +
                "\"onParentWalkableType\":\"ACTIVITY\"," +
                "\"coursewareAction\":\"CREATED\"" +
                "}]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handle_fail_default_limit() throws WriteResponseException {
        TestPublisher<ElementChangeLogPayload> error = TestPublisher.create();

        when(coursewareChangeLogService.fetchCoursewareChangeLogByElement(elementId, limit)).thenReturn(error.flux());
        error.error(new RuntimeException());

        when(message.getElementId()).thenReturn(elementId);
        when(message.getLimit()).thenReturn(null);
        listElementChangeLogMessageHandler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByElement(elementId, limit);

        String expected = "{\"" +
                "type\":\"project.courseware.changelog.list.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetching courseware element change logs\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_fail_custom_limit() throws WriteResponseException {
        TestPublisher<ElementChangeLogPayload> error = TestPublisher.create();

        when(coursewareChangeLogService.fetchCoursewareChangeLogByElement(elementId, 1)).thenReturn(error.flux());
        error.error(new RuntimeException());

        when(message.getElementId()).thenReturn(elementId);
        when(message.getLimit()).thenReturn(1);
        listElementChangeLogMessageHandler.handle(session, message);

        verify(coursewareChangeLogService, atLeastOnce()).fetchCoursewareChangeLogByElement(elementId, 1);

        String expected = "{\"" +
                "type\":\"project.courseware.changelog.list.error\"," +
                "\"code\":422," +
                "\"message\":\"error fetching courseware element change logs\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
