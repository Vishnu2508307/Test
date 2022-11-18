package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import com.smartsparrow.iam.service.AuthenticationType;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;

import com.google.common.collect.Lists;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.iam.AccountProvisionMessage;

import reactor.core.publisher.Flux;

class AccountProvisionMessageHandlerTest {

    @InjectMocks
    private AccountProvisionMessageHandler handler;

    @Mock
    private AccountService accountService;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    private Session session;

    private static final String prefix = "Mr";
    private static final String name = "Name";
    private static final String surname = "Surname";
    private static final String suffix = "Jr";
    private static final String validEmail = "email@dev.com";
    private static final String invalidEmail = "emaildev.com";
    private static final String password = "pass";
    private static final String affiliation = "institute";
    private static final String jobTitle = "job title";
    private static final String authenticationType = AuthenticationType.BRONTE.toString();
    private static final String accountId = "f7313c20-15d0-11e8-b207-3da58e2e88c5";
    private static final String subscriptionId = "f72c3310-15d0-11e8-b207-3da58e2e88c5";
    private AccountAdapter accountAdapter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        accountAdapter = mock(AccountAdapter.class);
        Account account = mock(Account.class);

        when(accountAdapter.getAccount()).thenReturn(account);
        when(account.getSubscriptionId()).thenReturn(UUID.fromString(subscriptionId));
        when(account.getId()).thenReturn(UUID.fromString(accountId));
        when(account.getIamRegion()).thenReturn(Region.GLOBAL);
    }

    @Test
    void handle_success() throws Exception {
        AccountProvisionMessage message = mock(AccountProvisionMessage.class);
        when(message.getHonorificPrefix()).thenReturn(prefix);
        when(message.getGivenName()).thenReturn(name);
        when(message.getFamilyName()).thenReturn(surname);
        when(message.getHonorificSuffix()).thenReturn(suffix);
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn(password);
        when(message.getAffiliation()).thenReturn(affiliation);
        when(message.getJobTitle()).thenReturn(jobTitle);
        when(message.getType()).thenReturn(AccountProvisionMessageHandler.IAM_STUDENT_PROVISION);

        AccountIdentityAttributes identity = new AccountIdentityAttributes();
        identity.setAccountId(UUID.fromString(accountId));
        identity.setHonorificPrefix(prefix);
        identity.setGivenName(name);
        identity.setFamilyName(surname);
        identity.setHonorificSuffix(suffix);
        identity.setEmail(Sets.newSet(validEmail));
        identity.setPrimaryEmail(validEmail);
        identity.setSubscriptionId(UUID.fromString(subscriptionId));
        identity.setAffiliation(affiliation);
        identity.setJobTitle(jobTitle);
        when(accountAdapter.getIdentityAttributes()).thenReturn(identity);

        when(accountService.provision(eq(AccountProvisionSource.RTM), eq(prefix), eq(name), eq(surname), eq(suffix),
                eq(validEmail), eq(password), eq(affiliation), eq(jobTitle), eq(false), any())).thenReturn(accountAdapter);

        when(subscriptionPermissionService.saveAccountPermission(any(UUID.class), any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("iam.student.provision.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("account"));
                assertEquals(12, responseMap.size());
                assertEquals(accountId, responseMap.get("accountId"));
                assertEquals(subscriptionId, responseMap.get("subscriptionId"));
                assertEquals(Region.GLOBAL.name(), responseMap.get("iamRegion"));
                assertEquals(prefix, responseMap.get("honorificPrefix"));
                assertEquals(name, responseMap.get("givenName"));
                assertEquals(surname, responseMap.get("familyName"));
                assertEquals(suffix, responseMap.get("honorificSuffix"));
                assertEquals(Lists.newArrayList(validEmail), responseMap.get("email"));
                assertEquals(validEmail, responseMap.get("primaryEmail"));
                assertEquals(affiliation, responseMap.get("affiliation"));
                assertEquals(jobTitle, responseMap.get("jobTitle"));
                assertEquals(authenticationType, responseMap.get("authenticationType"));
            });
        });
    }

    @Test
    void handle_instructor() throws Exception {
        AccountProvisionMessage message = mock(AccountProvisionMessage.class);
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn(password);
        when(message.getType()).thenReturn(AccountProvisionMessageHandler.IAM_INSTRUCTOR_PROVISION);

        AccountIdentityAttributes identity = new AccountIdentityAttributes();
        identity.setAccountId(UUID.fromString(accountId));
        identity.setEmail(Sets.newSet(validEmail));
        identity.setPrimaryEmail(validEmail);
        identity.setSubscriptionId(UUID.fromString(subscriptionId));
        identity.setIamRegion(Region.GLOBAL);
        when(accountAdapter.getIdentityAttributes()).thenReturn(identity);

        when(accountService.provision(eq(AccountProvisionSource.RTM), eq(null), eq(null), eq(null), eq(null),
                eq(validEmail), eq(password), eq(null), eq(null), eq(true), any())).thenReturn(accountAdapter);

        when(subscriptionPermissionService.saveAccountPermission(any(UUID.class), any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Flux.just(new Void[]{}));
        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("iam.instructor.provision.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("account"));
                assertEquals(6, responseMap.size());
                assertEquals(accountId, responseMap.get("accountId"));
                assertEquals(subscriptionId, responseMap.get("subscriptionId"));
                assertEquals(Region.GLOBAL.name(), responseMap.get("iamRegion"));
                assertEquals(Lists.newArrayList(validEmail), responseMap.get("email"));
                assertEquals(validEmail, responseMap.get("primaryEmail"));
                assertEquals(authenticationType, responseMap.get("authenticationType"));
            });
        });
    }

    @Test
    void handle_existingUser() throws Exception {
        AccountProvisionMessage message = mock(AccountProvisionMessage.class);
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn(password);
        when(message.getType()).thenReturn(AccountProvisionMessageHandler.IAM_INSTRUCTOR_PROVISION);
        when(accountService.provision(eq(AccountProvisionSource.RTM), any(), any(), any(), any(),
                any(), any(), any(), any(), anyBoolean(), any())).thenThrow(ConflictException.class);

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"iam.instructor.provision.error\",\"code\":409," +
                "\"response\":{\"reason\":\"Unable to create user: email already in use\"}}");
    }

    @Test
    void validate_emptyEmail() {
        AccountProvisionMessage message = mock(AccountProvisionMessage.class);
        when(message.getEmail()).thenReturn("");

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));
        assertEquals("`email` and `password` are required", ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_emptyPassword() {
        AccountProvisionMessage message = mock(AccountProvisionMessage.class);
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("");

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));
        assertEquals("`email` and `password` are required", ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_invalidEmail() {
        AccountProvisionMessage message = mock(AccountProvisionMessage.class);
        when(message.getEmail()).thenReturn(invalidEmail);
        when(message.getPassword()).thenReturn(password);

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));
        assertEquals("Invalid email supplied", ((RTMValidationException) t).getErrorMessage());
    }
}
