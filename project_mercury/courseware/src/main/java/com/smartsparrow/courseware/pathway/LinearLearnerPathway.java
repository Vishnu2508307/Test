package com.smartsparrow.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.progress.LinearPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import io.leangen.graphql.annotations.GraphQLIgnore;
import reactor.core.publisher.Flux;

/**
 * A linear pathway for a learner.
 *
 * Supplies:
 *      - This will provide the next available walkable (only one) which has not been completed.
 *
 *      For example:
 *          All walkables: A, B, C, D
 *          Completed: A, B
 *          Supplies: C then D
 *
 *          All walkables: A, B, C, D
 *          Completed: A, B, C
 *          (new courseware published)
 *          All walkables: A, B, X, C, D, Y
 *          Completed: A, B, C
 *          Supplies: X then D then Y.
 *
 */
public class LinearLearnerPathway extends LinearPathway implements LearnerPathway {

    private UUID deploymentId;
    private UUID changeId;
    private String config;

    private final LearnerPathwayService learnerPathwayService;
    private final ProgressService progressService;

    @Inject
    LinearLearnerPathway(LearnerPathwayService learnerPathwayService, ProgressService progressService) {
        super();
        this.learnerPathwayService = learnerPathwayService;
        this.progressService = progressService;
    }

    // overriden for set method chaining.
    @Override
    LinearLearnerPathway setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    @GraphQLIgnore
    public UUID getDeploymentId() {
        return deploymentId;
    }

    LinearLearnerPathway setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    @GraphQLIgnore
    public UUID getChangeId() {
        return changeId;
    }

    LinearLearnerPathway setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    /**
     * Supply the next walkable.
     *
     * @param studentId the student id
     * @return the next walkable for this student
     */
    @Override
    public Flux<WalkableChild> supplyRelevantWalkables(final UUID studentId) {
        // A linear pathway is depends on the student's progress; find that.
        return progressService.findLatestLinearPathway(deploymentId, getId(), studentId)
                .defaultIfEmpty(new LinearPathwayProgress().setCompletedWalkables(Lists.newArrayList()))
                // get the current list of walkables.
                .flatMapMany(progress -> learnerPathwayService.findWalkables(getId(), getDeploymentId())
                        // filter for ones already seen if progress exists, otherwise just return first
                        .filter(walkableChild -> progress.getId() == null || !progress.getCompletedWalkables().contains(walkableChild.getElementId())))
                //choose the first
                .next().flux();
    }

    public LinearLearnerPathway setConfig(String config) {
        this.config = config;
        return this;
    }

    @Nullable
    @Override
    public String getConfig() {
        return config;
    }

    @Override
    public LinearLearnerPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        super.setPreloadPathway(preloadPathway);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LinearLearnerPathway that = (LinearLearnerPathway) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId, config);
    }

    @Override
    public String toString() {
        return "LinearLearnerPathway{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                ", learnerPathwayService=" + learnerPathwayService +
                ", progressService=" + progressService +
                '}';
    }
}
