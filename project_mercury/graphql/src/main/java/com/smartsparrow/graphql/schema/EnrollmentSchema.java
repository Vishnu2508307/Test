package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static java.util.Comparator.comparing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentStatus;
import com.smartsparrow.cohort.data.HistoricalCohortEnrollment;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.util.UUIDs;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class EnrollmentSchema {

    private static final String ERROR_MSG = "Unauthorized";
    //
    private final AllowCohortInstructor allowCohortInstructor;
    //
    private final CohortEnrollmentService cohortEnrollmentService;

    @Inject
    public EnrollmentSchema(AllowCohortInstructor allowCohortInstructor,
            CohortEnrollmentService cohortEnrollmentService) {
        this.allowCohortInstructor = allowCohortInstructor;
        this.cohortEnrollmentService = cohortEnrollmentService;
    }

    @GraphQLQuery(name = "enrollments", description = "find all the enrollments to a cohort")
    public CompletableFuture<Page<HistoricalCohortEnrollment>> getEnrollmentsForCohort(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                       @GraphQLContext CohortSummary cohortSummary,
                                                                                       @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                                                       @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow instructors to request this data.
        affirmPermission(cohortSummary.getId() != null && allowCohortInstructor.test(context.getAuthenticationContext(),
                                                                                     cohortSummary.getId()), ERROR_MSG);

        // get a list of the currently enrolled accounts
        final List<UUID> currentlyEnrolled = cohortEnrollmentService.fetchEnrolledAccountIds(cohortSummary.getId())
                .collectList()
                .defaultIfEmpty(Lists.newArrayList())
                .block();

        // get a list of all the accounts that were ever enrolled
        final Mono<List<HistoricalCohortEnrollment>> enrollments = cohortEnrollmentService.fetchHistoricalEnrollments(cohortSummary.getId())
                // maintain a consistent sort order for paging.
                .sort(comparing(HistoricalCohortEnrollment::getAccountId, UUIDs::compareByTime))
                .map(enrollment -> {
                    if (currentlyEnrolled.contains(enrollment.getAccountId())) {
                        return enrollment.setEnrollmentStatus(EnrollmentStatus.ENROLLED);
                    }
                    return enrollment.setEnrollmentStatus(EnrollmentStatus.NOT_ENROLLED);
                })
                .collectList()
                .defaultIfEmpty(Lists.newArrayList());

        return GraphQLPageFactory.createPage(enrollments, before, last).toFuture();
    }

    @GraphQLQuery(name = "enrollmentByStudent", description = "find a specific enrollment of a student to a cohort")
    public CompletableFuture<HistoricalCohortEnrollment> getEnrollmentsForCohortByStudent(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                          @GraphQLContext CohortSummary cohortSummary,
                                                                                          @GraphQLNonNull @GraphQLArgument(name = "studentId", description = "the student id") UUID studentId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow instructors to request this data.
        affirmPermission(cohortSummary.getId() != null && allowCohortInstructor.test(context.getAuthenticationContext(),
                                                                                     cohortSummary.getId()), ERROR_MSG);

        HistoricalCohortEnrollment historicalEnrollment = cohortEnrollmentService.getHistoricalAccountEnrollment(cohortSummary.getId(), studentId)
                .block();

        // not found
        if (historicalEnrollment == null) {
            return Mono.just(new HistoricalCohortEnrollment()).toFuture();
        }

        final CohortEnrollment currentlyEnrolled = cohortEnrollmentService.getAccountEnrollment(studentId, cohortSummary.getId())
                .block();

        if (currentlyEnrolled == null) {
            return Mono.just(historicalEnrollment.setEnrollmentStatus(EnrollmentStatus.NOT_ENROLLED)).toFuture();
        }

        return Mono.just(historicalEnrollment.setEnrollmentStatus(EnrollmentStatus.ENROLLED)).toFuture();
    }

}
