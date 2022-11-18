package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.learner.data.DeployedActivity;

public class PublishedActivityBroadcastMessage extends CohortBroadcastMessage {

    private static final long serialVersionUID = -7620560853615438473L;

    private DeployedActivity deployedActivity;

    public PublishedActivityBroadcastMessage(UUID cohortId) {
        super(cohortId);
    }

    public DeployedActivity getPublishedActivity() {
        return deployedActivity;
    }

    public PublishedActivityBroadcastMessage setPublishedActivity(DeployedActivity deployedActivity) {
        this.deployedActivity = deployedActivity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishedActivityBroadcastMessage that = (PublishedActivityBroadcastMessage) o;
        return Objects.equals(deployedActivity, that.deployedActivity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deployedActivity);
    }

    @Override
    public String toString() {
        return "PublishedActivityBroadcastMessage{" +
                "publishedActivity=" + deployedActivity +
                '}';
    }

    @Override
    public UUID getCohortId() {
        return deployedActivity.getCohortId();
    }
}
