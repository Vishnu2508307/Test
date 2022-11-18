package com.smartsparrow.rtm.message.event.courseware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.message.event.lang.EventPublisherException;
import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ActivityChangeEventPublisherTest {

    @InjectMocks
    private ActivityChangeEventPublisher eventPublisher;

    @Mock
    private CoursewareService coursewareService;

    private CoursewareElement element;
    private CoursewareElement parent;
    private CoursewareElementBroadcastMessage data;
    private RTMClient rtmClient;
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID parentElementId = UUID.randomUUID();
    private static final UUID foundParentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        rtmClient = mock(RTMClient.class);
        element = CoursewareElement.from(elementId, CoursewareElementType.COMPONENT);
        parent = CoursewareElement.from(parentElementId, CoursewareElementType.ACTIVITY);
        data = mock(CoursewareElementBroadcastMessage.class);

        when(coursewareService.getParentActivityIds(any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Mono.just(Lists.newArrayList(foundParentId)));

        when(coursewareService.saveChange(any(UUID.class))).thenReturn(Mono.just(new ActivityChange()));
    }

    @Test
    void publish_errorGettingParentActivities() {
        TestPublisher<List<UUID>> activities = TestPublisher.create();
        activities.error(new RuntimeException());

        when(coursewareService.getParentActivityIds(any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(activities.mono());

        assertThrows(RuntimeException.class, () -> eventPublisher.publish(rtmClient, data));
    }

    @Test
    void publish_emptyListForParentActivities() {
        when(data.getElement()).thenReturn(element);
        when(data.getAction()).thenReturn(CoursewareAction.CREATED);

        when(coursewareService.getParentActivityIds(any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Mono.just(new ArrayList<>()));

        EventPublisherException e = assertThrows(EventPublisherException.class,
                () -> eventPublisher.publish(rtmClient, data));

        assertTrue(e.getMessage().contains("activities cannot be empty"));
    }

    @Test
    void publish_deleteAction() {
        when(data.getParentElement()).thenReturn(parent);
        when(data.getAction()).thenReturn(CoursewareAction.DELETED);

        ArgumentCaptor<UUID> elementIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<CoursewareElementType> elementTypeCaptor = ArgumentCaptor.forClass(CoursewareElementType.class);

        eventPublisher.publish(rtmClient, data);

        verify(coursewareService).getParentActivityIds(elementIdCaptor.capture(), elementTypeCaptor.capture());

        assertEquals(parentElementId, elementIdCaptor.getValue());
        assertEquals(CoursewareElementType.ACTIVITY, elementTypeCaptor.getValue());

        verify(coursewareService).saveChange(foundParentId);
    }

    @Test
    void publish_anyOtherAction() {
        when(data.getElement()).thenReturn(element);
        when(data.getAction()).thenReturn(CoursewareAction.CONFIG_CHANGE);

        ArgumentCaptor<UUID> elementIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<CoursewareElementType> elementTypeCaptor = ArgumentCaptor.forClass(CoursewareElementType.class);

        eventPublisher.publish(rtmClient, data);

        verify(coursewareService).getParentActivityIds(elementIdCaptor.capture(), elementTypeCaptor.capture());

        assertEquals(elementId, elementIdCaptor.getValue());
        assertEquals(CoursewareElementType.COMPONENT, elementTypeCaptor.getValue());

        verify(coursewareService).saveChange(foundParentId);
    }

}
