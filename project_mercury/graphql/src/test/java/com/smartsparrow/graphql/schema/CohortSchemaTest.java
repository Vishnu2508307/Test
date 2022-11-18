package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.PassportService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CohortSchemaTest {

    @InjectMocks
    private CohortSchema cohortSchema;
    @Mock
    private CohortService cohortService;
    @Mock
    private AllowCohortInstructor allowCohortInstructor;
    @Mock
    private AllowEnrolledStudent allowEnrolledStudent;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private CohortEnrollmentService cohortEnrollmentService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    @Mock
    private PassportService passportService;

    private ResolutionEnvironment resolutionEnvironment;

    @Mock
    private CohortSettings cohortSettings;
    private static final Learn learn = new Learn();
    private static final UUID invalidCohortId = UUIDs.timeBased();
    private static final UUID cohortId = UUIDs.timeBased();
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final String pearsonUid = "pearsonUid";
    private static final String pearsonToken = "pearsonToken";
    private WebSessionToken webSessionToken;
    public static final String PRODUCT_ID = "A103000103955";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        //
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        webSessionToken = mock(WebSessionToken.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getWebSessionToken()).thenReturn(webSessionToken);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));
        when(authenticationContext.getAuthenticationType()).thenReturn(AuthenticationType.IES);
        when(authenticationContext.getPearsonUid()).thenReturn(pearsonUid);
        when(authenticationContext.getPearsonToken()).thenReturn(pearsonToken);

        when(cohortService.fetchCohortSummary(invalidCohortId)).thenReturn(Mono.empty());
        CohortSummary summary = new CohortSummary() //
                .setSubscriptionId(subscriptionId).setId(cohortId);
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.just(summary));
        when(cohortService.fetchCohortSettings(cohortId)).thenReturn(Mono.just(cohortSettings));
        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(this.authenticationContextProvider.get())).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    @DisplayName("Should fail if cohortId is not supplied")
    void getCohort_cohortIdNotSupplied() {
        assertThrows(IllegalArgumentFault.class, () -> cohortSchema.getCohort(resolutionEnvironment, learn, null).join());
    }

    @Test
    @DisplayName("Should fail if cohortId is invalid")
    void getCohort_cohortIdInvalid() {
        when(cohortService.fetchCohortSettings(invalidCohortId)).thenReturn(Mono.empty());
        cohortSchema
                .getCohort(resolutionEnvironment, learn, invalidCohortId)
                .handle((cohortSummary, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return cohortSummary;
                });
    }

    @Test
    @DisplayName("Should return cohort if student is enrolled")
    void getCohort_studentIsEnrolled() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortId)).thenReturn(true);
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);

        assertNotNull(cohortSchema.getCohort(resolutionEnvironment, learn, cohortId).join());
    }

    @Test
    @DisplayName("Should return cohort if instructor has permissions")
    void getCohort_instructorHasPermissions() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);

        assertNotNull(cohortSchema.getCohort(resolutionEnvironment, learn, cohortId).join());
    }

    @Test
    @DisplayName("Should return cohort if user is both instructor and student")
    void getCohort_instructorAndStudent() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);

        assertNotNull(cohortSchema.getCohort(resolutionEnvironment, learn, cohortId).join());
    }

    @Test
    @DisplayName("Should return cohort and auto-enroll")
    void getCohort_autoEnroll() {
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);
        when(webSessionToken.getAuthoritySubscriptionId()).thenReturn(subscriptionId);
        when(cohortEnrollmentService.enrollAccount(any(), any(), any(EnrollmentType.class), any(String.class))).thenReturn(Mono.empty());
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.just(new CohortSummary()
                .setId(cohortId)
                .setType(EnrollmentType.OPEN)));
        assertNotNull(cohortSchema.getCohort(resolutionEnvironment, learn, cohortId).join());

        verify(cohortEnrollmentService).enrollAccount(accountId, cohortId, EnrollmentType.OPEN, pearsonUid);
    }

    @Test
    @DisplayName("It should fail when user is not entitled on enrollmentType PASSPORT")
    void getCohort_passportFail() {
        TestPublisher<Boolean> publisher = TestPublisher.create();
        publisher.error(new PermissionFault("not entitled"));

        String productURN = "productURN";

        when(allowEnrolledStudent.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);
        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);
        when(cohortService.fetchCohortSummary(cohortId))
                .thenReturn(Mono.just(new CohortSummary().setId(cohortId).setType(EnrollmentType.PASSPORT)));

        when(cohortSettings.getProductId()).thenReturn(productURN);

        when(passportService.checkEntitlement(pearsonUid, productURN)).thenReturn(publisher.mono());

        cohortSchema
                .getCohort(resolutionEnvironment, learn, cohortId)
                .handle((cohortSummary, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    assertEquals("User does not have permissions to view cohort", throwable.getMessage());
                    return cohortSummary;
                })
                .join();

        verify(cohortEnrollmentService, never())
                .enrollAccount(any(UUID.class), any(UUID.class), any(EnrollmentType.class), any(String.class));

    }

    @Test
    @DisplayName("It should enroll when the user is entitled on enrollmentType PASSPORT")
    void getCohort_passportSuccess() {
        String productURN = "productURN";
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.just(new CohortSummary()
                .setId(cohortId)
                .setType(EnrollmentType.PASSPORT)));
        when(cohortEnrollmentService.enrollAccount(any(), any(), any(EnrollmentType.class), anyString())).thenReturn(Mono.empty());
        when(cohortSettings.getProductId()).thenReturn(productURN);
        when(passportService.checkEntitlement(pearsonUid, productURN)).thenReturn(Mono.just(true));

        CohortSummary summary = cohortSchema.getCohort(resolutionEnvironment, learn, cohortId).join();
        verify(cohortEnrollmentService).enrollAccount(accountId, cohortId, EnrollmentType.PASSPORT, pearsonUid);

        assertNotNull(summary);


    }

    @Test
    void getCohortProductId(){
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);
        when(cohortService.fetchCohortProductId(cohortId)).thenReturn(Mono.just(PRODUCT_ID));
        String cohortProductId = cohortSchema.getProductId(new CohortSummary().setId(cohortId)).join();
        assertNotNull(cohortProductId);
    }

    @Test
    void getCohortInValidProductId(){
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(true);
        when(cohortService.fetchCohortProductId(cohortId)).thenReturn(Mono.empty());
        String cohortProductId = cohortSchema.getProductId(new CohortSummary().setId(cohortId)).join();
        assertNull(cohortProductId);
    }
}
