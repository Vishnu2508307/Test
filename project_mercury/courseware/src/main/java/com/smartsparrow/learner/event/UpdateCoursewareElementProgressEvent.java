package com.smartsparrow.learner.event;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.learner.progress.Progress;

/**
 * Event to trigger a progress update across courseware elements. Specifies the element to target.
 */
public class UpdateCoursewareElementProgressEvent {

    private UpdateProgressMessage updateProgressEvent;

    private CoursewareElement element;

    private List<Progress> eventProgress = new ArrayList<>();

    public UpdateCoursewareElementProgressEvent() {
    }

    public UpdateProgressMessage getUpdateProgressEvent() {
        return updateProgressEvent;
    }

    public UpdateCoursewareElementProgressEvent setUpdateProgressEvent(UpdateProgressMessage updateProgressEvent) {
        this.updateProgressEvent = updateProgressEvent;
        return this;
    }

    public CoursewareElement getElement() {
        return element;
    }

    public UpdateCoursewareElementProgressEvent setElement(CoursewareElement element) {
        this.element = element;
        return this;
    }

    /**
     * Get the event progress in the same order as the EvaluationEventMessage#ancestryFromInteractive
     *
     * @return the progress triggered in this event from the interactive to the root
     */
    public List<Progress> getEventProgress() {
        return eventProgress;
    }

    public UpdateCoursewareElementProgressEvent setEventProgress(List<Progress> eventProgress) {
        this.eventProgress = eventProgress;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UpdateCoursewareElementProgressEvent that = (UpdateCoursewareElementProgressEvent) o;
        return Objects.equal(getUpdateProgressEvent(), that.getUpdateProgressEvent()) && Objects
                .equal(getElement(), that.getElement()) && Objects.equal(getEventProgress(), that.getEventProgress());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUpdateProgressEvent(), getElement(), getEventProgress());
    }

    @Override
    public String toString() {
        return "UpdateCoursewareElementProgressEvent{" + "updateProgressEvent=" + updateProgressEvent + ", element=" + element
                + ", eventProgress=" + eventProgress + '}';
    }
}
