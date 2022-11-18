package com.smartsparrow.rtm.subscription.courseware.elementthemedelete;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for an element theme delete activity
 */
public class ElementThemeDeleteRTMProducer extends AbstractProducer<ElementThemeDeleteRTMConsumable> {

    private ElementThemeDeleteRTMConsumable elementThemeDeleteRTMConsumable;

    @Inject
    public ElementThemeDeleteRTMProducer() {
    }

    public ElementThemeDeleteRTMProducer buildElementThemeDeleteRTMConsumable(RTMClientContext rtmClientContext,
                                                                              UUID activityId,
                                                                              UUID elementId,
                                                                              CoursewareElementType elementType) {
        this.elementThemeDeleteRTMConsumable = new ElementThemeDeleteRTMConsumable(rtmClientContext,
                                                                                   new ActivityBroadcastMessage(
                                                                                           activityId,
                                                                                           elementId,
                                                                                           elementType));
        return this;
    }

    @Override
    public ElementThemeDeleteRTMConsumable getEventConsumable() {
        return elementThemeDeleteRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementThemeDeleteRTMProducer that = (ElementThemeDeleteRTMProducer) o;
        return Objects.equals(elementThemeDeleteRTMConsumable, that.elementThemeDeleteRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementThemeDeleteRTMConsumable);
    }

    @Override
    public String toString() {
        return "ElementThemeDeleteRTMProducer{" +
                "elementThemeDeleteRTMConsumable=" + elementThemeDeleteRTMConsumable +
                '}';
    }
}
