package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.payload.CohortSettingsPayload;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;

import reactor.core.publisher.Mono;

class GetCohortMessageHandlerTest {

    @InjectMocks
    private GetCohortMessageHandler getCohortMessageHandler;
    @Mock
    private CohortService cohortService;
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void validate_noCohortId() {
        CohortGenericMessage message = mock(CohortGenericMessage.class);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> getCohortMessageHandler.validate(message));
        assertEquals("cohortId is required", ex.getErrorMessage());
        assertEquals(400, ex.getStatusCode());
        assertEquals("workspace.cohort.get.error", ex.getType());
    }

    @Test
    void handle_noCohort() throws WriteResponseException {
        UUID cohortId = UUID.randomUUID();
        CohortGenericMessage message = mock(CohortGenericMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);
        when(cohortService.getCohortPayload(any())).thenReturn(Mono.empty());

        getCohortMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"workspace.cohort.get.error\",\"code\":404," +
                "\"message\":\"Cohort with id '" + cohortId + "' not found\"}");
    }

    @Test
    void handle_success() throws IOException {
        UUID cohortId = UUID.randomUUID();
        CohortGenericMessage message = mock(CohortGenericMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);
        CohortPayload payload = new CohortPayload()
                .setSummaryPayload(
                        new CohortSummaryPayload()
                                .setCohortId(cohortId)
                                .setName("Cohort Name")
                                .setEnrollmentType(EnrollmentType.MANUAL)
                                .setStartDate("Thu, 16 Aug 2018 00:00:00 GMT")
                                .setEndDate("Thu, 23 Aug 2018 00:00:00 GMT")
                                .setFinishedDate("Thu, 23 Aug 2020 00:00:00 GMT"))
                .setSettingsPayload(
                        new CohortSettingsPayload()
                                .setBannerImage("Banner Image")
                                .setBannerPattern("Banner Pattern")
                                .setColor("color")
                                .setProductId("A103000103955")
                );
        when(cohortService.getCohortPayload(eq(cohortId))).thenReturn(Mono.just(payload));

        getCohortMessageHandler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.cohort.get.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("cohort"));
                assertEquals(2, responseMap.size());
                Map summaryMap = (Map) responseMap.get("summary");
                assertEquals(6, summaryMap.size());
                assertEquals(cohortId.toString(), summaryMap.get("cohortId"));
                assertEquals("Cohort Name", summaryMap.get("name"));
                assertEquals("MANUAL", summaryMap.get("enrollmentType"));
                assertEquals("Thu, 16 Aug 2018 00:00:00 GMT", summaryMap.get("startDate"));
                assertEquals("Thu, 23 Aug 2018 00:00:00 GMT", summaryMap.get("endDate"));
                assertEquals("Thu, 23 Aug 2020 00:00:00 GMT", summaryMap.get("finishedDate"));
                Map settingsMap = (Map) responseMap.get("settings");
                assertEquals(4, settingsMap.size());
                assertEquals("Banner Image", settingsMap.get("bannerImage"));
                assertEquals("Banner Pattern", settingsMap.get("bannerPattern"));
                assertEquals("color", settingsMap.get("color"));
                assertEquals("A103000103955", settingsMap.get("productId"));
            });
        });
    }
}
