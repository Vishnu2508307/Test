package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.iam.AddShadowAttributeMessageHandler.IAM_SHADOW_ATTRIBUTE_ADD_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.AccountShadowAttributeSource;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.AccountShadowAttributeMessage;

import reactor.core.publisher.Flux;

class AddShadowAttributeMessageHandlerTest {


    @Mock
    private Provider<MutableAuthenticationContext> authenticationContextProvider;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AddShadowAttributeMessageHandler handler;

    private Session session;
    private AccountShadowAttributeMessage accountShadowAttributeMessage;
    private UUID accountId;
    private Account target;
    private static final UUID subscriptionId = UUID.fromString("61a11563-1631-4df4-bd5a-b3c68ea466f5");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        accountId = UUID.randomUUID();
        target = new Account().setSubscriptionId(subscriptionId).setId(accountId);
        target.setRoles((new HashSet<>()));
        accountShadowAttributeMessage = mockValid(accountId);
    }

    @Test
    void validate_noAccountShadowAttributeName() {
        when(accountShadowAttributeMessage.getAccountShadowAttributeName()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(
                accountShadowAttributeMessage));
        assertEquals("missing Shadow Name", ex.getMessage());
    }

    @Test
    void validate_noAccountId() {
        when(accountShadowAttributeMessage.getAccountId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(
                accountShadowAttributeMessage));
        assertEquals("missing accountId", ex.getMessage());
    }

    @Test
    void validate_noValue() {
        when(accountShadowAttributeMessage.getValue()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(
                accountShadowAttributeMessage));
        assertEquals("missing value", ex.getMessage());
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(accountService.findById(accountShadowAttributeMessage.getAccountId())).thenReturn(Flux.just(target));
        when(accountService.addShadowAttribute(accountShadowAttributeMessage.getAccountId(), accountShadowAttributeMessage.getAccountShadowAttributeName(), accountShadowAttributeMessage.getValue(), AccountShadowAttributeSource.SYSTEM)).thenReturn(Flux.empty());
        handler.handle(session, accountShadowAttributeMessage);
        verify(accountService).findById(accountShadowAttributeMessage.getAccountId());
        verify(accountService).addShadowAttribute(accountShadowAttributeMessage.getAccountId(), accountShadowAttributeMessage.getAccountShadowAttributeName(), accountShadowAttributeMessage.getValue(), AccountShadowAttributeSource.SYSTEM);
        verifySentMessage(session, "{\"type\":\"" + IAM_SHADOW_ATTRIBUTE_ADD_OK + "\"}");
    }
    @Test
    void validate_accountNotFound() {
        when(accountService.findById(accountId)).thenReturn(Flux.empty());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(
                accountShadowAttributeMessage));

        assertEquals("iam.shadow.attribute.add.error", t.getType());
        assertEquals(String.format("account %s not found", accountId), t.getErrorMessage());
    }



    private AccountShadowAttributeMessage mockValid(UUID accountId) {
        AccountShadowAttributeMessage editRoleMessage = mock(AccountShadowAttributeMessage.class);
        when(editRoleMessage.getAccountId()).thenReturn(accountId);
        when(editRoleMessage.getAccountShadowAttributeName()).thenReturn(AccountShadowAttributeName.ID_ZOHO);
        when(editRoleMessage.getValue()).thenReturn("value");
        return editRoleMessage;
    }

    private AccountShadowAttributeMessage mockInvalid() {
        AccountShadowAttributeMessage editRoleMessage = mock(AccountShadowAttributeMessage.class);
        when(editRoleMessage.getAccountId()).thenReturn(null);
        when(editRoleMessage.getAccountShadowAttributeName()).thenReturn(null);
        when(editRoleMessage.getValue()).thenReturn(null);
        return editRoleMessage;
    }
}
