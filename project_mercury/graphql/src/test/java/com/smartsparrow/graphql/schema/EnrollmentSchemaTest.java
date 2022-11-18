package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentStatus;
import com.smartsparrow.cohort.data.HistoricalCohortEnrollment;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.util.UUIDs;

import graphql.relay.Edge;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class EnrollmentSchemaTest {

    @InjectMocks
    private EnrollmentSchema enrollmentSchema;

    @Mock
    private AllowCohortInstructor allowCohortInstructor;

    @Mock
    private CohortEnrollmentService cohortEnrollmentService;

    @Mock
    private CohortSummary cohortSummary;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(cohortSummary.getId()).thenReturn(cohortId);
        when(allowCohortInstructor.test(authenticationContext, cohortId)).thenReturn(true);
        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContext)).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getEnrollmentsForCohort() {
        UUID accountIdOne = UUIDs.timeBased();
        UUID accountIdTwo = UUIDs.timeBased();
        UUID accountIdThree = UUIDs.timeBased();

        HistoricalCohortEnrollment one = new HistoricalCohortEnrollment()
                .setAccountId(accountIdOne);
        HistoricalCohortEnrollment two = new HistoricalCohortEnrollment()
                .setAccountId(accountIdTwo);
        HistoricalCohortEnrollment three = new HistoricalCohortEnrollment()
                .setAccountId(accountIdThree);

        // different ordering
        when(cohortEnrollmentService.fetchEnrolledAccountIds(cohortId))
                .thenReturn(Flux.just(accountIdOne, accountIdTwo));
        // different ordering
        when(cohortEnrollmentService.fetchHistoricalEnrollments(cohortId))
                .thenReturn(Flux.just(two, one, three));

        Page<HistoricalCohortEnrollment> result = enrollmentSchema
                .getEnrollmentsForCohort(resolutionEnvironment,cohortSummary, null, null).join();

        assertNotNull(result);

        List<Edge<HistoricalCohortEnrollment>> edges = result.getEdges();

        assertNotNull(edges);
        assertEquals(3, edges.size());

        HistoricalCohortEnrollment nodeOne = edges.get(0).getNode();
        assertEquals(accountIdOne, nodeOne.getAccountId());
        assertEquals(EnrollmentStatus.ENROLLED, nodeOne.getEnrollmentStatus());

        HistoricalCohortEnrollment nodeTwo = edges.get(1).getNode();
        assertEquals(accountIdTwo, nodeTwo.getAccountId());
        assertEquals(EnrollmentStatus.ENROLLED, nodeTwo.getEnrollmentStatus());

        HistoricalCohortEnrollment nodeThree = edges.get(2).getNode();
        assertEquals(accountIdThree, nodeThree.getAccountId());
        assertEquals(EnrollmentStatus.NOT_ENROLLED, nodeThree.getEnrollmentStatus());
    }

    @Test
    void getEnrollmentForCohortByStudent() {
        UUID accountId = UUID.randomUUID();

        when(cohortEnrollmentService.getAccountEnrollment(accountId, cohortId)).thenReturn(Mono.empty());
        when(cohortEnrollmentService.getHistoricalAccountEnrollment(cohortId, accountId))
                .thenReturn(Mono.just(new HistoricalCohortEnrollment()
                        .setAccountId(accountId)
                        .setCohortId(cohortId)));

        HistoricalCohortEnrollment found = (HistoricalCohortEnrollment) enrollmentSchema
                .getEnrollmentsForCohortByStudent(resolutionEnvironment, cohortSummary, accountId).join();

        assertNotNull(found);
        assertEquals(accountId, found.getAccountId());
        assertEquals(cohortId, found.getCohortId());
        assertEquals(EnrollmentStatus.NOT_ENROLLED, found.getEnrollmentStatus());
    }

}
