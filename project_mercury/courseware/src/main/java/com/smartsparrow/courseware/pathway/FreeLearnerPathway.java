package com.smartsparrow.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.service.LearnerPathwayService;

import reactor.core.publisher.Flux;

/**
 * A free pathway for a learner. This type of pathway is useful for learning scenarios where the student
 * is free to navigate between child courseware elements.
 *
 * Supplies:
 *      - This will provide all the children walkables.
 *
 */
public class FreeLearnerPathway extends FreePathway implements LearnerPathway {


    private UUID deploymentId;
    private UUID changeId;
    private String config;

    private  final LearnerPathwayService learnerPathwayService;

    @Inject
    FreeLearnerPathway(LearnerPathwayService learnerPathwayService) {
        this.learnerPathwayService = learnerPathwayService;
    }

    // overriden for set method chaining.
    @Override
    FreeLearnerPathway setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    FreeLearnerPathway setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    FreeLearnerPathway setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Nullable
    @Override
    public String getConfig() {
        return config;
    }

    public FreeLearnerPathway setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public Flux<WalkableChild> supplyRelevantWalkables(final UUID studentId) {

        // FIXME: add affirm that studentId is not null.
        // FIXME: add authorization checks

        //
        // return all the available children courseware elements.
        //
        return learnerPathwayService.findWalkables(getId(), deploymentId);
    }

    @Override
    public FreeLearnerPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        super.setPreloadPathway(preloadPathway);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FreeLearnerPathway that = (FreeLearnerPathway) o;
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
        return "FreeLearnerPathway{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                ", learnerPathwayService=" + learnerPathwayService +
                '}';
    }
}
