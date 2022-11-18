package com.smartsparrow.rtm.subscription.courseware.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create activity scenario
 */
public class ScenarioCreatedRTMProducer extends AbstractProducer<ScenarioCreatedRTMConsumable> {

    private ScenarioCreatedRTMConsumable scenarioCreatedRTMConsumable;

    @Inject
    public ScenarioCreatedRTMProducer() {
    }

    public ScenarioCreatedRTMProducer buildScenarioCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                        UUID activityId,
                                                                        UUID scenarioId,
                                                                        UUID parentElementId,
                                                                        CoursewareElementType parentElementType,
                                                                        ScenarioLifecycle lifecycle) {
        this.scenarioCreatedRTMConsumable = new ScenarioCreatedRTMConsumable(rtmClientContext,
                                                                             new ScenarioCreatedBroadcastMessage(
                                                                                     activityId,
                                                                                     scenarioId,
                                                                                     parentElementId,
                                                                                     parentElementType,
                                                                                     lifecycle));
        return this;
    }

    @Override
    public ScenarioCreatedRTMConsumable getEventConsumable() {
        return scenarioCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioCreatedRTMProducer that = (ScenarioCreatedRTMProducer) o;
        return Objects.equals(scenarioCreatedRTMConsumable, that.scenarioCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ScenarioCreatedRTMProducer{" +
                "scenarioCreatedRTMConsumable=" + scenarioCreatedRTMConsumable +
                '}';
    }
}
