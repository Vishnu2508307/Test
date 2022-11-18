package com.smartsparrow.learner.data;

import java.util.Objects;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.progress.ProgressActionContext;

/**
 * This object will track the evaluation state while updating the progress upwards in the courseware tree.
 *
 * For instance an interactive evaluation could trigger an activity evaluation when this has scenarios with
 * lifecycle {@link com.smartsparrow.courseware.data.ScenarioLifecycle#ACTIVITY_COMPLETE}. In this instance
 * there will be 2 evaluations happening, This state allows to track the current evaluated walkable and the
 * most recent progression action. This is important in case the ACTIVITY tries to completes the pathway above.
 */
public class EvaluationActionState {

    //this will store the courseware element corresponds to the progress action type
    private CoursewareElement coursewareElement;
    //this will be the progress action context of the ancestor element
    private ProgressActionContext progressActionContext;

    public CoursewareElement getCoursewareElement() { return coursewareElement; }

    public EvaluationActionState setCoursewareElement(final CoursewareElement coursewareElement) {
        this.coursewareElement = coursewareElement;
        return this;
    }

    public ProgressActionContext getProgressActionContext() { return progressActionContext; }

    public EvaluationActionState setProgressActionContext(final ProgressActionContext progressActionContext) {
        this.progressActionContext = progressActionContext;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationActionState that = (EvaluationActionState) o;
        return Objects.equals(coursewareElement, that.coursewareElement) && Objects.equals(
                progressActionContext,
                that.progressActionContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coursewareElement, progressActionContext);
    }

    @Override
    public String toString() {
        return "EvaluationActionState{" +
                "coursewareElement=" + coursewareElement +
                ", progressActionContext=" + progressActionContext +
                '}';
    }
}
