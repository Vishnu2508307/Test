package com.smartsparrow.rtm.message.event.courseware;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSimpleEventPublisher;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.PublishedActivityBroadcastMessage;
import com.smartsparrow.la.event.ContentSeedingMessage;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.rtm.ws.RTMClient;

import reactor.test.publisher.TestPublisher;

public class ContentSeedingEventPublisherTest {

    @InjectMocks
    ContentSeedingEventPublisher contentSeedingEventPublisher;

    private RTMClient rtmClient;
    private static final UUID elementId = UUID.randomUUID();
    private PublishedActivityBroadcastMessage data;
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private ContentSeedingEventPublisher spy;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        DeployedActivity deployedActivity = new DeployedActivity()
                .setActivityId(elementId)
                .setChangeId(changeId)
                .setCohortId(cohortId)
                .setId(deploymentId);
        rtmClient = mock(RTMClient.class);
        data = new PublishedActivityBroadcastMessage(cohortId)
                .setPublishedActivity(deployedActivity);
        spy = mockSimpleEventPublisher(contentSeedingEventPublisher);
    }

    @Test
    void publish_error() {
        TestPublisher publisher = TestPublisher.create();
        publisher.error(new RuntimeException("error"));
        PublishedActivityBroadcastMessage mock = mock(PublishedActivityBroadcastMessage.class);

        spy.publish(rtmClient, mock);

        verify(spy.getCamel(), never()).toStream(anyString(), any(ContentSeedingMessage.class));
    }

    @Test
    void publish_success() {
        ArgumentCaptor<ContentSeedingMessage> captor = ArgumentCaptor.forClass(ContentSeedingMessage.class);

        spy.publish(rtmClient, data);

        verify(spy.getCamel(), times(1)).toStream(anyString(), captor.capture());

        ContentSeedingMessage value = captor.getValue();

        assertNotNull(value);
        assertEquals(changeId, value.getChangeId());
        assertEquals(elementId, value.getElementId());
        assertEquals(cohortId, value.getCohortId());
        assertEquals(deploymentId, value.getDeploymentId());
        assertEquals(CoursewareElementType.ACTIVITY, value.getCoursewareElementType());
    }
}
