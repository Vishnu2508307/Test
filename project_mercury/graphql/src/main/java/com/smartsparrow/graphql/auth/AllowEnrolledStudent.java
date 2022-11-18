package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;

@Singleton
public class AllowEnrolledStudent {

    private final CohortEnrollmentService cohortEnrollmentService;

    private final CohortService cohortService;

    @Inject
    public AllowEnrolledStudent(final CohortEnrollmentService cohortEnrollmentService,
                                final CohortService cohortService) {
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.cohortService = cohortService;
    }

    public boolean test(AuthenticationContext authenticationContext, UUID cohortId) {
        Account account = authenticationContext.getAccount();
        affirmArgument(cohortId != null, "missing cohortId");
        affirmArgument(account != null, "account can not be null");
        affirmArgument(CollectionUtils.isNotEmpty(account.getRoles()), "roles can not be empty or null");

        boolean allowEnrolledStudentCheck = false;

        // check with the given cohortId
        allowEnrolledStudentCheck = cohortEnrollmentService.getAccountEnrollment(account.getId(), cohortId).block() != null;

        // only if given cohortId is not authorized should we check (LTI) instance cohortIds, if there are any
        // todo once every FE GQL query passes the cohortId, and every BE schema uses it, the code block below can be removed
        if (!allowEnrolledStudentCheck) {
            List<CohortEnrollment> cohortEnrollments = cohortService.findCohortInstanceIds(cohortId) // for LTI students
                    .flatMap(cohortIds -> cohortEnrollmentService.getAccountEnrollment(account.getId(), cohortIds))
                    .filter(cohortEnrollment -> cohortEnrollment != null)
                    .collectList().block();

            if (cohortEnrollments != null && cohortEnrollments.size() > 0) {
                allowEnrolledStudentCheck = true;
            }
        }

        return allowEnrolledStudentCheck;

//        TODO: keep it. will change back to this when cohort id is available in the graphql argument
//        return cohortEnrollmentService.getAccountEnrollment(account.getId(), cohortId).block() != null;


    }
}
