package com.smartsparrow.courseware.pathway;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.Random;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LearnerBKTPathway extends BKTPathway implements LearnerPathway {

    private final ProgressService progressService;
    private final LearnerPathwayService learnerPathwayService;

    private UUID deploymentId;
    private UUID changeId;
    private String config;

    @Inject
    public LearnerBKTPathway(final ProgressService progressService,
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

    @Override
    public Flux<WalkableChild> supplyRelevantWalkables(final UUID studentId) {
        // find the latest progress on the pathway
        return progressService.findLatestNBKTPathway(deploymentId, getId(), studentId, 1)
                .singleOrEmpty()
                // return an empty progress when not found
                .switchIfEmpty(Mono.just(new BKTPathwayProgress()))
                .flatMapMany(progress -> {

                    // if the progress is empty, this is the first time, return a random walkable
                    if (progress.getId() == null) {
                        return randomWalkable(new ArrayList<>());
                    }

                    // if the found progress is marked as completed then return no walkables
                    if (progress.isCompleted()) {
                        return Flux.empty();
                    }

                    // if there is a walkable in progress then return it
                    if (progress.getInProgressElementId() != null) {
                        return Flux.just(new WalkableChild()
                                .setElementId(progress.getInProgressElementId())
                                .setElementType(progress.getInProgressElementType()));
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

    public LearnerBKTPathway setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public LearnerBKTPathway setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public LearnerBKTPathway setConfig(final String config) {
        this.config = config;
        return this;
    }

    @Override
    public LearnerBKTPathway setId(final UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerBKTPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        super.setPreloadPathway(preloadPathway);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerBKTPathway that = (LearnerBKTPathway) o;
        return Objects.equals(progressService, that.progressService) &&
                Objects.equals(learnerPathwayService, that.learnerPathwayService) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), progressService, learnerPathwayService, deploymentId, changeId, config);
    }

    @Override
    public String toString() {
        return "LearnerBKTPathway{" +
                "progressService=" + progressService +
                ", learnerPathwayService=" + learnerPathwayService +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                '}';
    }

    /**
     * This pathway will be marked as completed when:
     * <br> - either the student maintains the probability of knowing a skill to a specific value
     * {@link LearnerBKTPathway#getTransitProbability()} for a number of configured consecutive screens
     * <br> - or the total number of completed walkables is equal to or higher than the exitAfter configured value.
     *
     * @return get the exit after configured value
     */
    public Integer getExitAfter() {
        return Json.query(config, Lists.newArrayList(EXIT_AFTER), Integer.class);
    }

    /**
     * @return the configured probability of Slip (mistake)
     */
    public Double getSlipProbability() {
        return Json.query(config, Lists.newArrayList(P_S), BigDecimal.class).doubleValue();
    }

    /**
     * @return the configured probability of Guess (random guess)
     */
    public Double getGuessProbability() {
        return Json.query(config, Lists.newArrayList(P_G), BigDecimal.class).doubleValue();
    }

    /**
     * @return the configured probability of transit (learned)
     */
    public Double getTransitProbability() {
        return Json.query(config, Lists.newArrayList(P_T), BigDecimal.class).doubleValue();
    }

    /**
     * @return the configured L0 value
     */
    public Double getL0() {
        return Json.query(config, Lists.newArrayList(P_L0), BigDecimal.class).doubleValue();
    }

    /**
     * @return how many consecutive screens the student should maintain the minimum P(Lₙ) value configured in this
     * pathway
     */
    public Integer getMaintainFor() {
        return Json.query(config, Lists.newArrayList(MAINTAIN_FOR), Integer.class);
    }

    /**
     * @return the minimum P(Lₙ) value the student should achieve
     */
    public Double getPLN() {
        return Json.query(config, Lists.newArrayList(P_LN), BigDecimal.class).doubleValue();
    }

    /**
     * @return the list of configured document items that should be
     */
    public List<ConfiguredDocumentItem> getCompetency() {
        return Lists.newArrayList(Json.query(config, Lists.newArrayList(COMPETENCY), ConfiguredDocumentItem[].class));
    }
}
