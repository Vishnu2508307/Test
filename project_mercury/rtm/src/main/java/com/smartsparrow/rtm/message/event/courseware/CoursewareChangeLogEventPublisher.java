package com.smartsparrow.rtm.message.event.courseware;

import static com.smartsparrow.dataevent.RouteUri.ACTIVITY_CHANGELOG_EVENT;
import static com.smartsparrow.dataevent.RouteUri.PROJECT_CHANGELOG_EVENT;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.ChangeLogByElement;
import com.smartsparrow.courseware.data.ChangeLogByProject;
import com.smartsparrow.courseware.data.CoursewareChangeLog;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.ActivityChangeLogEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareChangeLogBroadcastMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.eventmessage.ProjectChangeLogEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.data.ProjectActivity;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This event publisher is responsible for saving the changelog entries to the database
 * TODO this changelog event publisher could be use to broadcast the subscription changes when implemented
 * TODO or maybe better to have another publisher. Either way looking at this is a good starting point
 */
public class CoursewareChangeLogEventPublisher extends SimpleEventPublisher<CoursewareElementBroadcastMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareChangeLogEventPublisher.class);

    private final CoursewareService coursewareService;
    private final ActivityService activityService;
    private final CoursewareChangeLogService coursewareChangeLogService;

    @Inject
    public CoursewareChangeLogEventPublisher(final CoursewareService coursewareService,
                                             final ActivityService activityService,
                                             final CoursewareChangeLogService coursewareChangeLogService) {
        this.coursewareService = coursewareService;
        this.activityService = activityService;
        this.coursewareChangeLogService = coursewareChangeLogService;
    }

    @Override
    public void publish(RTMClient rtmClient, CoursewareElementBroadcastMessage data) {
        final UUID accountId = data.getAccountId();
        final CoursewareElement onElement = data.getElement();
        //if courseware action is deleted, then look up the path using parent element.
        final CoursewareElement onTargetElement = data.getAction().equals(CoursewareAction.DELETED) ? data.getParentElement() : data.getElement();

        // onTargetElement would be null if courseware action is deleted and element is root element
        if (onTargetElement != null) {
            // find all the ancestor for this element
            final Mono<List<CoursewareElement>> parentWalkables = coursewareService.getPath(onTargetElement.getElementId(), onTargetElement.getElementType())
                    .map(elements -> elements.stream()
                            // filter out the pathways from this list and return only walkables
                            .filter(element -> !element.getElementType().equals(CoursewareElementType.PATHWAY))
                            .collect(Collectors.toList()));

            final Flux<CoursewareChangeLog> changeLogs = buildCoursewareChangelog(data, accountId, onElement, parentWalkables);

            // subscribe so the changelogs can be persisted
            changeLogs.collectList()
                    .subscribe(coursewareChangeLogs -> {
                        emitEvents(rtmClient, data, coursewareChangeLogs);
                    }, ex -> {
                        ex = Exceptions.unwrap(ex);
                        log.error("could not publish changelog event {} {}", data.getAction(), ex.getMessage());
                    });
        } else {
            //In case of root element and courseware action as deleted, element id would be project id for creating courseware change log for project
            final Mono<? extends CoursewareChangeLog> projectChangelogMono = coursewareChangeLogService.createCoursewareChangeLogForProject(
                    data.getProjectId(),
                    onElement,
                    null,
                    data.getAction(),
                    accountId
            );
            projectChangelogMono.subscribe(coursewareChangeLog -> {
                emitEventsRootActivity(rtmClient, data, coursewareChangeLog);
            }, ex -> {
                ex = Exceptions.unwrap(ex);
                log.error("could not publish changelog event {} {}", data.getAction(), ex.getMessage());
            });
        }
    }

    /**
     * Create courseware changelog for element and project.
     * @param data  the courseware element broadcast message data
     * @param accountId  the account id.
     * @param onElement   on element id.
     * @param parentWalkables  list of courseware element of parent walakable .
     * @return Flux<CoursewareChangeLog> Flux of courseware change log for project and element
     */
    private Flux<CoursewareChangeLog> buildCoursewareChangelog(CoursewareElementBroadcastMessage data, UUID accountId, CoursewareElement onElement, Mono<List<CoursewareElement>> parentWalkables) {
        return parentWalkables
                        .map(walkables -> {
                            // find the parent walkable for this element (the element could be a pathway)
                            final CoursewareElement onParentWalkable = findParentWalkable(walkables, onElement);

                            final CoursewareElement rootActivity = walkables.get(0);
                            // find the project id for the root element (it could be null ATM pending frontend implementation)
                            final Mono<CoursewareChangeLog> projectChangelogMono = activityService.findProjectIdByActivity(rootActivity.getElementId())
                                    .defaultIfEmpty(new ProjectActivity())
                                    // FIXME attempt to save the project changelog, this is a temp behaviour until the frontend has
                                    // FIXME implemented all the project services and the feature is released
                                    .flatMap(projectActivity -> {
                                        if (projectActivity.getProjectId() != null) {
                                            // persist the project changelog
                                            return coursewareChangeLogService.createCoursewareChangeLogForProject(
                                                    projectActivity.getProjectId(),
                                                    onElement,
                                                    onParentWalkable,
                                                    data.getAction(),
                                                    accountId
                                            );
                                        }
                                        // return an empty non persisted project changelog
                                        return Mono.just(new ChangeLogByProject());
                                    });

                            final Flux<? extends CoursewareChangeLog> elementChangeLogs = walkables.stream()
                                    .map(walkable -> coursewareChangeLogService.createCoursewareChangeLogForElement(
                                            walkable.getElementId(),
                                            onElement,
                                            onParentWalkable,
                                            data.getAction(),
                                            accountId
                                    ).flux())
                                    .reduce(Flux::concatWith)
                                    .orElse(Flux.empty());

                            return projectChangelogMono
                                    .flux()
                                    .concatWith(elementChangeLogs);
                        })
                        .flatMapMany(changeLog -> changeLog)
                        // filter out the (dummy) defaulted change logs
                        .filter(changeLog -> changeLog.getId() != null);
    }

    private void emitEvents(final RTMClient rtmClient, CoursewareElementBroadcastMessage data,
                            final List<CoursewareChangeLog> coursewareChangeLogs) {
        // find the project changelog (there should always be 1)
        CoursewareChangeLog projectChangelog = coursewareChangeLogs.stream()
                .filter(changelog -> changelog instanceof ChangeLogByProject)
                .findFirst()
                .orElse(null);

        // find the root activity changelog, it could be null if the root activity has been deleted
        CoursewareChangeLog rootElementChangelog = coursewareChangeLogs.stream()
                .filter(changelog -> changelog instanceof ChangeLogByElement)
                // changelogs are ordered and the first in the list is for the root activity
                // it is important to keep the ordering or this logic falls apart
                .findFirst()
                .orElse(null);

        if (projectChangelog != null) {
            ProjectChangeLogEventMessage projectChangeLogEventMessage = new ProjectChangeLogEventMessage(((ChangeLogByProject) projectChangelog).getProjectId())
                    .setContent(new CoursewareChangeLogBroadcastMessage()
                            .setCoursewareChangeLog(projectChangelog))
                    .setProducingClientId(rtmClient.getRtmClientContext().getClientId());

            // emit the project changelog event for listening subscriptions
            Mono.just(projectChangeLogEventMessage)
                    .map(event -> getCamel().toStream(PROJECT_CHANGELOG_EVENT, event)) //
                    .subscribe();
        }

        if (rootElementChangelog != null) {
            ActivityChangeLogEventMessage activityChangeLogEventMessage = new ActivityChangeLogEventMessage(((ChangeLogByElement) rootElementChangelog).getElementId())
                    .setContent(new CoursewareChangeLogBroadcastMessage()
                            .setCoursewareChangeLog(rootElementChangelog))
                    .setProducingClientId(rtmClient.getRtmClientContext().getClientId());

            // emit the activity changelog for listening subscriptions
            Mono.just(activityChangeLogEventMessage)
                    .map(event -> getCamel().toStream(ACTIVITY_CHANGELOG_EVENT, event)) //
                    .subscribe();
        }

        if (projectChangelog == null) {
            // TODO update this log line once projects are going live
            log.warn("found unexpected null project changelog, this is a temporary behaviour {}", data.toString());
        }
    }

    /**
     * Traverse the walkable list backwards and finds the first parent walkable in the list
     *
     * @param walkables a list of walkables from the root element to the last ancestor
     * @param onElement the element to find the parent for
     * @return either the parent walkable or null
     */
    private CoursewareElement findParentWalkable(final List<CoursewareElement> walkables, final CoursewareElement onElement) {
        ListIterator<CoursewareElement> listIterator = walkables.listIterator(walkables.size());

        while (listIterator.hasPrevious()) {
            CoursewareElement prev = listIterator.previous();

            // skip the onElement, we need its parent
            if (prev.equals(onElement)) {
                continue;
            }

            if (CoursewareElementType.isAWalkable(prev.getElementType())) {
                return prev;
            }
        }

        return null;
    }


    private void emitEventsRootActivity(RTMClient rtmClient, CoursewareElementBroadcastMessage data, CoursewareChangeLog projectChangelog) {
        if (projectChangelog != null) {
            ProjectChangeLogEventMessage projectChangeLogEventMessage = new ProjectChangeLogEventMessage(((ChangeLogByProject) projectChangelog).getProjectId())
                    .setContent(new CoursewareChangeLogBroadcastMessage()
                            .setCoursewareChangeLog(projectChangelog))
                    .setProducingClientId(rtmClient.getRtmClientContext().getClientId());

            // emit the project changelog event for listening subscriptions
            Mono.just(projectChangeLogEventMessage)
                    .map(event -> getCamel().toStream(PROJECT_CHANGELOG_EVENT, event)) //
                    .subscribe();
        }
        if (projectChangelog == null) {
            // TODO update this log line once projects are going live
            log.warn("found unexpected null project changelog, this is a temporary behaviour {}", data.toString());
        }
    }
}
