package com.smartsparrow.eval.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.progress.Progress;

/**
 * This class acts as a context that wraps the learner evaluation response.
 * This class will be passed around to {@link com.smartsparrow.eval.service.LearnerEvaluationResponseEnricher} and
 * the action consumers so additional informations about evaluation can be added to the result
 */
public class LearnerEvaluationResponseContext {

    private LearnerEvaluationResponse response;
    private EvaluationActionState evaluationActionState;
    /**
     * This field is only set when the default progression action is required
     * see {@link com.smartsparrow.eval.service.LearnerScenarioActionEnricher}
     */
    private LearnerPathway parentPathway;
    private List<CoursewareElement> ancestry;
    private final List<Progress> progresses;
    private Map<UUID, String> scopeEntriesMap;
    private UUID timeId;
    private LTIData ltiData;

    public LearnerEvaluationResponseContext() {
        this.progresses = new ArrayList<>();
    }

    public LearnerEvaluationResponse getResponse() {
        return response;
    }

    public LearnerEvaluationResponseContext setResponse(LearnerEvaluationResponse response) {
        this.response = response;
        return this;
    }

    public EvaluationActionState getEvaluationActionState() {
        return evaluationActionState;
    }

    public LearnerEvaluationResponseContext setEvaluationActionState(EvaluationActionState evaluationActionState) {
        this.evaluationActionState = evaluationActionState;
        return this;
    }

    /**
     * This field is only set when the default progression action is required
     * see {@link com.smartsparrow.eval.service.LearnerScenarioActionEnricher}
     *
     * @return the parent pathway of the evaluated walkable or null
     */
    @Nullable
    public LearnerPathway getParentPathway() {
        return parentPathway;
    }

    public LearnerEvaluationResponseContext setParentPathway(LearnerPathway parentPathway) {
        this.parentPathway = parentPathway;
        return this;
    }

    public List<CoursewareElement> getAncestry() {
        return ancestry;
    }

    public LearnerEvaluationResponseContext setAncestry(List<CoursewareElement> ancestry) {
        this.ancestry = ancestry;
        return this;
    }

    public List<Progress> getProgresses() {
        return progresses;
    }

    public LearnerEvaluationResponseContext addProgress(final Progress progress) {
        this.progresses.add(progress);
        return this;
    }

    public Map<UUID, String> getScopeEntriesMap() {
        return scopeEntriesMap;
    }

    public LearnerEvaluationResponseContext setScopeEntriesMap(Map<UUID, String> scopeEntriesMap) {
        this.scopeEntriesMap = scopeEntriesMap;
        return this;
    }

    /**
     * This field is only set when the action is CHANGE_SCOPE
     *
     * @return the learnspace generated id
     */
    @Nullable
    public UUID getTimeId() {
        return timeId;
    }

    public LearnerEvaluationResponseContext setTimeId(final UUID timeId) {
        this.timeId = timeId;
        return this;
    }

    public LTIData getLtiData() {
        return ltiData;
    }

    public LearnerEvaluationResponseContext setLtiData(final LTIData ltiData) {
        this.ltiData = ltiData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerEvaluationResponseContext that = (LearnerEvaluationResponseContext) o;
        return Objects.equals(response, that.response) &&
                Objects.equals(evaluationActionState, that.evaluationActionState) &&
                Objects.equals(parentPathway, that.parentPathway) &&
                Objects.equals(progresses, that.progresses) &&
                Objects.equals(ancestry, that.ancestry) &&
                Objects.equals(scopeEntriesMap, that.scopeEntriesMap)&&
                Objects.equals(timeId, that.timeId) &&
                Objects.equals(ltiData, that.ltiData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response, evaluationActionState, parentPathway, ancestry, progresses, scopeEntriesMap, timeId, ltiData);
    }

    @Override
    public String toString() {
        return "LearnerEvaluationResponseContext{" +
                "response=" + response +
                ", evaluationActionState=" + evaluationActionState +
                ", parentPathway=" + parentPathway +
                ", ancestry=" + ancestry +
                ", progresses=" + progresses +
                ", scopeEntriesMap=" + scopeEntriesMap +
                ", timeId=" + timeId +
                ", ltiData=" + ltiData +
                '}';
    }
}
