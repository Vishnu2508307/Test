package com.smartsparrow.learner.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResult;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.learner.attempt.Attempt;

/**
 * Track the result of an evaluation operation. To find a more complete object look for {@link Evaluation}
 * TODO should this be replaced entirely by the Evaluation object?
 */
public class EvaluationResult {

    private UUID id;

    private UUID coursewareElementId;
    // private boolean scopeId;
    // private boolean scopeUrn;

    @JsonInclude
    private boolean interactiveComplete;

    private Deployment deployment;

    private Attempt attempt;
    private UUID attemptId;
    // private UUID cohortId;
    private ScenarioCorrectness scenarioCorrectness;

    private List<ScenarioEvaluationResult> scenarioEvaluationResults;

    // TODO:
    // TODO: (track other relevant data points)
    // TODO: track the scenarios (and scenario groups) that fired and those which did not.
    // TODO:

    // this field is deprecated and will be removed from the response when the front end is ready to move on
    // and read the actions from the triggeredActions list
    @Deprecated
    private List<ActionResult> actionResults;

    private List<Action> triggeredActions = new ArrayList<>();

    private UUID parentId;


    public EvaluationResult() {
    }

    /**
     *
     * @return the ID of the evaluation operation, similar to a transaction id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set the ID of the evaluation operation
     *
     * @param id a time UUID representing an identifier for the evaluation operation
     * @return this
     */
    public EvaluationResult setId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     *
     * @return the target of the evaluation (e.g. the interactive id)
     */
    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    /**
     * Set the target of the evaluation (e.g. the interactive id)
     *
     * @param coursewareElementId the id of the target evaluation
     * @return this
     */
    public EvaluationResult setCoursewareElementId(UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public boolean getInteractiveComplete() {
        return interactiveComplete;
    }
    public EvaluationResult setInteractiveComplete(boolean interactiveComplete) {
        this.interactiveComplete = interactiveComplete;
        return this;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public EvaluationResult setAttempt(Attempt attempt) {
        this.attempt = attempt;
        return this;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public EvaluationResult setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    public ScenarioCorrectness getScenarioCorrectness() {
        return scenarioCorrectness;
    }

    public EvaluationResult setScenarioCorrectness(ScenarioCorrectness scenarioCorrectness) {
        this.scenarioCorrectness = scenarioCorrectness;
        return this;
    }

    @Deprecated
    public List<ActionResult> getActionResults() {
        return actionResults;
    }

    @Deprecated
    public EvaluationResult setActionResults(List<ActionResult> actionResults) {
        this.actionResults = actionResults;
        return this;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public EvaluationResult setDeployment(Deployment deployment) {
        this.deployment = deployment;
        return this;
    }

    /**
     *
     * @return a list of all the scenarios that fired with their scenario evaluation results
     */
    public List<ScenarioEvaluationResult> getScenarioEvaluationResults() {
        return scenarioEvaluationResults;
    }

    public EvaluationResult setScenarioEvaluationResults(List<ScenarioEvaluationResult> scenarioEvaluationResults) {
        this.scenarioEvaluationResults = scenarioEvaluationResults;
        return this;
    }

    @JsonIgnore
    public UUID getParentId() {
        return parentId;
    }

    public EvaluationResult setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public List<Action> getTriggeredActions() {
        return triggeredActions;
    }

    public EvaluationResult setTriggeredActions(List<Action> triggeredActions) {
        this.triggeredActions = triggeredActions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationResult that = (EvaluationResult) o;
        return interactiveComplete == that.interactiveComplete &&
                Objects.equals(id, that.id) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                Objects.equals(deployment, that.deployment) &&
                Objects.equals(attempt, that.attempt) &&
                Objects.equals(attemptId, that.attemptId) &&
                scenarioCorrectness == that.scenarioCorrectness &&
                Objects.equals(scenarioEvaluationResults, that.scenarioEvaluationResults) &&
                Objects.equals(actionResults, that.actionResults) &&
                Objects.equals(triggeredActions, that.triggeredActions) &&
                Objects.equals(parentId, that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, coursewareElementId, interactiveComplete, deployment, attempt, attemptId,
                scenarioCorrectness, scenarioEvaluationResults, actionResults, triggeredActions, parentId);
    }

    @Override
    public String toString() {
        return "EvaluationResult{" +
                "id=" + id +
                ", coursewareElementId=" + coursewareElementId +
                ", interactiveComplete=" + interactiveComplete +
                ", deployment=" + deployment +
                ", attempt=" + attempt +
                ", attemptId=" + attemptId +
                ", scenarioCorrectness=" + scenarioCorrectness +
                ", scenarioEvaluationResults=" + scenarioEvaluationResults +
                ", actionResults=" + actionResults +
                ", triggeredActions=" + triggeredActions +
                ", parentId=" + parentId +
                '}';
    }
}
