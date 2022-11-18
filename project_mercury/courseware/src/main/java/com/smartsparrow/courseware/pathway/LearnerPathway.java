package com.smartsparrow.courseware.pathway;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.eval.action.progress.ProgressionType.ACTIVITY_COMPLETE;
import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_COMPLETE;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.learner.data.LearnerElement;

import io.leangen.graphql.annotations.GraphQLIgnore;
import reactor.core.publisher.Flux;

public interface LearnerPathway extends Pathway {

    UUID getDeploymentId();

    UUID getChangeId();

    PreloadPathway getPreloadPathway();

    default CoursewareElementType getElementType() {
        return CoursewareElementType.PATHWAY;
    }

    /**
     * Provide the caller with the relevant walkables, based on the supplied arguments.
     *
     * @param studentId the student id
     * @return the relevant walkables
     */
    Flux<WalkableChild> supplyRelevantWalkables(UUID studentId);

    /**
     * @return the json string representing the config for the pathway or <code>null</code> if the pathway has no config
     */
    @Nullable
    String getConfig();

    /**
     * Get the default action to perform on a pathway when a walkable does not have scenarios
     *
     * @return this default implementation returns a ProgressAction with {@link ProgressionType#INTERACTIVE_COMPLETE}
     */
    @JsonIgnore
    @GraphQLIgnore
    default ProgressAction getDefaultAction() {
        return new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setResolver(new LiteralContext()
                        .setType(Resolver.Type.LITERAL))
                .setContext(new ProgressActionContext()
                        .setProgressionType(INTERACTIVE_COMPLETE));
    }

    /**
     * Get the default action to perform on a pathway when a walkable does not have scenarios
     *
     * @return this default implementation returns a ProgressAction with {@link ProgressionType#INTERACTIVE_COMPLETE}
     */
    @JsonIgnore
    @GraphQLIgnore
    default ProgressAction getDefaultAction(CoursewareElementType walkableType) {
        affirmArgument(CoursewareElementType.isAWalkable(walkableType), "walkable elementType required");
        // if it's not interactive it's an activity
        ProgressionType progressionType = walkableType.equals(INTERACTIVE) ? INTERACTIVE_COMPLETE : ACTIVITY_COMPLETE;
        return new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setResolver(new LiteralContext()
                        .setType(Resolver.Type.LITERAL))
                .setContext(new ProgressActionContext()
                        .setProgressionType(progressionType));
    }
}
