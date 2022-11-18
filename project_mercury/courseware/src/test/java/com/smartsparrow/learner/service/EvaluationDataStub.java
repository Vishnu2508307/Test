package com.smartsparrow.learner.service;

import java.util.ArrayList;
import java.util.UUID;

import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.util.UUIDs;

public class EvaluationDataStub {

    public static EvaluationResult buildEvaluationResult(boolean interactiveComplete) {
        final Attempt attempt = new Attempt()
                .setId(UUID.randomUUID())
                .setParentId(UUID.randomUUID());

        final Deployment deployment =  new Deployment()
                .setId(UUID.randomUUID())
                .setChangeId(UUID.randomUUID())
                .setCohortId(UUID.randomUUID());

        return new EvaluationResult()
                .setId(UUIDs.timeBased())
                .setCoursewareElementId(UUID.randomUUID())
                .setInteractiveComplete(interactiveComplete)
                .setDeployment(deployment)
                .setAttempt(attempt)
                .setAttemptId(attempt.getId())
                .setScenarioCorrectness(ScenarioCorrectness.correct)
                .setScenarioEvaluationResults(new ArrayList<>())
                .setActionResults(new ArrayList<>())
                .setParentId(UUID.randomUUID());
    }

    public static LearnerEvaluationRequest buildLearnerEvaluationRequest(UUID elementId) {
        UUID changeId = UUID.randomUUID();
        UUID deploymentId = UUID.randomUUID();

        final LearnerWalkable walkable = new LearnerInteractive()
                .setId(elementId)
                .setChangeId(changeId)
                .setDeploymentId(deploymentId);

        final Deployment deployment =  new Deployment()
                .setId(UUID.randomUUID())
                .setChangeId(changeId)
                .setCohortId(deploymentId);

        return new LearnerEvaluationRequest()
                .setLearnerWalkable(walkable)
                .setStudentId(UUID.randomUUID())
                .setDeployment(deployment)
                .setParentPathwayId(UUID.randomUUID());
    }
}
