package com.smartsparrow.rest.resource.sso;

import static com.smartsparrow.sso.service.LTIParam.OAUTH_CONSUMER_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.google.inject.Provider;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.BronteWebToken;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebToken;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerKey;
import com.smartsparrow.sso.service.LTI11ConsumerCredentials;
import com.smartsparrow.sso.service.LTIConsumerCredentials;
import com.smartsparrow.sso.service.LTIVersion;
import com.smartsparrow.sso.service.LTIWebSession;
import com.smartsparrow.sso.service.LTIv11Service;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class LTIv11ResourceTest {

    @InjectMocks
    LTIv11Resource ltIv11Resource;

    @Mock
    CohortService cohortService;

    @Mock
    DeploymentService deploymentService;

    @Mock
    LTIConfig ltiConfig;

    @Mock
    CohortEnrollmentService cohortEnrollmentService;

    @Mock
    LTIv11Service ltIv11Service;

    @Mock
    AuthenticationService<LTIConsumerCredentials, LTIWebSession> authenticationService;

    @Mock
    RedissonReactiveClient redissonReactiveClient;

    @Mock
    RBucketReactive<Map<String, String>> redissonReactiveBucket;

    @Mock
    private Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String productId = "4d477510-c5af-11ec-9d64-0242ac120002";
    private static final UUID deploymentId = UUID.fromString("e1380220-3434-11eb-964f-f38a7a65449d");
    private static final UUID cohortId = UUID.fromString("8c7df708-adec-11eb-8529-0242ac130003");

    private static final String validParamContinueTo = "https://learning.pearson.com/" + cohortId + "/" + deploymentId;
    private static final String validOnDemandParamContinueTo = "https://learning.pearson.com/ondemand/product/" + productId;

    private static final String ltiSmsCourseId = "123456789";
    private static final String ltiDiscipline = "Biology";
    private static final String labId= "bb4889b0-d896-11ec-a46c-9fb00cfc9726/ecf47b50-dd45-11ec-8cdd-c16e69997ffe";
    private static final String lmsCourseId = ltiSmsCourseId + ':' + ltiDiscipline;
    private static final String key = "fooKey";
    private static final String secret = "barSecret";

    // required for the lti launch + ies flow to work
    private static final String IESTokenMock = "eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVmMjBlMDlkMWQ0Y" +
            "jc0MDFkZmNjYmZiMiIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwiZXhwIjoxNTk3MTI0NjQxLCJpYXQiOjE1OTcxMjI4NDEsImNsaWVud" +
            "F9pZCI6Im9YNVZtNlNFRWVTaTVRQkVBVTAwODF0eDIwVUhFNDY5Iiwic2Vzc2lkIjoiNzY1ZmVjZDAtMWU5ZC00ZDkxLWEzNDktMDI2N" +
            "zRkNzRkNGI4In0.Bd89NMcbydGhzv_QkP-rCXUqNNrPhl9qwXQ0czz_cKgsI66Bqi9aAcaTGeSz2awbFlOzDfrYm9fkwrlbq0yaeowjo" +
            "SVw6BAhXct_vqv83_agcY3w5fhJmpl-gUL4wj3uZIg8uKHXBF8fhjaNLdIO9HmahwAocSpH71EtLOD62nnGv3EmsF9Hzw0abpPGMSF9g" +
            "DUqRS3rjXzrAkjRzX9CX1A_odYPkP65UYSgHNfVKP7jjJHS1x-v2um6GpX435RO-F38LPRy336mEeoGoZv6X9q6i5JA5H2dauhLna728" +
            "q3Fmg2kKsLlvGi148JM4wNb6-DzxBnq_LyES0e7Iwkg1w";
    private static final String piUserId = "piUserId";

    //
    HttpHeaders httpHeaders = null;
    MultivaluedMap<String, String> bodyParams = null;
    Cookie existingBearerTokenCookie = null;
    Cookie PiAuthSessionCookie = null;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MutableAuthenticationContext mutableAuthenticationContext = mock(MutableAuthenticationContext.class);
        httpHeaders = mock(HttpHeaders.class);
        MultivaluedHashMap<String, String> requestHeaders = new MultivaluedHashMap<>();
        when(httpHeaders.getRequestHeaders()).thenReturn(requestHeaders);

        bodyParams = new MultivaluedHashMap<>();
        bodyParams.add(OAUTH_CONSUMER_KEY.getValue(), key);
        bodyParams.add("custom_sms_course_id", ltiSmsCourseId);
        bodyParams.add("custom_product_discipline", ltiDiscipline);
        bodyParams.add("custom_lab_id", labId);

        existingBearerTokenCookie = new Cookie("bearerToken", "cookieMonster");
        PiAuthSessionCookie = new Cookie("PiAuthSession", IESTokenMock);

        WebToken webToken = new BronteWebToken("token")
                .setCreatedTs(System.currentTimeMillis())
                .setValidUntilTs(System.currentTimeMillis() + 10000);

        when(cohortService.fetchCohortSummary(cohortId))
                .thenReturn(Mono.just(new CohortSummary()
                        .setId(cohortId)
                        .setWorkspaceId(workspaceId)));


        when(authenticationService.authenticate(any(LTIConsumerCredentials.class))).thenReturn(Mono.just(new LTIWebSession(new Account()
                .setId(accountId))
                .setWebToken(webToken)));

        when(cohortEnrollmentService.findCohortEnrollment(accountId,cohortId)).thenReturn(Mono.just(new CohortEnrollment()
                                                                                                            .setAccountId(accountId)
                                                                                                            .setCohortId(cohortId)));

        when(ltIv11Service.findConsumerKey(workspaceId, cohortId, key))
                .thenReturn(Mono.just(new LTIv11ConsumerKey()
                        .setCohortId(cohortId)
                        .setWorkspaceId(workspaceId)
                        .setConsumerConfigurationId(UUID.randomUUID())
                        .setId(UUID.randomUUID())
                        .setOauthConsumerKey(key)
                        .setLogDebug(true)
                        .setOauthConsumerSecret(secret)));

        when(redissonReactiveBucket.set(any(Map.class), eq(1l), eq(TimeUnit.DAYS)))
                     .thenReturn(Mono.empty());
        doReturn(redissonReactiveBucket).when(redissonReactiveClient).getBucket(any(String.class));

        when(mutableAuthenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);
    }

    @Test
    void allowed_redirectUrl() {
        ArgumentCaptor<LTIConsumerCredentials> credentialsCaptor = ArgumentCaptor.forClass(LTIConsumerCredentials.class);

        ltIv11Resource.launchRequestHandler(httpHeaders, bodyParams, validParamContinueTo, existingBearerTokenCookie, null);

        verify(authenticationService).authenticate(credentialsCaptor.capture());

        LTIConsumerCredentials credentials = credentialsCaptor.getValue();

        assertNotNull(credentials);
        assertEquals(LTIVersion.VERSION_1_1, credentials.getLTIVersion());

        LTI11ConsumerCredentials creds = (LTI11ConsumerCredentials) credentials;

        assertNotNull(creds);
        assertEquals(existingBearerTokenCookie.getValue(), creds.getInvalidateBearerToken());
        assertEquals(cohortId, creds.getCohortId());
        assertEquals(key, creds.getKey());
        assertEquals(secret, creds.getSecret());
        assertEquals(workspaceId, creds.getWorkspaceId());
        assertEquals(validParamContinueTo, creds.getUrl());
        assertTrue(creds.isLogDebug());
        assertNotNull(creds.getLtiMessage());
        assertNull(creds.getPiToken());
    }

    @Test
    void allowed_redirectUrl_andEnrollUserIfNotExists() {
        ArgumentCaptor<LTIConsumerCredentials> credentialsCaptor = ArgumentCaptor.forClass(LTIConsumerCredentials.class);

        ltIv11Resource.launchRequestHandler(httpHeaders, bodyParams, validParamContinueTo, existingBearerTokenCookie, PiAuthSessionCookie);

        verify(authenticationService).authenticate(credentialsCaptor.capture());

        LTIConsumerCredentials credentials = credentialsCaptor.getValue();

        assertNotNull(credentials);
        assertEquals(LTIVersion.VERSION_1_1, credentials.getLTIVersion());

        LTI11ConsumerCredentials creds = (LTI11ConsumerCredentials) credentials;

        assertNotNull(creds);
        assertEquals(existingBearerTokenCookie.getValue(), creds.getInvalidateBearerToken());
        assertEquals(cohortId, creds.getCohortId());
        assertEquals(key, creds.getKey());
        assertEquals(secret, creds.getSecret());
        assertEquals(workspaceId, creds.getWorkspaceId());
        assertEquals(validParamContinueTo, creds.getUrl());
        assertTrue(creds.isLogDebug());
        assertNotNull(creds.getLtiMessage());
        assertNotNull(creds.getPiToken());
    }

    //uncomment test cases after sometime
    /*@Test
    void allowed_onDemandRedirectUrl() {
        when(cohortService.findIdByLmsCourse(lmsCourseId)).thenReturn(Mono.just(cohortId));
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.just(deploymentId));

        ArgumentCaptor<LTIConsumerCredentials> credentialsCaptor = ArgumentCaptor.forClass(LTIConsumerCredentials.class);

        ltIv11Resource.launchRequestHandler(httpHeaders, bodyParams, validOnDemandParamContinueTo, existingBearerTokenCookie, null);

        verify(authenticationService).authenticate(credentialsCaptor.capture());

        LTIConsumerCredentials credentials = credentialsCaptor.getValue();

        assertNotNull(credentials);
        assertEquals(LTIVersion.VERSION_1_1, credentials.getLTIVersion());

        LTI11ConsumerCredentials creds = (LTI11ConsumerCredentials) credentials;

        assertNotNull(creds);
        assertEquals(existingBearerTokenCookie.getValue(), creds.getInvalidateBearerToken());
        assertEquals(cohortId, creds.getCohortId());
        assertEquals(key, creds.getKey());
        assertEquals(secret, creds.getSecret());
        assertEquals(workspaceId, creds.getWorkspaceId());
        assertEquals(validParamContinueTo, creds.getUrl());
        assertTrue(creds.isLogDebug());
        assertNotNull(creds.getLtiMessage());
        assertNull(creds.getPiToken());
    }*/

    //uncomment test cases after sometime
    /*@Test
    void allowed_onDemandRedirectUrl_andCreateCohortDynamically() {
        UUID tempCohortId = UUID.randomUUID();
        CohortSummary tempCohortSummary = new CohortSummary()
                .setId(tempCohortId)
                .setCreatorId(UUID.randomUUID())
                .setWorkspaceId(workspaceId)
                .setName("On-Demand Cohort Template")
                .setType(EnrollmentType.LTI)
                .setSubscriptionId(UUID.randomUUID());
        CohortSettings tempCohortSettings = new CohortSettings()
                .setCohortId(tempCohortId)
                .setProductId(productId);

        when(cohortService.findIdByLmsCourse(lmsCourseId)).thenReturn(Mono.empty());
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.just(deploymentId));
        when(cohortService.findIdByProduct(productId)).thenReturn(Mono.just(tempCohortId));
        when(cohortService.fetchCohortSummary(tempCohortId)).thenReturn(Mono.just(tempCohortSummary));
        when(cohortService.fetchCohortSettings(tempCohortId)).thenReturn(Mono.just(tempCohortSettings));
        when(cohortService.createCohort(any(UUID.class), eq(tempCohortSummary.getCreatorId()), eq(tempCohortSummary.getWorkspaceId()),
                                        eq(tempCohortSummary.getName()), eq(EnrollmentType.LTI),
                                        nullable(Long.class), nullable(Long.class),
                                        eq(tempCohortSummary.getSubscriptionId())))
                .thenReturn(Mono.just(new CohortSummary()));
        when(cohortService.createSettings(any(UUID.class),
                                          nullable(String.class), nullable(String.class), nullable(String.class),
                                          eq(productId)))
                .thenReturn(Mono.just(new CohortSettings()));
        when(ltiConfig.getKey()).thenReturn("key");
        when(ltiConfig.getSecret()).thenReturn("secret");
        when(cohortService.saveLTIConsumerKey(any(CohortSummary.class), eq("key"), eq("secret")))
                .thenReturn(Mono.just(new CohortSummary()));
        when(deploymentService.saveDeploymentCohortId(eq(cohortId), eq(deploymentId))).thenReturn(Mono.empty());
        when(cohortService.saveCohortInstanceId(eq(tempCohortId), eq(cohortId))).thenReturn(Mono.empty());
        when(cohortService.saveLmsCourseCohortId(eq(lmsCourseId), any(UUID.class))).thenReturn(Mono.empty());

        ArgumentCaptor<LTIConsumerCredentials> credentialsCaptor = ArgumentCaptor.forClass(LTIConsumerCredentials.class);

        try (MockedStatic<UUIDs> mockedUUIDs = Mockito.mockStatic(com.smartsparrow.util.UUIDs.class)) {
            // this is control the newCohortId generated in LTIv11Resource:fetchOnDemandCohortId(...)
            mockedUUIDs.when(com.smartsparrow.util.UUIDs::timeBased).thenReturn(cohortId);

            ltIv11Resource.launchRequestHandler(httpHeaders,
                                                bodyParams,
                                                validOnDemandParamContinueTo,
                                                existingBearerTokenCookie,
                                                null);
        }

        verify(authenticationService).authenticate(credentialsCaptor.capture());

        LTIConsumerCredentials credentials = credentialsCaptor.getValue();

        assertNotNull(credentials);
        assertEquals(LTIVersion.VERSION_1_1, credentials.getLTIVersion());

        LTI11ConsumerCredentials creds = (LTI11ConsumerCredentials) credentials;

        assertNotNull(creds);
        assertEquals(existingBearerTokenCookie.getValue(), creds.getInvalidateBearerToken());
        assertEquals(cohortId, creds.getCohortId());
        assertEquals(key, creds.getKey());
        assertEquals(secret, creds.getSecret());
        assertEquals(workspaceId, creds.getWorkspaceId());
        assertEquals(validParamContinueTo, creds.getUrl());
        assertTrue(creds.isLogDebug());
        assertNotNull(creds.getLtiMessage());
        assertNull(creds.getPiToken());
    }*/

    @Test
    void empty_redirectUrl() {
        final String paramContinueTo = "";
        assertThrows(IllegalArgumentFault.class, () -> {
            ltIv11Resource.launchRequestHandler(httpHeaders, bodyParams, paramContinueTo, existingBearerTokenCookie, null);
        });
    }

    @Test
    void nonWhitelisted_redirectUrl() {
        final String paramContinueTo = "https://www.evil.site/redirect/to/something";
        assertThrows(IllegalArgumentFault.class, () -> {
            ltIv11Resource.launchRequestHandler(httpHeaders, bodyParams, paramContinueTo, existingBearerTokenCookie, null);
        });
    }

    @Test
    void invalid_consumer_key() {
        when(ltIv11Service.findConsumerKey(any(UUID.class), any(UUID.class), any(String.class)))
                .thenReturn(Mono.empty());

        assertThrows(IllegalArgumentFault.class, () -> {
            ltIv11Resource.launchRequestHandler(httpHeaders, bodyParams, validParamContinueTo, existingBearerTokenCookie, null);
        });
    }

}