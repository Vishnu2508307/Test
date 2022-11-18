package com.smartsparrow.eval.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.ScenarioCorrectness;

/**
 * Holds the scenario evaluation info
 */
public class ScenarioEvaluationResult {

    private UUID scenarioId;
    private ScenarioCorrectness scenarioCorrectness;
    private boolean evaluationResult;
    private String actions;
    private String errorMessage;
    private CoursewareElement coursewareElement;

    public CoursewareElement getCoursewareElement() { return coursewareElement; }

    public ScenarioEvaluationResult setCoursewareElement(final CoursewareElement coursewareElement) {
        this.coursewareElement = coursewareElement;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ScenarioEvaluationResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * @return the id of the scenario
     */
    public UUID getScenarioId() {
        return scenarioId;
    }

    public ScenarioEvaluationResult setScenarioId(UUID scenarioId) {
        this.scenarioId = scenarioId;
        return this;
    }

    /**
     * @return an enum representing the correctness of the scenario
     */
    public ScenarioCorrectness getScenarioCorrectness() {
        return scenarioCorrectness;
    }

    public ScenarioEvaluationResult setScenarioCorrectness(ScenarioCorrectness scenarioCorrectness) {
        this.scenarioCorrectness = scenarioCorrectness;
        return this;
    }

    /**
     * @return the boolean result from the scenario evaluation
     */
    public boolean getEvaluationResult() {
        return evaluationResult;
    }

    public ScenarioEvaluationResult setEvaluationResult(boolean evaluationResult) {
        this.evaluationResult = evaluationResult;
        return this;
    }

    /**
     * This method is the source of truth when it comes to get the correctness of the scenario. Correctness here means
     * a scenario with correctness label {@link ScenarioCorrectness#correct} evaluated to true. This is the only case
     * where a scenario will considered to be correct. If for instance a scenario with label
     * {@link ScenarioCorrectness#incorrect} evaluated to true or false the scenario is considered to be incorrect
     *
     * @return <code>true</code> if the scenario is correct, <code>false</code> if the scenario is incorrect or
     * if the scenario correctness cannot be determined
     */
    public boolean isCorrect() {
        return ScenarioCorrectness.correct.equals(this.scenarioCorrectness) && Boolean.TRUE.equals(this.evaluationResult);
    }

    /**
     * Returns the actions json string belonging to the scenario
     *
     * @return
     */
    public String getActions() {
        return actions;
    }

    /**
     * Scenario actions still in json format
     *
     * @param actions
     * @return
     */
    public ScenarioEvaluationResult setActions(String actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioEvaluationResult that = (ScenarioEvaluationResult) o;
        return evaluationResult == that.evaluationResult &&
                Objects.equals(scenarioId, that.scenarioId) &&
                scenarioCorrectness == that.scenarioCorrectness &&
                Objects.equals(actions, that.actions) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(coursewareElement, that.coursewareElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, scenarioCorrectness, evaluationResult, actions, errorMessage, coursewareElement);
    }

    @Override
    public String toString() {
        return "ScenarioEvaluationResult{" +
                "scenarioId=" + scenarioId +
                ", scenarioCorrectness=" + scenarioCorrectness +
                ", evaluationResult=" + evaluationResult +
                ", actions='" + actions + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", coursewareElement=" + coursewareElement +
                '}';
    }
}
