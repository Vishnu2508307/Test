package com.smartsparrow.courseware.service;


import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivitySummary;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.payload.PluginRefPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ProjectGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ActivitySummaryService {

    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final ProjectGateway projectGateway;
    private final AccountService accountService;
    private final PluginService pluginService;


    @Inject
    public ActivitySummaryService(ActivityService activityService,
                                  CoursewareService coursewareService,
                                  ProjectGateway projectGateway,
                                  AccountService accountService,
                                  PluginService pluginService) {
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.projectGateway = projectGateway;
        this.accountService = accountService;
        this.pluginService = pluginService;
    }


    /**
     * Find all the root activities that belong to a project
     *
     * @param projectId  the project id to find the activities for
     * @param fieldNames the activity titles.
     * @return a flux of activity summary
     */
    @Trace(async = true)
    public Flux<ActivitySummary> findActivitiesSummaryForProject(final UUID projectId, final List<String> fieldNames) {
        affirmArgument(projectId != null, "projectId is missing");

        return projectGateway.findActivities(projectId)
                .flatMap(projectActivity -> this.getActivitySummary(projectActivity.getActivityId(), fieldNames))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds a summary object for an activity
     * @param activityId the activity id
     * @param fieldNames the activity titles.
     * @throws ActivityNotFoundException if no activity found for the given id
     */
    @Trace(async = true)
    private Mono<ActivitySummary> getActivitySummary(final UUID activityId, final List<String> fieldNames) {
        checkArgument(activityId != null, "missing activity id");

        return activityService.findById(activityId)
                .flatMap(activity -> getActivitySummary(activity, fieldNames))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds a summary object for an activity.
     *
     * @param activity   the activity
     * @param fieldNames activity titles
     * @return activity summary
     */
    @Trace(async = true)
    private Mono<ActivitySummary> getActivitySummary(final Activity activity, final List<String> fieldNames) {
        checkArgument(activity != null, "missing activity");

        Mono<AccountPayload> creator = accountService.getAccountPayload(activity.getCreatorId())
                .defaultIfEmpty(new AccountPayload())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<UUID> updatedAtTimeuuid = activityService.findLatestConfigId(activity.getId())
                // we use the latest config id to display the updatedAt timestamp
                // should the configId be missing then updatedAt and createdAt are the same
                .defaultIfEmpty(activity.getId())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<List<ConfigurationField>> configFields = coursewareService.fetchConfigurationFields(activity.getId(),
                fieldNames).collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<PluginSummary> plugin = pluginService.fetchById(activity.getPluginId())
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new PluginNotFoundFault(activity.getPluginId());
                });
        Mono<List<PluginFilter>> pluginFilters = pluginService.fetchPluginFiltersByIdVersionExpr(activity.getPluginId(), activity.getPluginVersionExpr());


        return Mono.zip(creator, updatedAtTimeuuid, configFields, plugin, pluginFilters)
                .map(tuple5 -> new ActivitySummary()
                        .setActivityId(activity.getId())
                        .setCreator(tuple5.getT1())
                        .setCreatedAt(activity.getId() == null ? null : DateFormat.asRFC1123(activity.getId()))
                        .setUpdatedAt(DateFormat.asRFC1123(tuple5.getT2()))
                        .setConfigFields(tuple5.getT3())
                        .setPlugin(PluginRefPayload.from(tuple5.getT4(), activity.getPluginVersionExpr(), tuple5.getT5())));
    }
}
