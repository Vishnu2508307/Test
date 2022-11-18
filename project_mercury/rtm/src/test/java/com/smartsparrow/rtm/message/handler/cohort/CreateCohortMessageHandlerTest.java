package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.payload.CohortSettingsPayload;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.exception.DateTimeFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.cohort.CreateCohortMessage;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

class CreateCohortMessageHandlerTest {

    private static final UUID accountID = UUID.randomUUID();

    private Session session;
    private CreateCohortMessageHandler createCohortMessageHandler;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private CohortService cohortService;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private Account account;
    @Mock
    private LTIConfig ltiConfig;

    UUID invalidWorkspaceId = UUIDs.timeBased();
    UUID workspaceId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        createCohortMessageHandler = new CreateCohortMessageHandler(authenticationContextProvider, cohortService,
                                                                    workspaceService, ltiConfig);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountID);

        when(workspaceService.fetchById(invalidWorkspaceId)).thenReturn(Mono.empty());
        Workspace w = new Workspace().setId(workspaceId).setSubscriptionId(UUIDs.timeBased());
        when(workspaceService.fetchById(workspaceId)).thenReturn(Mono.just(w));
    }

    @Test
    void validate_noName() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);

        RTMValidationException t = assertThrows(RTMValidationException.class,
                () -> createCohortMessageHandler.validate(message));

        assertEquals("name is required", t.getErrorMessage());

        when(message.getName()).thenReturn("");
        t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("name is required", t.getErrorMessage());
    }

    @Test
    void validate_noEnrollmentType() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);
        when(message.getName()).thenReturn("name");

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("enrollmentType is required", t.getErrorMessage());
    }

    @Test
    void validate_invalidEnrollmentType() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);
        when(message.getName()).thenReturn("name");
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LMS);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("enrollmentType not supported", t.getMessage());
    }

    @Test
    void validate_noWorkspaceId() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);

        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getWorkspaceId()).thenReturn(null);
        when(message.getName()).thenReturn("name");


        RTMValidationException t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("workspaceId is required", t.getErrorMessage());
    }

    @Test
    void validate_workspaceNotFound() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);

        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getWorkspaceId()).thenReturn(invalidWorkspaceId);
        when(message.getName()).thenReturn("name");

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("workspace not found for id " + invalidWorkspaceId, t.getErrorMessage());
    }

    @Test
    void validate_success() throws RTMValidationException {
        CreateCohortMessage message = mock(CreateCohortMessage.class);
        when(message.getName()).thenReturn("name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getWorkspaceId()).thenReturn(UUID.randomUUID());

        when(workspaceService.fetchById(any())).thenReturn(Mono.just(new Workspace()));


        createCohortMessageHandler.validate(message);
    }

    @Test
    void handle_success() throws IOException {
        String bannerImage = "bannerImage";
        String bannerPattern = "bannerPattern";
        String color = "color";
        String startDate = "Fri, 17 Aug 2018 00:00:00 GMT";
        String endDate = "Sat, 17 Aug 2019 00:00:00 GMT";
        String name = "cohort name";
        String productId = "A103000103955";

        CreateCohortMessage message = new CreateCohortMessage()
                .setBannerImage(bannerImage)
                .setBannerPattern(bannerPattern)
                .setColor(color)
                .setEndDate(endDate)
                .setEnrollmentType(EnrollmentType.OPEN)
                .setName(name)
                .setStartDate(startDate)
                .setWorkspaceId(workspaceId)
                .setProductId(productId);

        CohortSummary summary = new CohortSummary()
                .setId(UUID.randomUUID())
                .setType(EnrollmentType.OPEN);

        CohortSummaryPayload summaryPayload = new CohortSummaryPayload()
                .setName("CohortName")
                .setEnrollmentType(EnrollmentType.OPEN)
                .setStartDate("startDate")
                .setEndDate("endDate")
                .setCohortId(UUID.randomUUID());

        CohortSettingsPayload settingsPayload = new CohortSettingsPayload()
                .setBannerImage("bannerImage")
                .setBannerPattern("bannerPattern")
                .setColor("color")
                .setProductId(productId);

        CohortPayload cohortPayload = new CohortPayload()
                .setSettingsPayload(settingsPayload)
                .setSummaryPayload(summaryPayload);

        when(cohortService.createCohort(eq(accountID), eq(workspaceId), eq(name), eq(EnrollmentType.OPEN), any(), any(), any()))
                .thenReturn(Mono.just(summary));
        when(cohortService.createSettings(any(UUID.class),
                                          eq(bannerPattern),
                                          eq(color),
                                          eq(bannerImage),
                                          eq(productId)))
                .thenReturn(Mono.empty());
        when(cohortService.getCohortPayload(any())).thenReturn(Mono.just(cohortPayload));

        createCohortMessageHandler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("workspace.cohort.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("cohort"));
            assertEquals(2, responseMap.size());
            Map summaryMap = (Map) responseMap.get("summary");
            assertEquals(5, summaryMap.size());
            assertEquals(summaryPayload.getName(), summaryMap.get("name"));
            assertEquals(summaryPayload.getEnrollmentType().toString(), summaryMap.get("enrollmentType"));
            assertEquals(summaryPayload.getStartDate(), summaryMap.get("startDate"));
            assertEquals(summaryPayload.getEndDate(), summaryMap.get("endDate"));
            assertEquals(summaryPayload.getCohortId().toString(), summaryMap.get("cohortId"));
            Map settingsMap = (Map) responseMap.get("settings");
            assertEquals(4, settingsMap.size());
            assertEquals(settingsPayload.getBannerImage(), settingsMap.get("bannerImage"));
            assertEquals(settingsPayload.getBannerPattern(), settingsMap.get("bannerPattern"));
            assertEquals(settingsPayload.getColor(), settingsMap.get("color"));
            assertEquals(settingsPayload.getProductId(), settingsMap.get("productId"));
            verify(cohortService, never()).saveLTIConsumerKey(any(CohortSummary.class), anyString(), anyString());
        }));
    }

    @Test
    void handle_withSettings() throws IOException {
        String startDate = "Fri, 17 Aug 2018 00:00:00 GMT";
        String endDate = "Sat, 17 Aug 2019 00:00:00 GMT";
        String name = "cohort name";

        CreateCohortMessage message = new CreateCohortMessage()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setName(name)
                .setEnrollmentType(EnrollmentType.OPEN)
                .setWorkspaceId(workspaceId);

        CohortSummary summary = new CohortSummary()
                .setId(UUID.randomUUID())
                .setType(EnrollmentType.OPEN);

        CohortSummaryPayload summaryPayload = new CohortSummaryPayload()
                .setName("CohortName")
                .setEnrollmentType(EnrollmentType.OPEN)
                .setStartDate("startDate")
                .setEndDate("endDate")
                .setCohortId(UUID.randomUUID());

        CohortSettingsPayload settingsPayload = new CohortSettingsPayload();

        CohortPayload cohortPayload = new CohortPayload()
                .setSettingsPayload(settingsPayload)
                .setSummaryPayload(summaryPayload);

        when(cohortService.createCohort(eq(accountID), eq(workspaceId), eq(name), eq(EnrollmentType.OPEN), any(), any(), any()))
                .thenReturn(Mono.just(summary));
        when(cohortService.createSettings(any(UUID.class),
                                          any(),
                                          any(),
                                          any(),
                                          any()))
                .thenReturn(Mono.just(new CohortSettings()));
        when(cohortService.getCohortPayload(any())).thenReturn(Mono.just(cohortPayload));

        createCohortMessageHandler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("workspace.cohort.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("cohort"));
            assertEquals(2, responseMap.size());
            Map summaryMap = (Map) responseMap.get("summary");
            assertEquals(5, summaryMap.size());
            assertEquals(summaryPayload.getName(), summaryMap.get("name"));
            assertEquals(summaryPayload.getEnrollmentType().toString(), summaryMap.get("enrollmentType"));
            assertEquals(summaryPayload.getStartDate(), summaryMap.get("startDate"));
            assertEquals(summaryPayload.getEndDate(), summaryMap.get("endDate"));
            assertEquals(summaryPayload.getCohortId().toString(), summaryMap.get("cohortId"));
            Map settingsMap = (Map) responseMap.get("settings");
            assertEquals(0, settingsMap.size());
            verify(cohortService, never()).saveLTIConsumerKey(any(CohortSummary.class), anyString(), anyString());
        }));
    }

    @Test
    void validate_invalidStartDate() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getStartDate()).thenReturn("invalid");

        DateTimeFault e = assertThrows(DateTimeFault.class, () -> createCohortMessageHandler.validate(message));
        assertNotNull(e);
        assertEquals(e.getMessage(),"Invalid startDate");
    }

    @Test
    void validate_invalidEndDate() throws IOException {
        CreateCohortMessage message = mock(CreateCohortMessage.class);
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getEndDate()).thenReturn("invalid");

        DateTimeFault e = assertThrows(DateTimeFault.class, () -> createCohortMessageHandler.validate(message));
        assertNotNull(e);
        assertEquals(e.getMessage(),"Invalid endDate");
    }

    @Test
    void validate_LtiEnrollmentType_noConsumerCredential() {
        CreateCohortMessage message = mock(CreateCohortMessage.class);

        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn("name");

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("LTI consumer credential object is required", t.getErrorMessage());
    }

    @Test
    void validate_LtiEnrollmentType_consumerCredential_noKey() {
        LtiConsumerCredential ltiConsumerCredential = mock(LtiConsumerCredential.class);
        CreateCohortMessage message = mock(CreateCohortMessage.class);

        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn("name");

        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);
        when(message.getLtiConsumerCredential().getKey()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("consumer key is required", t.getErrorMessage());
    }

    @Test
    void validate_LtiEnrollmentType_consumerCredential_noSecret() {
        LtiConsumerCredential ltiConsumerCredential = mock(LtiConsumerCredential.class);
        CreateCohortMessage message = mock(CreateCohortMessage.class);

        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn("name");

        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);
        when(message.getLtiConsumerCredential().getKey()).thenReturn("key");
        when(message.getLtiConsumerCredential().getSecret()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> createCohortMessageHandler.validate(message));

        assertEquals("consumer secret is required", t.getErrorMessage());
    }

    @Test
    void validate_LtiEnrollmentType_consumerCredential_success() throws RTMValidationException {
        LtiConsumerCredential ltiConsumerCredential = mock(LtiConsumerCredential.class);
        CreateCohortMessage message = mock(CreateCohortMessage.class);

        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn("name");

        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);
        when(message.getLtiConsumerCredential().getKey()).thenReturn("key");
        when(message.getLtiConsumerCredential().getSecret()).thenReturn("secret");

        createCohortMessageHandler.validate(message);
    }

    @Test
    void handle_LtiEnrollmentType_success() throws IOException {
        String bannerImage = "bannerImage";
        String bannerPattern = "bannerPattern";
        String color = "color";
        String startDate = "Fri, 17 Aug 2018 00:00:00 GMT";
        String endDate = "Sat, 17 Aug 2019 00:00:00 GMT";
        String name = "Lti cohort name";
        String productId = "A103000103955";

        LtiConsumerCredential ltiConsumerCredential = new LtiConsumerCredential()
                .setKey("ltiKey")
                .setSecret("ltiSecret");

        CreateCohortMessage message = new CreateCohortMessage()
                .setBannerImage(bannerImage)
                .setBannerPattern(bannerPattern)
                .setColor(color)
                .setEndDate(endDate)
                .setEnrollmentType(EnrollmentType.LTI)
                .setName(name)
                .setStartDate(startDate)
                .setWorkspaceId(workspaceId)
                .setProductId(productId)
                .setLtiConsumerCredential(ltiConsumerCredential);

        CohortSummary summary = new CohortSummary()
                .setId(UUID.randomUUID())
                .setType(EnrollmentType.LTI);

        CohortSummaryPayload summaryPayload = new CohortSummaryPayload()
                .setName("CohortName")
                .setEnrollmentType(EnrollmentType.LTI)
                .setStartDate("startDate")
                .setEndDate("endDate")
                .setCohortId(UUID.randomUUID());

        CohortSettingsPayload settingsPayload = new CohortSettingsPayload()
                .setBannerImage("bannerImage")
                .setBannerPattern("bannerPattern")
                .setColor("color")
                .setProductId(productId)
                .setLtiConsumerCredentials(Arrays.asList(ltiConsumerCredential));

        CohortPayload cohortPayload = new CohortPayload()
                .setSettingsPayload(settingsPayload)
                .setSummaryPayload(summaryPayload);

        when(cohortService.createCohort(eq(accountID), eq(workspaceId), eq(name), eq(EnrollmentType.LTI), any(), any(), any()))
                .thenReturn(Mono.just(summary));
        when(cohortService.createSettings(any(UUID.class),
                eq(bannerPattern),
                eq(color),
                eq(bannerImage),
                eq(productId)))
                .thenReturn(Mono.empty());
        when(cohortService.saveLTIConsumerKey(any(CohortSummary.class), anyString(), anyString()))
                .thenReturn(Mono.just(summary));
        when(cohortService.getCohortPayload(any())).thenReturn(Mono.just(cohortPayload));
        when(ltiConfig.getKey()).thenReturn("ltiKey");
        when(ltiConfig.getSecret()).thenReturn("ltiSecret");

        createCohortMessageHandler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("workspace.cohort.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("cohort"));
            assertEquals(2, responseMap.size());
            Map summaryMap = (Map) responseMap.get("summary");
            assertEquals(5, summaryMap.size());
            assertEquals(summaryPayload.getName(), summaryMap.get("name"));
            assertEquals(summaryPayload.getEnrollmentType().toString(), summaryMap.get("enrollmentType"));
            assertEquals(summaryPayload.getStartDate(), summaryMap.get("startDate"));
            assertEquals(summaryPayload.getEndDate(), summaryMap.get("endDate"));
            assertEquals(summaryPayload.getCohortId().toString(), summaryMap.get("cohortId"));
            Map settingsMap = (Map) responseMap.get("settings");
            assertEquals(5, settingsMap.size());
            assertEquals(settingsPayload.getBannerImage(), settingsMap.get("bannerImage"));
            assertEquals(settingsPayload.getBannerPattern(), settingsMap.get("bannerPattern"));
            assertEquals(settingsPayload.getColor(), settingsMap.get("color"));

            List responseLtiConsumerCredentialList = (List) settingsMap.get("ltiConsumerCredentials");
            HashMap<String, String> ltiConsumerCredentialMap = (HashMap<String, String>) responseLtiConsumerCredentialList.get(0);

            assertEquals(ltiConsumerCredential.getKey(), ltiConsumerCredentialMap.get("key"));
            assertEquals(ltiConsumerCredential.getSecret(), ltiConsumerCredentialMap.get("secret"));
            verify(cohortService).saveLTIConsumerKey(any(CohortSummary.class), eq("ltiKey"), eq("ltiSecret"));
        }));
    }
}
