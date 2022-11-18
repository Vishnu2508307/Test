package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.ListAccountSummaryMessageHandler.AUTHOR_ACCOUNT_SUMMARY_LIST_OK;
import static com.smartsparrow.rtm.message.handler.courseware.ListAccountSummaryMessageHandler.AUTHOR_ACCOUNT_SUMMARY_LIST_ERROR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.service.AccountIdentityService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.ListAccountSummaryMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ListAccountSummaryMessageHandlerTest {

    private Session session;

    @InjectMocks
    ListAccountSummaryMessageHandler handler;

    @Mock
    AccountIdentityService accountInformationService;

    @Mock
    private ListAccountSummaryMessage message;

    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.getAccountIds()).thenReturn(Lists.newArrayList(UUID.randomUUID()));

        session = mockSession();
        handler = new ListAccountSummaryMessageHandler(accountInformationService);
    }

    @Test
    void validate_noAccountIds() {
        when(message.getAccountIds()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing accountIds", ex.getMessage());
    }

    @Test
    void validate_AccountIdsEmpty() {
        when(message.getAccountIds()).thenReturn(new ArrayList<>());
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("at least 1 element in accountIds is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(accountInformationService.fetchAccountSummaryPayload(any()))
                .thenReturn(Flux.just(new AccountSummaryPayload()
                .setAccountId(accountId)
                .setAvatarSmall("avatar")
                .setFamilyName("Joe")));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ACCOUNT_SUMMARY_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("accountSummaryPayloads"));
                assertEquals(accountId.toString(), ((LinkedHashMap)responseList.get(0)).get("accountId"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<AccountSummaryPayload> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(accountInformationService.fetchAccountSummaryPayload(any()))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ACCOUNT_SUMMARY_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch account summary information\"}");
    }
}
