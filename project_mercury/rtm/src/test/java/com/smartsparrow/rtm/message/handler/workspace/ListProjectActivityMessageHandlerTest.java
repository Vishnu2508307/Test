package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import com.smartsparrow.courseware.data.ActivitySummary;
import com.smartsparrow.courseware.service.ActivitySummaryService;
import com.smartsparrow.rtm.message.recv.workspace.ProjectActivityListMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;

import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;


import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListProjectActivityMessageHandlerTest {

    @InjectMocks
    private ListProjectActivityMessageHandler handler;

    @Mock
    private ActivitySummaryService activitySummaryService;

    @Mock
    private ProjectActivityListMessage message;

    private static final UUID projectId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getProjectId()).thenReturn(projectId);
    }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("projectId is required", f.getMessage());
    }

    @Test
    void validate_success() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException {

        when(activitySummaryService.findActivitiesSummaryForProject(projectId, Collections.emptyList()))
                .thenReturn(Flux.just(new ActivitySummary().setCreator(new AccountPayload())
                ));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"project.activity.list.ok\"," +
                            "\"response\":{" +
                                "\"activities\":[{\"creator\":{}}]" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<ActivitySummary> activities = TestPublisher.create();
        activities.error(new RuntimeException("ahi ahi ahi"));

        when(activitySummaryService.findActivitiesSummaryForProject(projectId, Collections.emptyList())).thenReturn(activities.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"project.activity.list.error\",\"code\":422,\"message\":\"error fetching activities\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
