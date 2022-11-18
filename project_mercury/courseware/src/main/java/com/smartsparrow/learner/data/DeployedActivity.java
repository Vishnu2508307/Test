package com.smartsparrow.learner.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class DeployedActivity extends Deployment implements Serializable {

    private static final long serialVersionUID = 4119338218870152391L;
    private UUID activityId;

    public UUID getActivityId() {
        return activityId;
    }

    public DeployedActivity setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    @Override
    public DeployedActivity setCohortId(UUID cohortId) {
        super.setCohortId(cohortId);
        return this;
    }

    @Override
    public DeployedActivity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public DeployedActivity setChangeId(UUID changeId) {
        super.setChangeId(changeId);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeployedActivity that = (DeployedActivity) o;
        return Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), activityId);
    }

    @Override
    public String toString() {
        return "PublishedActivity{" +
                "activityId=" + activityId +
                "} " + super.toString();
    }
}
