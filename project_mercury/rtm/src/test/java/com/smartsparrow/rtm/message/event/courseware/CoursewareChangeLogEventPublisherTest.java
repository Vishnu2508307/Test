package com.smartsparrow.rtm.message.event.courseware;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSimpleEventPublisher;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ChangeLogByElement;
import com.smartsparrow.courseware.data.ChangeLogByProject;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.ActivityChangeLogEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.eventmessage.ProjectChangeLogEventMessage;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.workspace.data.ProjectActivity;

import reactor.core.publisher.Mono;

class CoursewareChangeLogEventPublisherTest {

    @InjectMocks
    private CoursewareChangeLogEventPublisher publisher;

    private CoursewareChangeLogEventPublisher spy;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ActivityService activityService;

    @Mock
    private CoursewareChangeLogService coursewareChangeLogService;

    @Mock
    private CoursewareElementBroadcastMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID courseId = UUID.randomUUID();
    private static final UUID lessonId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final List<CoursewareElement> path = Lists.newArrayList(
            CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY),
            CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
            CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
            CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.PATHWAY),
            CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE)
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getAccountId()).thenReturn(accountId);
        when(message.getAction()).thenReturn(CoursewareAction.CONFIG_CHANGE);
        when(message.getElement()).thenReturn(CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE));

        when(coursewareService.getPath(interactiveId, CoursewareElementType.INTERACTIVE))
                .thenReturn(Mono.just(path));

        when(activityService.findProjectIdByActivity(courseId))
                .thenReturn(Mono.just(new ProjectActivity()
                        .setActivityId(courseId)
                        .setProjectId(projectId)));

        when(coursewareChangeLogService.createCoursewareChangeLogForElement(
                any(UUID.class),
                any(CoursewareElement.class),
                any(CoursewareElement.class),
                any(CoursewareAction.class),
                any(UUID.class)
        )).thenReturn(Mono.just(new ChangeLogByElement()
                .setElementId(elementId)
                .setOnParentWalkableId(UUID.randomUUID())
                .setOnParentWalkableType(CoursewareElementType.ACTIVITY)
                .setId(UUID.randomUUID())));

        when(coursewareChangeLogService.createCoursewareChangeLogForElement(
                eq(courseId),
                any(CoursewareElement.class),
                any(CoursewareElement.class),
                any(CoursewareAction.class),
                any(UUID.class)
        )).thenReturn(Mono.just(new ChangeLogByElement()
                .setElementId(courseId)
                .setId(UUID.randomUUID())));

        when(coursewareChangeLogService.createCoursewareChangeLogForProject(
                any(UUID.class),
                any(CoursewareElement.class),
                any(CoursewareElement.class),
                any(CoursewareAction.class),
                any(UUID.class)
        )).thenReturn(Mono.just(new ChangeLogByProject()
                .setProjectId(projectId)
                .setId(UUID.randomUUID())));

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");

        spy = mockSimpleEventPublisher(publisher);
    }

    @Test
    void publish_withProject() {
        ArgumentCaptor<ActivityChangeLogEventMessage> captor = ArgumentCaptor.forClass(ActivityChangeLogEventMessage.class);

        spy.publish(rtmClient, message);

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                interactiveId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                lessonId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                courseId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForProject(
                projectId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(spy.getCamel(), times(1)).toStream(anyString(), any(ProjectChangeLogEventMessage.class));
        verify(spy.getCamel(), atLeastOnce()).toStream(anyString(), captor.capture());

        ActivityChangeLogEventMessage activityChangelogEventMessage = captor.getValue();

        assertNotNull(activityChangelogEventMessage);
        assertTrue(activityChangelogEventMessage.getName().contains(courseId.toString()));

    }

    @Test
    void publish_noProject() {
        when(activityService.findProjectIdByActivity(courseId))
                .thenReturn(Mono.empty());

        spy.publish(rtmClient, message);

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                interactiveId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                lessonId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                courseId,
                CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                CoursewareElement.from(lessonId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, never()).createCoursewareChangeLogForProject(
                any(UUID.class),
                any(CoursewareElement.class),
                any(CoursewareElement.class),
                any(CoursewareAction.class),
                any(UUID.class)
        );

        verify(spy.getCamel(), never()).toStream(anyString(), any(ProjectChangeLogEventMessage.class));
        verify(spy.getCamel(), times(1)).toStream(anyString(), any(ActivityChangeLogEventMessage.class));
    }

    @Test
    void publish_noProject_rootActivity() {
        final List<CoursewareElement> path = Lists.newArrayList(
                CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY)
        );
        when(message.getElement()).thenReturn(CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY));
        when(coursewareService.getPath(courseId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(path));
        when(activityService.findProjectIdByActivity(courseId))
                .thenReturn(Mono.empty());

        when(coursewareChangeLogService.createCoursewareChangeLogForElement(
                any(UUID.class),
                any(CoursewareElement.class),
                eq(null),
                any(CoursewareAction.class),
                any(UUID.class)
        )).thenReturn(Mono.just(new ChangeLogByElement()
                .setElementId(elementId)
                .setId(UUID.randomUUID())));

        spy.publish(rtmClient, message);

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                courseId,
                CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY),
                null,
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, never()).createCoursewareChangeLogForProject(
                any(UUID.class),
                any(CoursewareElement.class),
                any(CoursewareElement.class),
                any(CoursewareAction.class),
                any(UUID.class)
        );

        verify(spy.getCamel(), never()).toStream(anyString(), any(ProjectChangeLogEventMessage.class));
        verify(spy.getCamel(), times(1)).toStream(anyString(), any(ActivityChangeLogEventMessage.class));
    }

    @Test
    void publish_onElementIsAPathway() {
        final List<CoursewareElement> path = Lists.newArrayList(
                CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY),
                CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY));

        when(message.getElement()).thenReturn(CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY));

        when(coursewareService.getPath(pathwayId, CoursewareElementType.PATHWAY))
                .thenReturn(Mono.just(path));

        spy.publish(rtmClient, message);

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForElement(
                courseId,
                CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
                CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(coursewareChangeLogService, times(1)).createCoursewareChangeLogForProject(
                projectId,
                CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
                CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY),
                CoursewareAction.CONFIG_CHANGE,
                accountId
        );

        verify(spy.getCamel(), times(1)).toStream(anyString(), any(ProjectChangeLogEventMessage.class));
        verify(spy.getCamel(), times(1)).toStream(anyString(), any(ActivityChangeLogEventMessage.class));
    }

    @Test
    void publish_targetElementInteractive() {

    }

}