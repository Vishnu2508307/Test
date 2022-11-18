package com.smartsparrow.rtm.subscription.courseware.updated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioUpdatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for an updated activity scenario
 */
public class ScenarioUpdatedRTMProducer extends AbstractProducer<ScenarioUpdatedRTMConsumable> {

    private ScenarioUpdatedRTMConsumable scenarioUpdatedRTMConsumable;

    @Inject
    public ScenarioUpdatedRTMProducer() {
    }

    public ScenarioUpdatedRTMProducer buildScenarioUpdatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                        UUID activityId,
                                                                        UUID scenarioId,
                                                                        UUID parentElementId,
                                                                        CoursewareElementType parentElementType,
                                                                        ScenarioLifecycle lifecycle) {
        this.scenarioUpdatedRTMConsumable = new ScenarioUpdatedRTMConsumable(rtmClientContext,
                                                                             new ScenarioUpdatedBroadcastMessage(
                                                                                     activityId,
                                                                                     scenarioId,
                                                                                     parentElementId,
                                                                                     parentElementType,
                                                                                     lifecycle));
        return this;
    }

    @Override
    public ScenarioUpdatedRTMConsumable getEventConsumable() {
        return scenarioUpdatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioUpdatedRTMProducer that = (ScenarioUpdatedRTMProducer) o;
        return Objects.equals(scenarioUpdatedRTMConsumable, that.scenarioUpdatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioUpdatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ScenarioUpdatedRTMProducer{" +
                "scenarioUpdatedRTMConsumable=" + scenarioUpdatedRTMConsumable +
                '}';
    }
}
