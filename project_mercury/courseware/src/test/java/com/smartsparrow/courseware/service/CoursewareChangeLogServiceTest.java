package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ChangeLogByElement;
import com.smartsparrow.courseware.data.ChangeLogByProject;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareChangeLogGateway;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.payload.ElementChangeLogPayload;
import com.smartsparrow.courseware.payload.ProjectChangeLogPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CoursewareChangeLogServiceTest {

    @InjectMocks
    private CoursewareChangeLogService coursewareChangeLogService;

    @Mock
    private CoursewareChangeLogGateway coursewareChangeLogGateway;

    @Mock
    private AccountService accountService;

    @Mock
    private CoursewareService coursewareService;

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID onElementId = UUID.randomUUID();
    private static final Integer limit = 50;
    private final CoursewareElementType onElementType = CoursewareElementType.INTERACTIVE;
    private final UUID accountId = UUID.randomUUID();
    private final CoursewareAction coursewareAction = CoursewareAction.CREATED;
    private final UUID parentWalkableId = UUID.randomUUID();
    private final CoursewareElementType parentWalkableType = CoursewareElementType.PATHWAY;
    private final CoursewareElement onElement = CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.COMPONENT);
    private final CoursewareElement onParentWalkable = CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.INTERACTIVE);
    AccountPayload accountPayload;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        accountPayload = new AccountPayload().setFamilyName("joe")
                .setGivenName("Ben")
                .setAvatarSmall("aa")
                .setPrimaryEmail("ben.joe@");

        when(coursewareChangeLogGateway.fetchChangeLogByElement(elementId, limit)).thenReturn(Flux.empty());

        when(coursewareChangeLogGateway.persist(any(ChangeLogByElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(coursewareChangeLogGateway.persist(any(ChangeLogByProject.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(coursewareService.fetchConfigurationFields(any(UUID.class), any(List.class)))
                .thenReturn(Flux.just(new ConfigurationField()
                .setFieldValue("Paint it Black")));

        when(accountService.getAccountPayload(accountId)).thenReturn(Mono.just(accountPayload));
    }

    @Test
    void fetchChangeLogByElement_noElementId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService
                        .fetchCoursewareChangeLogByElement(null,limit));

        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void fetchChangeLogByElement_empty() {
        when(coursewareChangeLogGateway.fetchChangeLogByElement(any(UUID.class), any(Integer.class))).thenReturn(Flux.empty());
        ElementChangeLogPayload elementChangeLogPayload = coursewareChangeLogService.fetchCoursewareChangeLogByElement(elementId,limit).blockFirst();
        assertNull(elementChangeLogPayload);
    }

    @Test
    void fetchChangeLogByElement_valid() {
        final UUID changeLogIdOne = UUIDs.timeBased();
        final UUID changeLogIdTwo = UUIDs.timeBased();

        ChangeLogByElement one = new ChangeLogByElement()
                .setElementId(elementId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setCoursewareAction(coursewareAction)
                .setOnParentWalkableId(parentWalkableId)
                .setOnParentWalkableType(parentWalkableType)
                .setId(changeLogIdOne);

        ChangeLogByElement two = new ChangeLogByElement()
                .setElementId(elementId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setCoursewareAction(coursewareAction)
                .setOnParentWalkableId(parentWalkableId)
                .setOnParentWalkableType(parentWalkableType)
                .setId(changeLogIdTwo);

        when(coursewareChangeLogGateway.fetchChangeLogByElement(elementId, 50))
                .thenReturn(Flux.just(one, two));


        List<ElementChangeLogPayload> all = coursewareChangeLogService.fetchCoursewareChangeLogByElement(elementId, limit)
                .collectList()
                .block();
        assertNotNull(all);
        assertEquals(2, all.size());

        // ensure ordering is kept
        assertEquals(changeLogIdOne, all.get(0).getId());
        assertEquals(changeLogIdTwo, all.get(1).getId());
    }

    @Test
    void fetchChangeLogByProject_noProjectId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService
                        .fetchCoursewareChangeLogByProject(null,limit));

        assertEquals("projectId is required", ex.getMessage());
    }

    @Test
    void fetchChangeLogByProject_empty() {
        when(coursewareChangeLogGateway.fetchChangeLogByProject(any(UUID.class), any(Integer.class))).thenReturn(Flux.empty());
        ProjectChangeLogPayload projectChangeLogPayload = coursewareChangeLogService.fetchCoursewareChangeLogByProject(projectId ,limit).blockFirst();
        assertNull(projectChangeLogPayload);
    }

    @Test
    void fetchChangeLogByProject_valid() {
        final UUID changeLogIdOne = UUIDs.timeBased();
        final UUID changeLogIdTwo = UUIDs.timeBased();
        ChangeLogByProject one = new ChangeLogByProject()
                .setProjectId(projectId)
                .setOnElementId(onElementId)
                .setOnElementType(onElementType)
                .setAccountId(accountId)
                .setCoursewareAction(coursewareAction)
                .setId(changeLogIdOne);
        ChangeLogByProject two = new ChangeLogByProject()
                .setProjectId(projectId)
                .setOnElementId(onElementId)
                .setOnElementType(CoursewareElementType.FEEDBACK)
                .setAccountId(accountId)
                .setCoursewareAction(CoursewareAction.CREATED)
                .setId(changeLogIdTwo);
        when(coursewareChangeLogGateway.fetchChangeLogByProject(projectId, limit))
                .thenReturn(Flux.just(one, two));

        List<ProjectChangeLogPayload> all = coursewareChangeLogService.fetchCoursewareChangeLogByProject(projectId, limit)
                .collectList()
                .block();
        assertNotNull(all);
        assertEquals(2, all.size());

        // ensure ordering is kept
        assertEquals(changeLogIdOne, all.get(0).getId());
        assertEquals(changeLogIdTwo, all.get(1).getId());

    }

    @Test
    void createChangeLogForProject_noElementId() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForProject(
                null,
                onElement,
                onParentWalkable,
                coursewareAction,
                accountId
        ));

        assertEquals("projectId is required", fault.getMessage());
    }

    @Test
    void createChangeLogForProject_noOnElement() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForProject(
                projectId,
                null,
                onParentWalkable,
                coursewareAction,
                accountId
        ));

        assertEquals("onElement is required", fault.getMessage());
    }

    @Test
    void createChangeLogForProject_noAccountId() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForProject(
                projectId,
                onElement,
                onParentWalkable,
                coursewareAction,
                null
        ));

        assertEquals("accountId is required", fault.getMessage());
    }

    @Test
    void createChangeLogForProject_noCoursewareAction() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForProject(
                projectId,
                onElement,
                onParentWalkable,
                null,
                accountId
        ));

        assertEquals("coursewareAction is required", fault.getMessage());
    }

    @Test
    void createChangeLogForProject_rootActivity() {
        ChangeLogByProject created = coursewareChangeLogService.createCoursewareChangeLogForProject(
                projectId,
                onElement,
                null,
                coursewareAction,
                accountId
        ).block();

        assertNotNull(created);
        assertEquals(projectId, created.getProjectId());
        assertEquals(onElement.getElementId(), created.getOnElementId());
        assertEquals(onElement.getElementType(), created.getOnElementType());
        assertEquals(coursewareAction, created.getCoursewareAction());
        assertEquals(accountId, created.getAccountId());
        assertEquals("Paint it Black", created.getOnElementTitle());
        assertNotNull(created.getId());
        assertNull(created.getOnParentWalkableTitle());
        assertNull(created.getOnParentWalkableId());
        assertNull(created.getOnParentWalkableType());
    }

    @Test
    void createChangeLogForProject() {
        ChangeLogByProject created = coursewareChangeLogService.createCoursewareChangeLogForProject(
                projectId,
                onElement,
                onParentWalkable,
                coursewareAction,
                accountId
        ).block();

        assertNotNull(created);
        assertEquals(projectId, created.getProjectId());
        assertEquals(onElement.getElementId(), created.getOnElementId());
        assertEquals(onElement.getElementType(), created.getOnElementType());
        assertEquals(coursewareAction, created.getCoursewareAction());
        assertEquals(accountId, created.getAccountId());
        assertEquals("Paint it Black", created.getOnElementTitle());
        assertEquals("Paint it Black", created.getOnParentWalkableTitle());
        assertNotNull(created.getId());
        assertEquals(onParentWalkable.getElementId(), created.getOnParentWalkableId());
        assertEquals(onParentWalkable.getElementType(), created.getOnParentWalkableType());
    }

    @Test
    void createChangeLogForElement_noElementId() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForElement(
                null,
                onElement,
                onParentWalkable,
                coursewareAction,
                accountId
        ));

        assertEquals("elementId is required", fault.getMessage());
    }

    @Test
    void createChangeLogForElement_noOnElement() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForElement(
                elementId,
                null,
                onParentWalkable,
                coursewareAction,
                accountId
        ));

        assertEquals("onElement is required", fault.getMessage());
    }

    @Test
    void createChangeLogForElement_noAccountId() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForElement(
                elementId,
                onElement,
                onParentWalkable,
                coursewareAction,
                null
        ));

        assertEquals("accountId is required", fault.getMessage());
    }

    @Test
    void createChangeLogForElement_noCoursewareAction() {
        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () -> coursewareChangeLogService.createCoursewareChangeLogForElement(
                elementId,
                onElement,
                onParentWalkable,
                null,
                accountId
        ));

        assertEquals("coursewareAction is required", fault.getMessage());
    }

    @Test
    void createChangeLogForElement_rootActivity() {
        ChangeLogByElement created = coursewareChangeLogService.createCoursewareChangeLogForElement(
                elementId,
                onElement,
                null,
                coursewareAction,
                accountId
        ).block();

        assertNotNull(created);
        assertEquals(elementId, created.getElementId());
        assertEquals(onElement.getElementId(), created.getOnElementId());
        assertEquals(onElement.getElementType(), created.getOnElementType());
        assertEquals(coursewareAction, created.getCoursewareAction());
        assertEquals(accountId, created.getAccountId());
        assertNotNull(created.getId());
        assertNull(created.getOnParentWalkableId());
        assertNull(created.getOnParentWalkableType());
    }

    @Test
    void createChangeLogForElement() {
        ChangeLogByElement created = coursewareChangeLogService.createCoursewareChangeLogForElement(
                elementId,
                onElement,
                onParentWalkable,
                coursewareAction,
                accountId
        ).block();

        assertNotNull(created);
        assertEquals(elementId, created.getElementId());
        assertEquals(onElement.getElementId(), created.getOnElementId());
        assertEquals(onElement.getElementType(), created.getOnElementType());
        assertEquals(coursewareAction, created.getCoursewareAction());
        assertEquals(accountId, created.getAccountId());
        assertNotNull(created.getId());
        assertEquals(onParentWalkable.getElementId(), created.getOnParentWalkableId());
        assertEquals(onParentWalkable.getElementType(), created.getOnParentWalkableType());
    }
}
