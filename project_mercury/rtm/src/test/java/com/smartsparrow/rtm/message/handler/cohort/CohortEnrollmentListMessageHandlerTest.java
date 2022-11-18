package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.rtm.message.handler.cohort.CohortEnrollmentListMessageHandler.WORKSPACE_COHORT_ENROLLMENT_LIST_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.payload.CohortEnrollmentPayload;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CohortEnrollmentListMessageHandlerTest {

    @Mock
    private CohortService cohortService;

    @Mock
    private CohortEnrollmentService cohortEnrollmentService;

    @InjectMocks
    private CohortEnrollmentListMessageHandler handler;

    private CohortGenericMessage cohortGenericMessage;
    private static final UUID cohortId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        cohortGenericMessage = mock(CohortGenericMessage.class);

        when(cohortGenericMessage.getCohortId()).thenReturn(cohortId);
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(cohortGenericMessage.getCohortId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(cohortGenericMessage));

        assertEquals("cohortId is required", ((RTMValidationException) t).getErrorMessage());
        assertEquals(WORKSPACE_COHORT_ENROLLMENT_LIST_ERROR, ((RTMValidationException) t).getType());

    }

    @Test
    void validate_cohortNotFound() {
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.empty());

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(cohortGenericMessage));

        assertEquals(String.format("cohort not found for id %s", cohortId), ((RTMValidationException) t).getErrorMessage());
        assertEquals(WORKSPACE_COHORT_ENROLLMENT_LIST_ERROR, ((RTMValidationException) t).getType());
    }

    @Test
    void handle_noEnrollmentsFound() throws WriteResponseException {
        when(cohortEnrollmentService.fetchEnrollments(cohortId)).thenReturn(Flux.empty());

        handler.handle(session, cohortGenericMessage);

        String expected = "{\"type\":\"workspace.cohort.enrollment.list.ok\",\"response\":{\"enrollments\":[]}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_enrollmentsFound() throws WriteResponseException {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        CohortEnrollment one = buildCohortEnrollment(cohortId, accountIdOne);
        CohortEnrollment two = buildCohortEnrollment(cohortId, accountIdTwo);

        CohortEnrollmentPayload payloadOne = CohortEnrollmentPayload.from(one, new AccountIdentityAttributes()
                .setAccountId(accountIdOne)
                .setPrimaryEmail("some.email@dev.dev")
                .setGivenName("Alice")
                .setFamilyName("Dev"), new AccountAvatar());
        CohortEnrollmentPayload payloadTwo = CohortEnrollmentPayload.from(two, new AccountIdentityAttributes(), new AccountAvatar());

        when(cohortEnrollmentService.fetchEnrollments(cohortId)).thenReturn(Flux.just(one, two));
        when(cohortEnrollmentService.getCohortEnrollmentPayload(one)).thenReturn(Mono.just(payloadOne));
        when(cohortEnrollmentService.getCohortEnrollmentPayload(two)).thenReturn(Mono.just(payloadTwo));

        handler.handle(session, cohortGenericMessage);

        String expected = "{\"type\":\"workspace.cohort.enrollment.list.ok\"," +
                "\"response\":{" +
                "\"enrollments\":[{" +
                    "\"cohortId\":\""+cohortId+"\"," +
                    "\"enrolledAt\":\""+DateFormat.asRFC1123(one.getEnrollmentDate())+"\"," +
                    "\"enrollmentType\":\"MANUAL\"," +
                    "\"accountSummary\":{" +
                        "\"accountId\":\""+accountIdOne+"\"," +
                        "\"givenName\":\"Alice\"," +
                        "\"familyName\":\"Dev\"," +
                        "\"primaryEmail\":\"some.email@dev.dev\"" +
                    "}" +
                "},{" +
                    "\"cohortId\":\""+cohortId+"\"," +
                    "\"enrolledAt\":\""+DateFormat.asRFC1123(two.getEnrollmentDate()) +"\"," +
                    "\"enrollmentType\":\"MANUAL\"," +
                    "\"accountSummary\":{" +
                        "\"accountId\":\""+accountIdTwo+"\"" +
                    "}}]}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        System.out.println(DateFormat.asRFC1123(two.getEnrollmentDate()));
    }

    private CohortEnrollment buildCohortEnrollment(UUID cohortId, UUID accountId) {
        return new CohortEnrollment()
                .setEnrollmentType(EnrollmentType.MANUAL)
                .setCohortId(cohortId)
                .setAccountId(accountId)
                .setEnrollmentDate(UUIDs.timeBased());
    }

}
