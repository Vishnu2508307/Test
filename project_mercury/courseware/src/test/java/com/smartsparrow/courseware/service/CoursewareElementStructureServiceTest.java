package com.smartsparrow.courseware.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CoursewareElementStructureServiceTest {

    @InjectMocks
    private CoursewareElementStructureService coursewareElementStructureService;

    @Mock
    private CoursewareService coursewareService;
    @Mock
    private ActivityService activityService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private InteractiveService interactiveService;

    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID interactiveId1 = UUID.randomUUID();
    private static final UUID interactiveId2 = UUID.randomUUID();
    private static final UUID componentId1 = UUID.randomUUID();
    private static final UUID componentId2 = UUID.randomUUID();
    private static final UUID componentId3 = UUID.randomUUID();
    private static final UUID componentId4 = UUID.randomUUID();
    private static final List<String> configFields = Collections.singletonList("title");

    /*
     * These tests have been built based on a course structure that looks like the following,
     * where A = Activity, I = Interactive, P = Pathway and C = Component
     *
     *          A
     *          |
     *          P
     *        /   \
     *       I     I
     *      / \   / \
     *     C   C C   C
     */

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(coursewareService.getRootElementId(any(), any())).thenReturn(Mono.just(activityId));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.just(Collections.singletonList(pathwayId)));
        when(activityService.findChildComponentIds(activityId)).thenReturn(Mono.just(Collections.emptyList()));
        when(interactiveService.findChildComponentIds(interactiveId1)).thenReturn(Mono.just(Arrays.asList(componentId1, componentId2)));
        when(interactiveService.findChildComponentIds(interactiveId2)).thenReturn(Mono.just(Arrays.asList(componentId3, componentId4)));
        when(coursewareService.fetchConfigurationFields(any(), any())).thenReturn(Flux.empty());

        WalkableChild child1 = new WalkableChild().setElementId(interactiveId1).setElementType(INTERACTIVE);
        WalkableChild child2 = new WalkableChild().setElementId(interactiveId2).setElementType(INTERACTIVE);

        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(Arrays.asList(child1, child2)));
    }

    @Test
    void getCoursewareElementStructureForRootElement() {
        CoursewareElementNode node = coursewareElementStructureService.getCoursewareElementStructure(activityId, ACTIVITY, configFields).block();

        assertNotNull(node);
        assertEquals(activityId, node.getElementId());
        assertEquals(ACTIVITY, node.getType());
        assertNull(node.getParentId());
        assertEquals(activityId, node.getTopParentId());

        CoursewareElementNode pathwayNode = node.getChildren().get(0);
        assertNotNull(pathwayNode);
        assertEquals(pathwayId, pathwayNode.getElementId());
        assertEquals(PATHWAY, pathwayNode.getType());
        assertEquals(activityId, pathwayNode.getParentId());
        assertEquals(activityId, pathwayNode.getTopParentId());

        CoursewareElementNode interactiveNode1 = pathwayNode.getChildren().get(0);
        assertNotNull(interactiveNode1);
        assertEquals(interactiveId1, interactiveNode1.getElementId());
        assertEquals(INTERACTIVE, interactiveNode1.getType());
        assertEquals(pathwayId, interactiveNode1.getParentId());
        assertEquals(activityId, interactiveNode1.getTopParentId());

        CoursewareElementNode componentNode1 = interactiveNode1.getChildren().get(0);
        assertNotNull(componentNode1);
        assertEquals(componentId1, componentNode1.getElementId());
        assertEquals(COMPONENT, componentNode1.getType());
        assertEquals(interactiveId1, componentNode1.getParentId());
        assertEquals(activityId, componentNode1.getTopParentId());

        CoursewareElementNode componentNode2 = interactiveNode1.getChildren().get(1);
        assertNotNull(componentNode2);
        assertEquals(componentId2, componentNode2.getElementId());
        assertEquals(COMPONENT, componentNode2.getType());
        assertEquals(interactiveId1, componentNode2.getParentId());
        assertEquals(activityId, componentNode2.getTopParentId());

        CoursewareElementNode interactiveNode2 = pathwayNode.getChildren().get(1);
        assertNotNull(interactiveNode2);
        assertEquals(interactiveId2, interactiveNode2.getElementId());
        assertEquals(INTERACTIVE, interactiveNode2.getType());
        assertEquals(pathwayId, interactiveNode2.getParentId());
        assertEquals(activityId, interactiveNode2.getTopParentId());

        CoursewareElementNode componentNode3 = interactiveNode2.getChildren().get(0);
        assertNotNull(componentNode3);
        assertEquals(componentId3, componentNode3.getElementId());
        assertEquals(COMPONENT, componentNode3.getType());
        assertEquals(interactiveId2, componentNode3.getParentId());
        assertEquals(activityId, componentNode3.getTopParentId());

        CoursewareElementNode componentNode4 = interactiveNode2.getChildren().get(1);
        assertNotNull(componentNode4);
        assertEquals(componentId4, componentNode4.getElementId());
        assertEquals(COMPONENT, componentNode4.getType());
        assertEquals(interactiveId2, componentNode4.getParentId());
        assertEquals(activityId, componentNode4.getTopParentId());
    }

    @Test
    void getCoursewareElementStructureForNonRootElement() {
        CoursewareElement activity = new CoursewareElement().setElementId(activityId).setElementType(ACTIVITY);
        CoursewareElement pathway = new CoursewareElement().setElementId(pathwayId).setElementType(PATHWAY);
        CoursewareElement interactive = new CoursewareElement().setElementId(interactiveId1).setElementType(INTERACTIVE);
        when(coursewareService.getPath(interactiveId1, INTERACTIVE)).thenReturn(Mono.just(Arrays.asList(activity, pathway, interactive)));

        CoursewareElementNode node = coursewareElementStructureService.getCoursewareElementStructure(interactiveId1, INTERACTIVE, configFields).block();

        assertNotNull(node);
        assertEquals(interactiveId1, node.getElementId());
        assertEquals(INTERACTIVE, node.getType());
        assertEquals(pathwayId, node.getParentId());
        assertEquals(activityId, node.getTopParentId());

        CoursewareElementNode componentNode1 = node.getChildren().get(0);
        assertNotNull(componentNode1);
        assertEquals(componentId1, componentNode1.getElementId());
        assertEquals(COMPONENT, componentNode1.getType());
        assertEquals(interactiveId1, componentNode1.getParentId());
        assertEquals(activityId, componentNode1.getTopParentId());

        CoursewareElementNode componentNode2 = node.getChildren().get(1);
        assertNotNull(componentNode2);
        assertEquals(componentId2, componentNode2.getElementId());
        assertEquals(COMPONENT, componentNode2.getType());
        assertEquals(interactiveId1, componentNode2.getParentId());
        assertEquals(activityId, componentNode2.getTopParentId());
    }
}
