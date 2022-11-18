package com.smartsparrow.courseware.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.ChangeLogByElement;
import com.smartsparrow.courseware.data.ChangeLogByProject;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareChangeLogGateway;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.payload.ElementChangeLogPayload;
import com.smartsparrow.courseware.payload.ProjectChangeLogPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareChangeLogService {

    private static final Logger log = LoggerFactory.getLogger(CoursewareChangeLogService.class);

    private final CoursewareChangeLogGateway coursewareChangeLogGateway;
    private final AccountService accountService;
    private final CoursewareService coursewareService;

    @Inject
    public CoursewareChangeLogService(final CoursewareChangeLogGateway coursewareChangeLogGateway,
                                      final AccountService accountService,
                                      final CoursewareService coursewareService) {
        this.coursewareChangeLogGateway = coursewareChangeLogGateway;
        this.accountService = accountService;
        this.coursewareService = coursewareService;
    }

    /**
     * fetch courseware changelog for project.
     *
     * @param projectId the project id.
     * @return {@link Flux<ChangeLogByProject>} Flux of change logs for project
     */
    public Flux<ProjectChangeLogPayload> fetchCoursewareChangeLogByProject(UUID projectId , Integer limit) {
        affirmArgument(projectId != null, "projectId is required");
        return coursewareChangeLogGateway.fetchChangeLogByProject(projectId, limit)
        .concatMap(this::getChangeLogForProjectPayload);
    }

    /**
     * get the courseware changelog for project
     * @param changeLogByProject the changelog for project object.
     * @return mono of change log by project payload.
     */
    public Mono<ProjectChangeLogPayload> getChangeLogForProjectPayload(ChangeLogByProject changeLogByProject) {
        Mono<AccountPayload> accountPayload = accountService.getAccountPayload(changeLogByProject.getAccountId());

        return Mono.zip(Mono.just(changeLogByProject), accountPayload)
                .map(tuple2 -> {
                    ChangeLogByProject projectChangeLog = tuple2.getT1();
                    AccountPayload accountInfo = tuple2.getT2();
                    return new ProjectChangeLogPayload()
                            .setId(projectChangeLog.getId())
                            .setProjectId(projectChangeLog.getProjectId())
                            .setCreatedAt(projectChangeLog.getId() == null ? null : DateFormat.asRFC1123(projectChangeLog.getId()))
                            .setOnElementId(projectChangeLog.getOnElementId())
                            .setOnElementType(projectChangeLog.getOnElementType())
                            .setOnParentWalkableId(projectChangeLog.getOnParentWalkableId())
                            .setOnParentWalkableType(projectChangeLog.getOnParentWalkableType())
                            .setCoursewareAction(projectChangeLog.getCoursewareAction())
                            .setAccountId(projectChangeLog.getAccountId())
                            .setGivenName(accountInfo.getGivenName())
                            .setFamilyName(accountInfo.getFamilyName())
                            .setAvatarSmall(accountInfo.getAvatarSmall())
                            .setPrimaryEmail(accountInfo.getPrimaryEmail())
                            .setOnElementTitle(projectChangeLog.getOnElementTitle())
                            .setOnParentWalkableTitle(projectChangeLog.getOnParentWalkableTitle());
                });
    }

    /**
     * Fetch courseware change log by courseware element id.
     *
     * @param elementId the courseware element id.
     * @return {@link Flux<ChangeLogByElement>} Flux of change log for element
     */
    public Flux<ElementChangeLogPayload> fetchCoursewareChangeLogByElement(UUID elementId , Integer limit) {
        affirmArgument(elementId != null, "elementId is required");
        return coursewareChangeLogGateway.fetchChangeLogByElement(elementId, limit)
                .concatMap(this::getChangeLogForElementPayload);
    }

    /**
     * get the courseware changelog for element
     * @param changeLogByElement the changelog for element object.
     * @return mono of change log for element payload.
     */
    public Mono<ElementChangeLogPayload> getChangeLogForElementPayload(ChangeLogByElement changeLogByElement) {
        Mono<AccountPayload> accountPayload = accountService.getAccountPayload(changeLogByElement.getAccountId());

        return Mono.zip(Mono.just(changeLogByElement), accountPayload)
                .map(tuple2 -> {
                    ChangeLogByElement elementChangeLog = tuple2.getT1();
                    AccountPayload accountInfo = tuple2.getT2();
                    return new ElementChangeLogPayload()
                            .setId(elementChangeLog.getId())
                            .setElementId(changeLogByElement.getElementId())
                            .setCreatedAt(elementChangeLog.getId() == null ? null : DateFormat.asRFC1123(elementChangeLog.getId()))
                            .setOnElementId(elementChangeLog.getOnElementId())
                            .setOnElementType(elementChangeLog.getOnElementType())
                            .setOnParentWalkableId(elementChangeLog.getOnParentWalkableId())
                            .setOnParentWalkableType(elementChangeLog.getOnParentWalkableType())
                            .setCoursewareAction(elementChangeLog.getCoursewareAction())
                            .setAccountId(elementChangeLog.getAccountId())
                            .setGivenName(accountInfo.getGivenName())
                            .setFamilyName(accountInfo.getFamilyName())
                            .setAvatarSmall(accountInfo.getAvatarSmall())
                            .setPrimaryEmail(accountInfo.getPrimaryEmail())
                            .setOnElementTitle(elementChangeLog.getOnElementTitle())
                            .setOnParentWalkableTitle(elementChangeLog.getOnParentWalkableTitle());
                });
    }

    /**
     * Persist a courseware changelog for an element
     *
     * @param elementId the element to track the changelog by
     * @param onElement the element that was changed
     * @param onParentWalkable the parent walkable that was changed
     * @param coursewareAction the courseware action
     * @param accountId the account that performed the courseware action
     * @return a mono with the created changelog entry
     */
    @SuppressWarnings("Duplicates")
    public Mono<ChangeLogByElement> createCoursewareChangeLogForElement(final UUID elementId,
                                                                        final CoursewareElement onElement,
                                                                        final CoursewareElement onParentWalkable,
                                                                        final CoursewareAction coursewareAction,
                                                                        final UUID accountId) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(onElement != null, "onElement is required");
        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(coursewareAction != null, "coursewareAction is required");

        final Mono<ConfigurationField> onElementTitle = getTitle(onElement);
        final Mono<ConfigurationField> onParentWalkableTitle = getTitle(onParentWalkable);

        final UUID changeLogId = UUIDs.timeBased();

        return Mono.zip(onElementTitle, onParentWalkableTitle)
                .flatMap(tuple2 -> {
                    final ChangeLogByElement changeLogByElement = new ChangeLogByElement()
                            .setElementId(elementId)
                            .setId(changeLogId)
                            .setAccountId(accountId)
                            .setCoursewareAction(coursewareAction)
                            .setOnElementId(onElement.getElementId())
                            .setOnElementType(onElement.getElementType())
                            // the following is null for root activities
                            .setOnParentWalkableId(onParentWalkable != null ? onParentWalkable.getElementId() : null)
                            .setOnParentWalkableType(onParentWalkable != null ? onParentWalkable.getElementType() : null)
                            .setOnElementTitle(tuple2.getT1().getFieldValue())
                            .setOnParentWalkableTitle(tuple2.getT2().getFieldValue());

                    return coursewareChangeLogGateway.persist(changeLogByElement)
                            .then(Mono.just(changeLogByElement));
                });
    }

    /**
     * Persist a courseware changelog for a project
     *
     * @param projectId the project to track the changelog by
     * @param onElement the element that was changed
     * @param onParentWalkable the parent walkable that was changed
     * @param coursewareAction the courseware action
     * @param accountId the account that performed the courseware action
     * @return a mono with the created changelog entry
     */
    @SuppressWarnings("Duplicates")
    public Mono<ChangeLogByProject> createCoursewareChangeLogForProject(final UUID projectId,
                                                                        final CoursewareElement onElement,
                                                                        final CoursewareElement onParentWalkable,
                                                                        final CoursewareAction coursewareAction,
                                                                        final UUID accountId) {

        affirmArgument(projectId != null, "projectId is required");
        affirmArgument(onElement != null, "onElement is required");
        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(coursewareAction != null, "coursewareAction is required");

        final Mono<ConfigurationField> onElementTitle = getTitle(onElement);
        final Mono<ConfigurationField> onParentWalkableTitle = getTitle(onParentWalkable);

        final UUID changeLogId = UUIDs.timeBased();

        return Mono.zip(onElementTitle, onParentWalkableTitle)
                .flatMap(tuple2 -> {
                    final ChangeLogByProject changeLogByProject = new ChangeLogByProject()
                            .setProjectId(projectId)
                            .setId(changeLogId)
                            .setOnElementId(onElement.getElementId())
                            .setAccountId(accountId)
                            .setOnElementType(onElement.getElementType())
                            .setCoursewareAction(coursewareAction)
                            // the following is null for root activities
                            .setOnParentWalkableId(onParentWalkable != null ? onParentWalkable.getElementId() : null)
                            .setOnParentWalkableType(onParentWalkable != null ? onParentWalkable.getElementType() : null)
                            .setOnElementTitle(tuple2.getT1().getFieldValue())
                            .setOnParentWalkableTitle(tuple2.getT2().getFieldValue());

                    return coursewareChangeLogGateway.persist(changeLogByProject)
                            .then(Mono.just(changeLogByProject));
                });
    }

    /**
     * Find the title for an elementId or return an empty configuration field
     *
     * @param element the element to find the title for
     * @return a configuration field
     */
    private Mono<ConfigurationField> getTitle(final CoursewareElement element) {
        if (element != null) {
            return coursewareService.fetchConfigurationFields(element.getElementId(), Lists.newArrayList(ConfigurationField.TITLE))
                    .singleOrEmpty()
                    .defaultIfEmpty(new ConfigurationField());
        }

        return Mono.just(new ConfigurationField());
    }
}
