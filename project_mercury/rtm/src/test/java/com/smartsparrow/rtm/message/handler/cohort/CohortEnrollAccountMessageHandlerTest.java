package com.smartsparrow.rtm.message.handler.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
import com.google.inject.Provider;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.payload.CohortEnrollmentPayload;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortEnrollmentMessage;
import com.smartsparrow.rtm.subscription.cohort.enrolled.CohortEnrolledRTMProducer;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CohortEnrollAccountMessageHandlerTest {

    @Mock
    private CohortService cohortService;

    @Mock
    private AccountService accountService;

    @Mock
    private CohortEnrollmentService cohortEnrollmentService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private CohortEnrolledRTMProducer cohortEnrolledRTMProducer;

    @InjectMocks
    private CohortEnrollAccountMessageHandler handler;
    private CohortEnrollmentMessage message;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID enrolledBy = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final String messageId = "messageId";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        message = mock(CohortEnrollmentMessage.class);

        when(message.getAccountId()).thenReturn(accountId);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getId()).thenReturn(messageId);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(enrolledBy));
    }

    @Test
    void validate_accountIdNotSupplied() {
        when(message.getAccountId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;
        assertEquals("accountId is required", e.getErrorMessage());
        assertEquals("workspace.cohort.account.enroll.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(message.getCohortId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;
        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals("workspace.cohort.account.enroll.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_cohortSummaryNotFound() {
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.empty());

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;
        assertEquals(String.format("cohort not found for id %s", cohortId), e.getErrorMessage());
        assertEquals("workspace.cohort.account.enroll.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_accountNotFound() {
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.just(new CohortSummary()));
        when(accountService.findById(message.getAccountId())).thenReturn(Flux.empty());

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        RTMValidationException e = (RTMValidationException) t;
        assertEquals(String.format("account not found for id %s", accountId), e.getErrorMessage());
        assertEquals("workspace.cohort.account.enroll.error", e.getType());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void handle_success() throws WriteResponseException {
        CohortEnrollment cohortEnrollment = new CohortEnrollment()
        .setEnrollmentDate(UUIDs.timeBased())
        .setEnrollmentType(EnrollmentType.MANUAL)
        .setAccountId(accountId)
        .setCohortId(cohortId);

        when(cohortEnrollmentService.enrollAccount(accountId, cohortId, enrolledBy))
                .thenReturn(Mono.just(cohortEnrollment));

        when(cohortEnrollmentService.getCohortEnrollmentPayload(cohortEnrollment))
                .thenReturn(Mono.just(CohortEnrollmentPayload
                        .from(cohortEnrollment, new AccountIdentityAttributes(), new AccountAvatar())));
        when(cohortEnrolledRTMProducer.buildCohortEnrolledRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortEnrolledRTMProducer);

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.cohort.account.enroll.ok\"," +
                "\"response\":{" +
                    "\"enrollment\":{" +
                        "\"cohortId\":\""+cohortId+"\"," +
                        "\"enrolledAt\":\""+DateFormat.asRFC1123(cohortEnrollment.getEnrollmentDate()) +"\"," +
                        "\"enrollmentType\":\"MANUAL\"," +
                        "\"accountSummary\":{" +
                            "\"accountId\":\""+accountId+"\"" +
                        "}" +
                    "}" +
                "}," +
                "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortEnrolledRTMProducer).buildCohortEnrolledRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortEnrolledRTMProducer).produce();
    }

}
