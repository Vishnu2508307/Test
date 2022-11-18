package com.smartsparrow.rtm.subscription.courseware.elementthemecreate;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ThemeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for an element theme create activity
 */
public class ElementThemeCreateRTMProducer extends AbstractProducer<ElementThemeCreateRTMConsumable> {

    private ElementThemeCreateRTMConsumable elementThemeCreateRTMConsumable;

    @Inject
    public ElementThemeCreateRTMProducer() {
    }

    public ElementThemeCreateRTMProducer buildElementThemeCreateRTMConsumable(RTMClientContext rtmClientContext,
                                                                              UUID activityId,
                                                                              UUID elementId,
                                                                              CoursewareElementType elementType,
                                                                              UUID themeId) {
        this.elementThemeCreateRTMConsumable = new ElementThemeCreateRTMConsumable(rtmClientContext,
                                                                                   new ThemeBroadcastMessage(
                                                                                           activityId,
                                                                                           elementId,
                                                                                           elementType,
                                                                                           themeId));
        return this;
    }

    @Override
    public ElementThemeCreateRTMConsumable getEventConsumable() {
        return elementThemeCreateRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementThemeCreateRTMProducer that = (ElementThemeCreateRTMProducer) o;
        return Objects.equals(elementThemeCreateRTMConsumable, that.elementThemeCreateRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementThemeCreateRTMConsumable);
    }

    @Override
    public String toString() {
        return "ElementThemeCreateRTMProducer{" +
                "elementThemeCreateRTMConsumable=" + elementThemeCreateRTMConsumable +
                '}';
    }
}
