package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FeedbackByLearnerInteractive {

    private UUID interactiveId;
    private UUID deploymentId;
    private UUID changeId;
    private List<UUID> feedbackIds;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public FeedbackByLearnerInteractive setInteractiveId(UUID interactiveId) {
        this.interactiveId = interactiveId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public FeedbackByLearnerInteractive setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public FeedbackByLearnerInteractive setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public List<UUID> getFeedbackIds() {
        return feedbackIds;
    }

    public FeedbackByLearnerInteractive setFeedbackIds(List<UUID> feedbackIds) {
        this.feedbackIds = feedbackIds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackByLearnerInteractive that = (FeedbackByLearnerInteractive) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(feedbackIds, that.feedbackIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveId, deploymentId, changeId, feedbackIds);
    }

    @Override
    public String toString() {
        return "FeedbackByLearnerInteractive{" +
                "interactiveId=" + interactiveId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", feedbackIds=" + feedbackIds +
                '}';
    }
}
