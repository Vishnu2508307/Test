package com.smartsparrow.cohort.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortGateway;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSettingsGateway;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.data.LtiConsumerCredentialGateway;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerConfiguration;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerKey;
import com.smartsparrow.sso.data.ltiv11.LTIv11Gateway;
import com.smartsparrow.util.ClockProvider;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

class CohortServiceTest {

    @InjectMocks
    private CohortService cohortService;
    @Mock
    private CohortGateway cohortGateway;
    @Mock
    private CohortSettingsGateway cohortSettingsGateway;
    @Mock
    private LtiConsumerCredentialGateway ltiConsumerCredentialGateway;
    @Mock
    private CohortPermissionService cohortPermissionService;
    @Mock
    private CohortEnrollmentService cohortEnrollmentService;
    @Mock
    private AccountService accountService;
    @Mock
    private TeamService teamService;
    @Mock
    private ClockProvider clockProvider;
    @Mock
    private LTIv11Gateway ltIv11Gateway;

    private UUID subscriptionId = UUIDs.timeBased();
    private UUID cohortId = UUIDs.timeBased();
    public static final String PRODUCT_ID = "A103000103955";
    private static final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(clockProvider.get()).thenReturn(clock);
    }

    @Test
    void updateCohort_noSettings() {
        CohortSummary summary = new CohortSummary();
        CohortSettings settings = new CohortSettings().setCohortId(UUIDs.random());
        when(cohortGateway.update(any())).thenReturn(Flux.empty());
        when(cohortSettingsGateway.update(any())).thenReturn(Flux.empty());

        StepVerifier.create(cohortService.updateCohort(summary, settings)).verifyComplete();

        verify(cohortGateway).update(eq(summary));
        verify(cohortSettingsGateway).update(eq(settings));
    }

    @Test
    void updateCohort_withColor() {
        CohortSummary summary = new CohortSummary();
        CohortSettings settings = new CohortSettings();
        settings.setColor("color");
        when(cohortSettingsGateway.update(any())).thenReturn(Flux.empty());
        when(cohortGateway.update(any())).thenReturn(Flux.empty());

        StepVerifier.create(cohortService.updateCohort(summary, settings)).verifyComplete();

        verify(cohortSettingsGateway).update(eq(settings));
        verify(cohortGateway).update(eq(summary));
        verify(cohortSettingsGateway, never()).delete(any());
    }

    @Test
    void updateCohort_withBannerImage() {
        CohortSummary summary = new CohortSummary();
        CohortSettings settings = new CohortSettings();
        settings.setBannerImage("bannerImage");
        when(cohortSettingsGateway.update(any())).thenReturn(Flux.empty());
        when(cohortGateway.update(any())).thenReturn(Flux.empty());

        StepVerifier.create(cohortService.updateCohort(summary, settings)).verifyComplete();

        verify(cohortSettingsGateway).update(eq(settings));
        verify(cohortGateway).update(eq(summary));
        verify(cohortSettingsGateway, never()).delete(any());
    }

    @Test
    void updateCohort_withBannerPattern() {
        CohortSummary summary = new CohortSummary();
        CohortSettings settings = new CohortSettings();
        settings.setBannerPattern("bannerPattern");
        when(cohortSettingsGateway.update(any())).thenReturn(Flux.empty());
        when(cohortGateway.update(any())).thenReturn(Flux.empty());

        StepVerifier.create(cohortService.updateCohort(summary, settings)).verifyComplete();

        verify(cohortSettingsGateway).update(eq(settings));
        verify(cohortGateway).update(eq(summary));
        verify(cohortSettingsGateway, never()).delete(any());
    }

    @Test
    void updateCohort_withProductId() {
        CohortSummary summary = new CohortSummary();
        CohortSettings settings = new CohortSettings();
        settings.setProductId(UUIDs.random().toString());
        when(cohortSettingsGateway.update(any())).thenReturn(Flux.empty());
        when(cohortGateway.update(any())).thenReturn(Flux.empty());

        StepVerifier.create(cohortService.updateCohort(summary, settings)).verifyComplete();

        verify(cohortSettingsGateway).update(eq(settings));
        verify(cohortGateway).update(eq(summary));
        verify(cohortSettingsGateway, never()).delete(any());
    }

    @Test
    void getCohortPayload_noSettings() {
        UUID cohortId = UUID.fromString("ec63c620-a11f-11e8-98e8-7b3817d5416d");
        UUID creatorId = UUID.randomUUID();
        CohortSummary summary = new CohortSummary()
                .setId(cohortId)
                .setName("Name")
                .setType(EnrollmentType.MANUAL)
                .setStartDate(1L)
                .setEndDate(2L)
                .setCreatorId(creatorId)
                .setFinishedDate(UUID.fromString("ec63c620-a11f-11e8-98e8-7b3817d5416d"));
        when(cohortGateway.findCohortSummary(eq(cohortId))).thenReturn(Mono.just(summary));
        when(cohortEnrollmentService.fetchEnrollments(eq(cohortId))).thenReturn(Flux.just(new CohortEnrollment(), new CohortEnrollment(), new CohortEnrollment()));
        when(accountService.getAccountPayload(eq(creatorId))).thenReturn(Mono.empty());
        when(cohortSettingsGateway.findCohortSettings(eq(cohortId))).thenReturn(Mono.empty());
        when(ltiConsumerCredentialGateway.findLtiConsumerCredentialByCohort(eq(cohortId))).thenReturn(Flux.empty());

        CohortPayload result = cohortService.getCohortPayload(cohortId).block();

        assertNotNull(result);
        assertEquals("Name", result.getSummaryPayload().getName());
        assertEquals(EnrollmentType.MANUAL, result.getSummaryPayload().getEnrollmentType());
        assertEquals("Thu, 1 Jan 1970 00:00:00 GMT", result.getSummaryPayload().getStartDate());
        assertEquals("Thu, 1 Jan 1970 00:00:00 GMT", result.getSummaryPayload().getEndDate());
        assertEquals(cohortId, result.getSummaryPayload().getCohortId());
        assertEquals(3, result.getSummaryPayload().getEnrollmentsCount());
        assertEquals(creatorId, result.getSummaryPayload().getCreator().getAccountId());
        assertEquals("Thu, 16 Aug 2018 06:45:11 GMT", result.getSummaryPayload().getCreatedAt());
        assertEquals("Thu, 16 Aug 2018 06:45:11 GMT", result.getSummaryPayload().getFinishedDate());
        // settings payload should never be null
        assertNotNull(result.getSettingsPayload());
    }

    @Test
    void getCohortPayload_withSettings() {
        UUID cohortId = UUID.fromString("ec63c620-a11f-11e8-98e8-7b3817d5416d");
        UUID creatorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CohortSummary summary = new CohortSummary()
                .setId(cohortId)
                .setCreatorId(creatorId);
        when(cohortGateway.findCohortSummary(eq(cohortId))).thenReturn(Mono.just(summary));
        when(cohortEnrollmentService.fetchEnrollments(eq(cohortId))).thenReturn(Flux.empty());
        when(accountService.getAccountPayload(eq(creatorId))).thenReturn(Mono.empty());
        when(cohortSettingsGateway.findCohortSettings(eq(cohortId))).thenReturn(
                Mono.just(new CohortSettings().setColor("color").setBannerPattern("bannerPattern").setBannerImage("bannerImage").setProductId(productId.toString())));
        when(ltiConsumerCredentialGateway.findLtiConsumerCredentialByCohort(eq(cohortId))).thenReturn(Flux.empty());

        CohortPayload result = cohortService.getCohortPayload(cohortId).block();

        assertNotNull(result);
        assertEquals(cohortId, result.getSummaryPayload().getCohortId());
        assertEquals("color", result.getSettingsPayload().getColor());
        assertEquals("bannerPattern", result.getSettingsPayload().getBannerPattern());
        assertEquals("bannerImage", result.getSettingsPayload().getBannerImage());
        assertEquals(productId.toString(), result.getSettingsPayload().getProductId());
    }

    @Test
    void getCohortPayload_noCohort() {
        UUID cohortId = UUID.randomUUID();
        when(cohortGateway.findCohortSummary(eq(cohortId))).thenReturn(Mono.empty());
        when(cohortEnrollmentService.fetchEnrollments(eq(cohortId))).thenReturn(Flux.just(new CohortEnrollment()));
        when(cohortSettingsGateway.findCohortSettings(eq(cohortId))).thenReturn(Mono.just(new CohortSettings().setColor("color")));
        when(ltiConsumerCredentialGateway.findLtiConsumerCredentialByCohort(eq(cohortId))).thenReturn(Flux.empty());

        CohortPayload result = cohortService.getCohortPayload(cohortId).block();

        assertNull(result);
    }

    @Test
    void getCohortSummaryPayload_noEnrollments_nullFinishedDate() {
        final UUID cohortId = UUID.randomUUID();
        final CohortSummary cohortSummary = new CohortSummary()
                .setCreatorId(UUID.randomUUID())
                .setStartDate(System.currentTimeMillis())
                .setEndDate(System.currentTimeMillis())
                .setId(UUIDs.timeBased())
                .setFinishedDate(null)
                .setWorkspaceId(null)
                .setName("cohort name")
                .setType(EnrollmentType.MANUAL);

        when(cohortGateway.findCohortSummary(cohortId)).thenReturn(Mono.just(cohortSummary));
        when(cohortEnrollmentService.fetchEnrollments(cohortId)).thenReturn(Flux.empty());

        when(accountService.getAccountPayload(cohortSummary.getCreatorId())).thenReturn(Mono.just(new AccountPayload()));

        final CohortSummaryPayload payload = cohortService.getCohortSummaryPayload(cohortId).block();

        assertAll(() -> {
            assertNotNull(payload);
            assertEquals(cohortSummary.getId(), payload.getCohortId());
            assertEquals(cohortSummary.getWorkspaceId(), payload.getWorkspaceId());
            assertEquals(cohortSummary.getName(), payload.getName());
            assertEquals(cohortSummary.getType(), payload.getEnrollmentType());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getStartDate()), payload.getStartDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getEndDate()), payload.getEndDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getId()), payload.getCreatedAt());
            assertNull(payload.getFinishedDate());
            assertNotNull(payload.getCreator());
            assertEquals(0, payload.getEnrollmentsCount());
        });
    }

    @Test
    void getCohortSummaryPayload_noAccountPayloadFound() {
        final UUID cohortId = UUID.randomUUID();
        final CohortSummary cohortSummary = new CohortSummary()
                .setCreatorId(UUID.randomUUID())
                .setStartDate(System.currentTimeMillis())
                .setEndDate(System.currentTimeMillis())
                .setId(UUIDs.timeBased())
                .setFinishedDate(null)
                .setWorkspaceId(null)
                .setName("cohort name")
                .setType(EnrollmentType.MANUAL);

        when(cohortGateway.findCohortSummary(cohortId)).thenReturn(Mono.just(cohortSummary));
        when(cohortEnrollmentService.fetchEnrollments(cohortId)).thenReturn(Flux.empty());

        when(accountService.getAccountPayload(cohortSummary.getCreatorId())).thenReturn(Mono.empty());

        final CohortSummaryPayload payload = cohortService.getCohortSummaryPayload(cohortId).block();

        assertAll(() -> {
            assertNotNull(payload);
            assertEquals(cohortSummary.getId(), payload.getCohortId());
            assertEquals(cohortSummary.getWorkspaceId(), payload.getWorkspaceId());
            assertEquals(cohortSummary.getName(), payload.getName());
            assertEquals(cohortSummary.getType(), payload.getEnrollmentType());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getStartDate()), payload.getStartDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getEndDate()), payload.getEndDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getId()), payload.getCreatedAt());
            assertNull(payload.getFinishedDate());
            assertNotNull(payload.getCreator());
            assertEquals(cohortSummary.getCreatorId(), payload.getCreator().getAccountId());
            assertEquals(0, payload.getEnrollmentsCount());
        });
    }

    @Test
    void getCohortSummaryPayload_withFinishedDate() {
        final UUID cohortId = UUID.randomUUID();
        final CohortSummary cohortSummary = new CohortSummary()
                .setCreatorId(UUID.randomUUID())
                .setStartDate(System.currentTimeMillis())
                .setEndDate(System.currentTimeMillis())
                .setId(UUIDs.timeBased())
                .setFinishedDate(UUIDs.timeBased())
                .setWorkspaceId(null)
                .setName("cohort name")
                .setType(EnrollmentType.MANUAL);

        when(cohortGateway.findCohortSummary(cohortId)).thenReturn(Mono.just(cohortSummary));
        when(cohortEnrollmentService.fetchEnrollments(cohortId))
                .thenReturn(Flux.just(new CohortEnrollment(), new CohortEnrollment()));

        when(accountService.getAccountPayload(cohortSummary.getCreatorId())).thenReturn(Mono.just(new AccountPayload()));

        final CohortSummaryPayload payload = cohortService.getCohortSummaryPayload(cohortId).block();

        assertAll(() -> {
            assertNotNull(payload);
            assertEquals(cohortSummary.getId(), payload.getCohortId());
            assertEquals(cohortSummary.getWorkspaceId(), payload.getWorkspaceId());
            assertEquals(cohortSummary.getName(), payload.getName());
            assertEquals(cohortSummary.getType(), payload.getEnrollmentType());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getStartDate()), payload.getStartDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getEndDate()), payload.getEndDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getId()), payload.getCreatedAt());
            assertNotNull(payload.getFinishedDate());
            assertEquals(DateFormat.asRFC1123(cohortSummary.getFinishedDate()), payload.getFinishedDate());
            assertNotNull(payload.getCreator());
            assertEquals(2, payload.getEnrollmentsCount());
        });
    }

    @Test
    void archive() {
        UUID cohortId = UUID.randomUUID();

        when(cohortGateway.setFinish(eq(cohortId), any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
        UUID finishedDate = cohortService.archive(cohortId).block();
        assertNotNull(finishedDate);
        verify(cohortGateway, atLeastOnce()).setFinish(cohortId, finishedDate);
    }

    @Test
    void unarchive() {
        UUID cohortId = UUID.randomUUID();

        when(cohortGateway.setFinish(cohortId, null)).thenReturn(Flux.just(new Void[]{}));

        cohortService.unarchive(cohortId).blockLast();

        verify(cohortGateway, atLeastOnce()).setFinish(cohortId, null);
    }

    @Test
    void createCohort_nullCreatorId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                cohortService.createCohort(null, UUID.randomUUID(), "name", EnrollmentType.MANUAL,
                        2L, 3L, subscriptionId));
        assertEquals("creatorId is required", e.getMessage());
    }

    @Test
    void createCohort_nullWorkspaceId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                cohortService.createCohort(UUID.randomUUID(), null, "name", EnrollmentType.MANUAL,
                        2L, 3L, subscriptionId));
        assertEquals("workspaceId is required", e.getMessage());
    }

    @Test
    void createCohort_nullName() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                cohortService.createCohort(UUID.randomUUID(), UUID.randomUUID(), null, EnrollmentType.MANUAL,
                        2L, 3L, subscriptionId));
        assertEquals("name is required", e.getMessage());
    }

    @Test
    void createCohort_nullType() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                cohortService.createCohort(UUID.randomUUID(), UUID.randomUUID(), "name", null,
                        2L, 3L, subscriptionId));
        assertEquals("enrollmentType is required", e.getMessage());
    }

    @Test
    void createCohort_nullSubscription() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                cohortService.createCohort(UUID.randomUUID(), UUID.randomUUID(), "name", EnrollmentType.MANUAL,
                        2L, 3L, null));
        assertEquals("subscriptionId is required", e.getMessage());
    }

    @Test
    void createCohort_success() {
        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();
        when(cohortGateway.persist(any(CohortSummary.class))).thenReturn(persistPublisher.flux());
        when(cohortPermissionService.saveAccountPermissions(any(UUID.class), any(UUID.class), eq(PermissionLevel.OWNER)))
                .thenReturn(persistPublisher.flux());

        CohortSummary summary = cohortService.createCohort(creatorId, workspaceId, "name", EnrollmentType.MANUAL,
                2L, 3L, subscriptionId).block();

        assertNotNull(summary);
        assertEquals("name", summary.getName());
        assertEquals(EnrollmentType.MANUAL, summary.getType());
        assertEquals(creatorId, summary.getCreatorId());
        assertEquals(workspaceId, summary.getWorkspaceId());
        assertEquals(subscriptionId, summary.getSubscriptionId());
        assertNotNull(summary.getId());
    }

    @Test
    void createSettings_nullCohortId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                cohortService.createSettings(null, "pattern", "color", "image", null));
        assertEquals("cohortId is required", e.getMessage());
    }

    @Test
    void createSettings_success() {
        UUID cohortId = UUID.randomUUID();

        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();

        when(cohortSettingsGateway.persist(any(CohortSettings.class))).thenReturn(persistPublisher.flux());

        CohortSettings settings = cohortService
                .createSettings(cohortId, "pattern", "color", "image", "A103000103955").block();

        assertNotNull(settings);
        assertEquals("pattern", settings.getBannerPattern());
        assertEquals("color", settings.getColor());
        assertEquals("image", settings.getBannerImage());
        assertEquals(cohortId, settings.getCohortId());
        assertEquals("A103000103955", settings.getProductId());
    }

    @Test
    void fetchCohorts() {
        UUID accountId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID cohortId1 = UUID.randomUUID();
        UUID cohortId2 = UUID.randomUUID();
        UUID cohortId3 = UUID.randomUUID();
        when(cohortGateway.findCohortsByAccount(accountId)).thenReturn(Flux.just(cohortId1, cohortId2));
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(cohortGateway.findCohortsByTeam(teamId)).thenReturn(Flux.just(cohortId3, cohortId2));

        List<UUID> result = cohortService.fetchCohorts(accountId).collectList().block();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(cohortId1));
        assertTrue(result.contains(cohortId2));
        assertTrue(result.contains(cohortId3));
    }

    @Test
    void fetchCohorts_noTeams() {
        UUID accountId = UUID.randomUUID();
        UUID cohortId1 = UUID.randomUUID();
        when(cohortGateway.findCohortsByAccount(accountId)).thenReturn(Flux.just(cohortId1));
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        List<UUID> result = cohortService.fetchCohorts(accountId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(cohortId1));
    }

    @Test
    void fetchCohorts_noAccountPermissions() {
        UUID accountId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID cohortId1 = UUID.randomUUID();
        when(cohortGateway.findCohortsByAccount(accountId)).thenReturn(Flux.empty());
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount().setTeamId(teamId)));
        when(cohortGateway.findCohortsByTeam(teamId)).thenReturn(Flux.just(cohortId1));

        List<UUID> result = cohortService.fetchCohorts(accountId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(cohortId1));
    }

    @Test
    void updateLearnerRedirectId_noCohortId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> cohortService.updateLearnerRedirectId(null, UUID.randomUUID()));

        assertNotNull(f);
        assertEquals("cohortId is required", f.getMessage());
        verify(cohortSettingsGateway, never()).updateLearnerRedirectId(any(UUID.class), any(UUID.class));
    }

    @Test
    void updateLearnerRedirectId() {
        UUID cohortId = UUID.randomUUID();
        UUID redirectId = UUID.randomUUID();

        when(cohortSettingsGateway.updateLearnerRedirectId(cohortId, redirectId))
                .thenReturn(Flux.just(new Void[]{}));

        cohortService.updateLearnerRedirectId(cohortId, redirectId)
                .blockLast();

        verify(cohortSettingsGateway).updateLearnerRedirectId(cohortId, redirectId);
    }

    @Test
    void getCohortProductId() {
        when(cohortSettingsGateway.findCohortSettings(cohortId)).thenReturn(Mono.just(new CohortSettings()
                .setProductId(PRODUCT_ID)));
        Mono<String> stringMono = cohortService.fetchCohortProductId(cohortId);
        assertNotNull(stringMono.block());
    }

    @Test
    void saveLTIConsumerKey_nullCohort() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> cohortService.saveLTIConsumerKey(null, null, null));

        assertEquals("cohortSummary is required", f.getMessage());
    }

    @Test
    void saveLTIConsumerKey_nullKey() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> cohortService.saveLTIConsumerKey(new CohortSummary(), null, null));

        assertEquals("consumer key is required", f.getMessage());
    }

    @Test
    void saveLTIConsumerKey_nullSecret() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> cohortService.saveLTIConsumerKey(new CohortSummary(), "key", null));

        assertEquals("consumer secret is required", f.getMessage());
    }

    @Test
    void saveLTIConsumerKey_noLTIConfiguration() {
        final UUID workspaceId = UUID.randomUUID();
        final UUID configurationId = UUID.randomUUID();
        final CohortSummary cohortSummary = new CohortSummary()
                .setId(cohortId)
                .setWorkspaceId(workspaceId);
        final String key = "key";
        final String secret = "secret";

        ArgumentCaptor<LTIv11ConsumerConfiguration> confCaptor = ArgumentCaptor.forClass(LTIv11ConsumerConfiguration.class);
        ArgumentCaptor<LTIv11ConsumerKey> keyCaptor = ArgumentCaptor.forClass(LTIv11ConsumerKey.class);

        when(ltIv11Gateway.findConfigurationByWorkspace(workspaceId))
                .thenReturn(Mono.empty());
        when(ltIv11Gateway.persist(any(LTIv11ConsumerConfiguration.class)))
                .thenReturn(Mono.just(new LTIv11ConsumerConfiguration()
                .setId(configurationId)));

        when(ltIv11Gateway.persist(any(LTIv11ConsumerKey.class)))
                .thenReturn(Mono.just(new LTIv11ConsumerKey()));

        CohortSummary summary = cohortService.saveLTIConsumerKey(cohortSummary, key, secret)
                .block();

        assertNotNull(summary);
        assertEquals(cohortSummary, summary);

        verify(ltIv11Gateway).persist(confCaptor.capture());
        verify(ltIv11Gateway).persist(keyCaptor.capture());

        LTIv11ConsumerConfiguration configuration = confCaptor.getValue();
        assertNotNull(configuration);
        assertNotNull(configuration.getId());
        assertNotNull(configuration.getId());
        assertEquals(workspaceId, configuration.getWorkspaceId());

        LTIv11ConsumerKey consumerKey = keyCaptor.getValue();
        assertNotNull(consumerKey);
        assertNotNull(consumerKey.getId());
        assertEquals(key, consumerKey.getOauthConsumerKey());
        assertEquals(secret, consumerKey.getOauthConsumerSecret());
        assertEquals(cohortId, consumerKey.getCohortId());
        assertEquals(workspaceId, consumerKey.getWorkspaceId());
        assertEquals(configurationId, consumerKey.getConsumerConfigurationId());
        assertFalse(consumerKey.isLogDebug());
    }

    @Test
    void saveLTIConsumerKey_LTIConfigurationFound() {
        final UUID workspaceId = UUID.randomUUID();
        final UUID configurationId = UUID.randomUUID();
        final CohortSummary cohortSummary = new CohortSummary()
                .setId(cohortId)
                .setWorkspaceId(workspaceId);
        final String key = "key";
        final String secret = "secret";

        ArgumentCaptor<LTIv11ConsumerKey> keyCaptor = ArgumentCaptor.forClass(LTIv11ConsumerKey.class);

        when(ltIv11Gateway.findConfigurationByWorkspace(workspaceId))
                .thenReturn(Mono.just(new LTIv11ConsumerConfiguration()
                        .setId(configurationId)));

        when(ltIv11Gateway.persist(any(LTIv11ConsumerKey.class)))
                .thenReturn(Mono.just(new LTIv11ConsumerKey()));

        CohortSummary summary = cohortService.saveLTIConsumerKey(cohortSummary, key, secret)
                .block();

        assertNotNull(summary);
        assertEquals(cohortSummary, summary);

        verify(ltIv11Gateway, never()).persist(any(LTIv11ConsumerConfiguration.class));
        verify(ltIv11Gateway).persist(keyCaptor.capture());

        LTIv11ConsumerKey consumerKey = keyCaptor.getValue();
        assertNotNull(consumerKey);
        assertNotNull(consumerKey.getId());
        assertEquals(key, consumerKey.getOauthConsumerKey());
        assertEquals(secret, consumerKey.getOauthConsumerSecret());
        assertEquals(cohortId, consumerKey.getCohortId());
        assertEquals(workspaceId, consumerKey.getWorkspaceId());
        assertEquals(configurationId, consumerKey.getConsumerConfigurationId());
        assertFalse(consumerKey.isLogDebug());
    }
}
