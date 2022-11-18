package com.smartsparrow.courseware.pathway;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.Random;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LearnerRandomPathway extends RandomPathway implements LearnerPathway {

    private final ProgressService progressService;
    private final LearnerPathwayService learnerPathwayService;

    private UUID deploymentId;
    private UUID changeId;
    private String config;

    @Inject
    public LearnerRandomPathway(final ProgressService progressService,
                                final LearnerPathwayService learnerPathwayService) {
        this.progressService = progressService;
        this.learnerPathwayService = learnerPathwayService;
    }

    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public UUID getChangeId() {
        return changeId;
    }

    /**
     * Supply the relevant walkables from this pathway for the student based on the following rules:
     * <br/> - when no attempt exists on the pathway, then returns a random walkable from the children list
     * <br/> - when an attempt exists on the pathway, and no walkable is in progress, then returns a random walkable from the children list
     * <br/> - when an attempt exists on the pathway, and a walkable is in progress, then returns the walkable in progress
     * <br/> - when an attempt exists on the pathway, and exitAfter condition is met, then returns no walkables
     *
     * @param studentId the student id
     * @return a flux with the relevant walkable child
     */
    @Override
    public Flux<WalkableChild> supplyRelevantWalkables(final UUID studentId) {

        // find the latest progress on the pathway
        return progressService.findLatestRandomPathway(deploymentId, getId(), studentId)
                // return an empty progress when not found
                .switchIfEmpty(Mono.just(new RandomPathwayProgress()))
                .flatMapMany(progress-> {

                    // if the progress is empty, this is the first time, return a random walkable
                    if (progress.getId() == null) {
                        return randomWalkable(new ArrayList<>());
                    }

                    // if there is a walkable in progress then return it
                    if (progress.getInProgressElementId() != null) {
                        return Flux.just(new WalkableChild()
                                .setElementId(progress.getInProgressElementId())
                                .setElementType(progress.getInProgressElementType()));
                    }

                    Integer exitAfter = getExitAfter();

                    // when the pathway is completed then return no walkables
                    if (progress.getCompletedWalkables().size() >= exitAfter) {
                        return Flux.empty();
                    }

                    // otherwise return a random walkable that hasn't been completed already
                    return randomWalkable(progress.getCompletedWalkables());
                });
    }

    /**
     * Get a random walkable from the list of candidates. Candidates walkables are the difference between completed
     * walkables and the pathway walkable children.
     *
     * @param completedWalkables the list of walkables that have already been completed by the student
     * @return a flux with a random walkable child
     */
    private Flux<WalkableChild> randomWalkable(final List<UUID> completedWalkables) {
        // find all the walkable children for this pathway
        return learnerPathwayService.findWalkables(getId(), deploymentId)
                // filter out the elements already completed
                .filter(walkable -> !completedWalkables.contains(walkable.getElementId()))
                .collectList()
                .flatMap(remainingWalkables -> {
                    // return a random entry from the candidate walkables
                    return Mono.just(remainingWalkables.get(Random.nextInt(remainingWalkables.size())));
                })
                .flux();
    }

    @Nullable
    @Override
    public String getConfig() {
        return config;
    }

    public LearnerRandomPathway setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public LearnerRandomPathway setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public LearnerRandomPathway setConfig(final String config) {
        this.config = config;
        return this;
    }

    @Override
    public LearnerRandomPathway setId(final UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerRandomPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        super.setPreloadPathway(preloadPathway);
        return this;
    }

    /**
     * The exitAfter configured value indicates the number of completed walkables required by this pathway to satisfy
     * the exit (complete) condition. This pathway will be marked as completed when the total number of completed
     * walkables is equal to the number value of the exitAfter configuration property.
     *
     * @return get the exit after configured value
     */
    public Integer getExitAfter() {
        return Json.query(config, Lists.newArrayList(EXIT_AFTER), Integer.class);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerRandomPathway that = (LearnerRandomPathway) o;
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
        return "LearnerRandomPathway{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                '}';
    }
}
