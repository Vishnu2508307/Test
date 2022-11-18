package com.smartsparrow.courseware.pathway;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.eval.action.progress.ProgressionType.ACTIVITY_REPEAT;
import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_REPEAT;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.Json;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LearnerGraphPathway extends GraphPathway implements LearnerPathway {

    private static final Logger log = LoggerFactory.getLogger(LearnerGraphPathway.class);

    private UUID deploymentId;
    private UUID changeId;
    private String config;

    private final ProgressService progressService;
    private  final LearnerPathwayService learnerPathwayService;

    @Inject
    public LearnerGraphPathway(final ProgressService progressService,
                               final LearnerPathwayService learnerPathwayService) {
        super();
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
    public ProgressAction getDefaultAction() {
        return new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setResolver(new LiteralContext()
                        .setType(Resolver.Type.LITERAL))
                .setContext(new ProgressActionContext()
                        .setProgressionType(INTERACTIVE_REPEAT));
    }

    @Override
    public ProgressAction getDefaultAction(CoursewareElementType walkableType) {
        affirmArgument(CoursewareElementType.isAWalkable(walkableType), "walkable elementType required");
        // if it's not interactive it's an activity
        ProgressionType progressionType = walkableType.equals(INTERACTIVE) ? INTERACTIVE_REPEAT : ACTIVITY_REPEAT;
        return new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setResolver(new LiteralContext()
                        .setType(Resolver.Type.LITERAL))
                .setContext(new ProgressActionContext()
                        .setProgressionType(progressionType));
    }

    /**
     * Supply the relevant walkable for a student on a learner graph pathway.
     *
     * @param studentId the student id to find the relevant walkable for
     * @return either the starting walkable defined on the pathway configuration or the currentWalkable on the last
     * pathway progress.
     * @throws IllegalStateFault when the configuration fields are not found or have invalid value.
     */
    @Override
    public Flux<WalkableChild> supplyRelevantWalkables(UUID studentId) {
        Flux<WalkableChild> startingWalkable = getConfiguredWalkable();

        // find the latest graph pathway progress
        return progressService.findLatestGraphPathway(deploymentId, getId(), studentId)
                // build the walkable child based on the current walkable in the progress
                .flatMapMany(progress -> Flux.just(new WalkableChild()
                        .setElementId(progress.getCurrentWalkableId())
                        .setElementType(progress.getCurrentWalkableType())))
                // return the configured starting walkable when graph pathway progress not found
                .switchIfEmpty(startingWalkable);
    }

    /**
     * Find the configured walkable for this pathway
     *
     * @return a flux of walkable child
     * @throws IllegalStateFault when the configuration fields are not found or have invalid value.
     */
    public Flux<WalkableChild> getConfiguredWalkable() {
        // find the configured starting walkable in a reactive fashion

        return Mono.just(1)
                .map(ignored -> {
                    final String elementIdField = Json.query(config, Lists.newArrayList(STARTING_WALKABLE_ID), String.class);
                    final String elementTypeField = Json.query(config, Lists.newArrayList(STARTING_WALKABLE_TYPE), String.class);

                    return new WalkableChild()
                            .setElementId(UUID.fromString(elementIdField))
                            .setElementType(Enums.of(CoursewareElementType.class, elementTypeField));
                })
                .onErrorResume(throwable -> {
                    if (log.isDebugEnabled()) {
                        log.debug("error supplying the relevant walkables", throwable);
                    }
                    return learnerPathwayService.findWalkables(getId(), getDeploymentId())
                            .next();
                })
                .flux();
    }

    @Override
    public LearnerGraphPathway setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Nullable
    @Override
    public String getConfig() {
        return config;
    }

    public LearnerGraphPathway setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public LearnerGraphPathway setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public LearnerGraphPathway setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public LearnerGraphPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        super.setPreloadPathway(preloadPathway);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerGraphPathway that = (LearnerGraphPathway) o;
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
        return "LearnerGraphPathway{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                '}';
    }
}
