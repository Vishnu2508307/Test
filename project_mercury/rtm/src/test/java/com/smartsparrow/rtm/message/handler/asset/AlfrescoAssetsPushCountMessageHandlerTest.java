package com.smartsparrow.rtm.message.handler.asset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.workspace.service.AlfrescoAssetPushService;

import reactor.core.publisher.Mono;

class AlfrescoAssetsPushCountMessageHandlerTest {

    @InjectMocks
    private AlfrescoAssetsPushCountMessageHandler alfrescoAssetsPushCountMessageHandler;

    @Mock
    private ActivityGenericMessage message;

    @Mock
    private AlfrescoAssetPushService alfrescoAssetPushService;

    @Mock
    private CoursewareService coursewareService;

    private static final UUID courseId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp(){

        MockitoAnnotations.openMocks(this);

        when(message.getActivityId()).thenReturn(courseId);
    }

    @Test
    void validate_noCourseId() {

        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetsPushCountMessageHandler.validate(message));

        assertNotNull(iaf);
        assertEquals("course id is required", iaf.getMessage());
    }

    @Test
    void validate_notARootActivity() {

        when(coursewareService.getPath(courseId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(
                        CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.ACTIVITY),
                        CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY)
                )));

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class, () -> alfrescoAssetsPushCountMessageHandler.validate(message));

        assertNotNull(iaf);
        assertEquals("invalid root activity id", iaf.getMessage());
    }

    @Test
    void handle_success() throws WriteResponseException {

        when(alfrescoAssetPushService.getAeroImageAssetCount(eq(courseId)))
                .thenReturn(Mono.just(3L));

        alfrescoAssetsPushCountMessageHandler.handle(session, message);

        verify(alfrescoAssetPushService).getAeroImageAssetCount(eq(courseId));

        final String expected = "{" +
                "\"type\":\"author.alfresco.assets.push.count.ok\"," +
                "\"response\":{" +
                    "\"pushAssetCount\":3" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}