package com.smartsparrow.rtm.subscription.courseware.scenarioreordered;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioReOrderedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a scenario reordered activity
 */
public class ScenarioReOrderedRTMProducer extends AbstractProducer<ScenarioReOrderedRTMConsumable> {

    private ScenarioReOrderedRTMConsumable scenarioReOrderedRTMConsumable;

    @Inject
    public ScenarioReOrderedRTMProducer() {
    }

    public ScenarioReOrderedRTMProducer buildScenarioReOrderedRTMConsumable(RTMClientContext rtmClientContext,
                                                                            UUID activityId,
                                                                            UUID parentId,
                                                                            CoursewareElementType parentType,
                                                                            List<UUID> scenarioIds,
                                                                            ScenarioLifecycle lifecycle) {
        this.scenarioReOrderedRTMConsumable = new ScenarioReOrderedRTMConsumable(rtmClientContext,
                                                                                 new ScenarioReOrderedBroadcastMessage(
                                                                                         activityId,
                                                                                         parentId,
                                                                                         parentType,
                                                                                         scenarioIds,
                                                                                         lifecycle));
        return this;
    }

    @Override
    public ScenarioReOrderedRTMConsumable getEventConsumable() {
        return scenarioReOrderedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioReOrderedRTMProducer that = (ScenarioReOrderedRTMProducer) o;
        return Objects.equals(scenarioReOrderedRTMConsumable, that.scenarioReOrderedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioReOrderedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ScenarioReOrderedRTMProducer{" +
                "scenarioReOrderedRTMConsumable=" + scenarioReOrderedRTMConsumable +
                '}';
    }
}
