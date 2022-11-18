package com.smartsparrow.graphql.type;

import java.util.Objects;

import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.learner.data.LearnerActivity;

/**
 * Type to combine an enrollment and a learner activity.
 */
public class EnrollmentLearnerActivity {

    private CohortEnrollment enrollment;
    private LearnerActivity learnerActivity;

    public EnrollmentLearnerActivity() {
    }

    public CohortEnrollment getEnrollment() {
        return enrollment;
    }

    public EnrollmentLearnerActivity setEnrollment(CohortEnrollment enrollment) {
        this.enrollment = enrollment;
        return this;
    }

    public LearnerActivity getLearnerActivity() {
        return learnerActivity;
    }

    public EnrollmentLearnerActivity setLearnerActivity(LearnerActivity learnerActivity) {
        this.learnerActivity = learnerActivity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EnrollmentLearnerActivity that = (EnrollmentLearnerActivity) o;
        return Objects.equals(enrollment, that.enrollment) && Objects.equals(learnerActivity, that.learnerActivity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollment, learnerActivity);
    }

    @Override
    public String toString() {
        return "EnrollmentPublishedActivity{" + "enrollment=" + enrollment + ", learnerActivity=" + learnerActivity
                + '}';
    }
}
