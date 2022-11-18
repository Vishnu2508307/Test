package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.activity.GetActivityMessageHandler.AUTHOR_ACTIVITY_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.activity.GetActivityMessageHandler.AUTHOR_ACTIVITY_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.DeletedActivity;
import com.smartsparrow.exception.NotFoundFault;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetActivityMessageHandlerTest {

    @InjectMocks
    private GetActivityMessageHandler handler;
    @Mock
    private ActivityService activityService;
    private Session session;
    @Mock
    private ActivityGenericMessage message;

    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        when(message.getActivityId()).thenReturn(activityId);
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("activityId is missing", ex.getMessage());
        assertEquals("BAD_REQUEST", ex.getType());
        assertEquals(400, ex.getResponseStatusCode());
    }

    @Test
    void validate_activityNotFound() {
        when(activityService.findById(message.getActivityId())).thenThrow(ActivityNotFoundException.class);

        NotFoundFault t = assertThrows(NotFoundFault.class, () -> handler.validate(message));
        assertEquals("Activity not found", t.getMessage());
        assertEquals("NOT_FOUND", t.getType());
        assertEquals(404, t.getResponseStatusCode());
    }

    @Test
    void validate_deletedActivity() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        when(activityService.fetchDeletedActivityById(message.getActivityId())).thenReturn(Mono.just(new DeletedActivity()));

        NotFoundFault t = assertThrows(NotFoundFault.class, () -> handler.validate(message));
        assertEquals("Activity not found", t.getMessage());
        assertEquals("NOT_FOUND", t.getType());
        assertEquals(404, t.getResponseStatusCode());
    }

    @Test
    void handle() throws IOException {
        ActivityPayload payload = ActivityPayload.from(new Activity().setId(activityId),
                                                       new ActivityConfig(),
                                                       new PluginSummary(),
                                                       new AccountPayload(),
                                                       new ActivityTheme(),
                                                       new ArrayList<>(),
                                                       new ArrayList<>(),
                                                       new CoursewareElementDescription(),
                                                       new ArrayList<>(),
                                                       new ThemePayload(),
                                                       Collections.emptyList());
        when(activityService.getActivityPayload(eq(activityId))).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ACTIVITY_GET_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("activity"));
                assertEquals(activityId.toString(), responseMap.get("activityId"));
                assertNotNull(responseMap.get("createdAt"));
            });
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new ActivityNotFoundException(activityId)).mono();
        when(activityService.getActivityPayload(eq(activityId))).thenReturn(mono);

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_ACTIVITY_GET_ERROR + "\",\"code\":404," +
                "\"message\":\"Can't find activity\"}");
    }
}
