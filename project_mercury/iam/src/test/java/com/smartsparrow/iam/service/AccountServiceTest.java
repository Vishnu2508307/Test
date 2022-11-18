package com.smartsparrow.iam.service;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.AccountAvatarGateway;
import com.smartsparrow.iam.data.AccountGateway;
import com.smartsparrow.iam.data.CredentialGateway;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.Passwords;

import blackboard.blti.message.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountGateway accountGateway;
    @Mock
    private AccountAvatarGateway avatarGateway;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private CredentialGateway credentialGateway;

    //
    private static final UUID validSubscriptionId = UUIDs.timeBased();
    private static final UUID invalidSubscriptionId = UUIDs.timeBased();
    private static final UUID validAccountId = UUIDs.timeBased();
    private static final UUID invalidAccountId = UUIDs.timeBased();
    private static final Long validAccountHistoricalId = 19L;
    private static final Long invalidAccountHistoricalId = 44L;
    private static final Region subscriptionRegion = Region.GLOBAL;
    private blackboard.blti.message.User ltiUser;
    private Account dbAccount;
    private AccountIdentityAttributes dbAccountIdentity;

    //
    private static final Long historicalId = 765L;
    private Set<AccountRole> roles = ImmutableSet.of(AccountRole.INSTRUCTOR, AccountRole.STUDENT);
    private Set<AccountRole> replacementRoles = ImmutableSet.of(AccountRole.STUDENT_GUEST);
    private static final String honorificPrefix = "Capt.";
    private static final String givenName = "Jean-Luc";
    private static final String familyName = "Picard";
    private static final String honorificSuffix = null;
    private static final String emailAddress = "picard@starfleet.tld";
    private static final String emailAddressInUpperCase = "PICARD@starfleet.tld";
    private static final String emailAddressHash = Hashing.email(emailAddress);
    private static final String clearTextPassword = "Alpha-Alpha-3-0-5";
    private static final Boolean passwordExpired = Boolean.FALSE;
    private static final String passwordHash = Passwords.hash(clearTextPassword);
    private static final String passwordTemporary = "picard@5PR";
    private static final String affiliation = "Federation Starfleet";
    private static final String jobTitle = "Captain USS Enterprise";
    private static final String signupSubjectArea = "space";
    private static final String signupGoal = "explore";
    private static final String signupOffering = "none";
    private static final String replacementEmail = "queen@borg.tld";
    private static final String secondaryEmail = "kirk@starfleet.tld";

    @BeforeEach
    public void setup() {
        // create any @Mock
        MockitoAnnotations.initMocks(this);

        // make happy cases.

        Subscription subscription = new Subscription().setId(validSubscriptionId)
                .setIamRegion(subscriptionRegion)
                .setName("test");

        when(subscriptionService.find(validSubscriptionId)).thenReturn(Flux.just(subscription));
        when(subscriptionService.find(invalidSubscriptionId)).thenReturn(Flux.empty());

        dbAccount = new Account()
                //
                .setId(validAccountId)
                .setIamRegion(subscription.getIamRegion())
                .setSubscriptionId(validSubscriptionId)
                .setRoles(roles)
                .setPasswordHash(passwordHash)
                .setStatus(AccountStatus.ENABLED)
                .setHistoricalId(historicalId);
        //
        dbAccountIdentity = new AccountIdentityAttributes().setAccountId(validAccountId)
                .setIamRegion(subscription.getIamRegion())
                .setSubscriptionId(subscription.getId())
                .setHonorificPrefix(honorificPrefix)
                .setGivenName(givenName)
                .setFamilyName(familyName)
                .setHonorificSuffix(honorificSuffix)
                .setPrimaryEmail(emailAddress)
                .setEmail(ImmutableSet.of(emailAddress))
                .setAffiliation(affiliation)
                .setJobTitle(jobTitle);
        //
        ltiUser = new blackboard.blti.message.User();
        ltiUser.setEmail(emailAddress);
        ltiUser.setGivenName(givenName);
        ltiUser.setFamilyName(familyName);
        ltiUser.setFullName(givenName + " " + familyName);
        ltiUser.setRoles(ImmutableList.of(new Role(Role.INSTRUCTOR)));
        ltiUser.setId("lti-user-id");
        ltiUser.setLisSourcedId("lis-sourced-id");

        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.just(dbAccount));
        when(accountGateway.findAccountById(invalidAccountId)).thenReturn(Flux.empty());
        when(accountGateway.findAccountByHistoricalId(validAccountHistoricalId)).thenReturn(Flux.just(dbAccount));
        when(accountGateway.findAccountByHistoricalId(invalidAccountHistoricalId)).thenReturn(Flux.empty());
        when(accountGateway.findAccountIdentityById(subscriptionRegion, validAccountId)).thenReturn(
                Flux.just(dbAccountIdentity));
        when(accountGateway.findAccountIdentityById(any(), eq(invalidAccountId))).thenReturn(
                Flux.empty());
        when(accountGateway.findActionsByAccountId(any())).thenReturn(Flux.empty());
        when(avatarGateway.findAvatarInfoByAccountId(any(), any())).thenReturn(Flux.empty());
        when(accountGateway.findAccountsBySubscription(validSubscriptionId)).thenReturn(Flux.just(dbAccount));
        when(accountGateway.findAccountLogEntry(subscriptionRegion, validAccountId)).thenReturn(
                Flux.just(new AccountLogEntry()));
        when(accountGateway.findAccountShadowAttribute(subscriptionRegion, validAccountId)).thenReturn(
                Flux.just(new AccountShadowAttribute()));
        when(accountGateway.findAccountShadowAttribute(any(Region.class), any(UUID.class),
                any(AccountShadowAttributeName.class))).thenReturn(
                Flux.empty());
        when(accountGateway.findAccountShadowAttribute(eq(subscriptionRegion), eq(validAccountId),
                eq(AccountShadowAttributeName.GEO_COUNTRY_CODE))).thenReturn(
                Flux.just(new AccountShadowAttribute()));
        when(credentialGateway.persistCredentialsTypeByAccount(any())).thenReturn(Mono.empty());
    }

    // test the error handling pattern.
    @Test
    public void error_handling() {
        doThrow(new DriverException("bad")).when(accountGateway)
                .mutateStatusBlocking(any(UUID.class), any(AccountStatus.class));

        assertThrows(RuntimeException.class, () -> accountService.disable(UUIDs.timeBased(), validAccountId));
    }

    @Test
    public void provision_subscriptionId_required() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
        accountService.provision(AccountProvisionSource.SIGNUP, null, roles, honorificPrefix, givenName, familyName,
                honorificSuffix, emailAddress, clearTextPassword, passwordExpired, affiliation,
                jobTitle, AuthenticationType.BRONTE));
    }

    @Test
    public void provision_invalid_subscriptionId() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
        accountService.provision(AccountProvisionSource.SIGNUP, invalidSubscriptionId, roles, honorificPrefix,
                givenName, familyName, honorificSuffix, emailAddress, clearTextPassword,
                passwordExpired, affiliation, jobTitle, AuthenticationType.BRONTE));
    }

    @Test
    public void provision_existing_email() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(new Account()));

        assertThrows(ConflictException.class, () -> accountService.provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles, honorificPrefix, givenName,
                familyName, honorificSuffix, emailAddress, clearTextPassword, passwordExpired,
                affiliation, jobTitle, AuthenticationType.BRONTE));
    }

    @Test
    public void provision_email_upper_case() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.empty());

        accountService.provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles, honorificPrefix, givenName,
                familyName, honorificSuffix, emailAddressInUpperCase, clearTextPassword, passwordExpired,
                affiliation, jobTitle, AuthenticationType.BRONTE);

        ArgumentCaptor<AccountShadowAttribute> captor = ArgumentCaptor.forClass(AccountShadowAttribute.class);

        // verify calls (deeply to ensure field/parameter mapping)
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        ArgumentCaptor<AccountHash> ahCaptor = ArgumentCaptor.forClass(AccountHash.class);
        verify(accountGateway).persistBlocking(ahCaptor.capture());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertEquals(emailAddress, aiaActual.getPrimaryEmail());
        assertEquals(ImmutableSet.of(emailAddress), aiaActual.getEmail());

        AccountHash ahActual = ahCaptor.getValue();
        assertEquals(emailAddressHash, ahActual.getHash());

        verify(accountGateway, times(1)).persistBlocking(captor.capture());

        List<AccountShadowAttribute> attrs = captor.getAllValues();

        assertNotNull(attrs);
        assertEquals(1, attrs.size());
        AccountShadowAttribute attr = attrs.get(0);
        assertEquals(AccountShadowAttributeName.PROVISION_SOURCE, attr.getAttribute());
    }

    @Test
    void provision_email_upper_case2() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.empty());
        when(subscriptionService.create(any(), eq(Region.GLOBAL))).thenReturn(new Subscription().setId(validSubscriptionId));

        accountService.provision(AccountProvisionSource.RTM,
                null, null,null, null,
                emailAddressInUpperCase, null, null, null, false, AuthenticationType.BRONTE);

        // verify calls (deeply to ensure field/parameter mapping)
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        ArgumentCaptor<AccountHash> ahCaptor = ArgumentCaptor.forClass(AccountHash.class);
        verify(accountGateway).persistBlocking(ahCaptor.capture());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertEquals(emailAddress, aiaActual.getPrimaryEmail());
        assertEquals(ImmutableSet.of(emailAddress), aiaActual.getEmail());

        AccountHash ahActual = ahCaptor.getValue();
        assertEquals(emailAddressHash, ahActual.getHash());
    }

    @Test
    void provision_email_invalid() {
        assertThrows(IllegalArgumentException.class, () -> accountService.provision(AccountProvisionSource.RTM,
                null, null,null, null,
                "emaildev.com", null,null, null, false,  AuthenticationType.BRONTE));
    }

    @Test
    void provision_instructor() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.empty());
        when(subscriptionService.create(any(), eq(Region.GLOBAL))).thenReturn(new Subscription().setId(validSubscriptionId));

        accountService.provision(AccountProvisionSource.RTM,
                null, null,null, null, emailAddress,
                null, null, null, true,  AuthenticationType.BRONTE);

        ArgumentCaptor<Account> aCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountGateway).persistBlocking(aCaptor.capture());
        assertEquals(Sets.newHashSet(AccountRole.STUDENT, AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR, AccountRole.ADMIN, AccountRole.DEVELOPER),
                aCaptor.getValue().getRoles());
    }

    @Test
    void provision_success() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.empty());
        when(subscriptionService.create(any(), eq(Region.GLOBAL))).thenReturn(new Subscription().setId(validSubscriptionId).setIamRegion(subscriptionRegion));

        accountService.provision(AccountProvisionSource.RTM,
                honorificPrefix, givenName, familyName, honorificSuffix,
                emailAddress, clearTextPassword, affiliation, jobTitle, false, AuthenticationType.BRONTE);

        verify(subscriptionService, times(1)).create(any(), eq(Region.GLOBAL));

        // verify calls (deeply to ensure field/parameter mapping)
        ArgumentCaptor<Account> aCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountGateway).persistBlocking(aCaptor.capture());
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        ArgumentCaptor<AccountHash> ahCaptor = ArgumentCaptor.forClass(AccountHash.class);
        verify(accountGateway).persistBlocking(ahCaptor.capture());
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountShadowAttribute.class));

        Account account = aCaptor.getValue();
        assertNotNull(account.getId());
        assertEquals(subscriptionRegion, account.getIamRegion());
        assertEquals(validSubscriptionId, account.getSubscriptionId());
        assertEquals(Sets.newHashSet(AccountRole.STUDENT), account.getRoles());
        assertEquals(AccountStatus.ENABLED, account.getStatus());
        assertNull(account.getHistoricalId());
        assertNotNull(account.getPasswordHash());
        assertEquals(passwordExpired, account.getPasswordExpired());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertNotNull(aiaActual.getAccountId());
        assertEquals(subscriptionRegion, aiaActual.getIamRegion());
        assertEquals(validSubscriptionId, aiaActual.getSubscriptionId());
        assertEquals(honorificPrefix, aiaActual.getHonorificPrefix());
        assertEquals(givenName, aiaActual.getGivenName());
        assertEquals(familyName, aiaActual.getFamilyName());
        assertNull(aiaActual.getHonorificSuffix());
        assertEquals(emailAddress, aiaActual.getPrimaryEmail());
        assertEquals(affiliation, aiaActual.getAffiliation());
        assertEquals(jobTitle, aiaActual.getJobTitle());
        assertEquals(ImmutableSet.of(emailAddress), aiaActual.getEmail());

        AccountHash ahActual = ahCaptor.getValue();
        assertEquals(account.getId(), ahActual.getAccountId());
        assertEquals(subscriptionRegion, ahActual.getIamRegion());
        assertEquals(Hashing.email(emailAddress), ahActual.getHash());
    }

    @Test
    void provision_existing_email2() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(new Account()));
        when(subscriptionService.create(any(), eq(Region.GLOBAL))).thenReturn(new Subscription().setId(validSubscriptionId).setIamRegion(subscriptionRegion));

        assertThrows(ConflictException.class, () -> accountService.provision(AccountProvisionSource.RTM,
                null, null,null, null, emailAddress,
                null, null, null, false, AuthenticationType.BRONTE));
    }

    @Test
    public void provision() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        accountService.provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles,
                honorificPrefix, givenName, familyName, honorificSuffix,
                emailAddress, clearTextPassword, passwordExpired, affiliation,
                jobTitle,  AuthenticationType.BRONTE);

        // verify calls (deeply to ensure field/parameter mapping)
        ArgumentCaptor<Account> aCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountGateway).persistBlocking(aCaptor.capture());
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        ArgumentCaptor<AccountHash> ahCaptor = ArgumentCaptor.forClass(AccountHash.class);
        verify(accountGateway).persistBlocking(ahCaptor.capture());
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountShadowAttribute.class));

        Account account = aCaptor.getValue();
        assertNotNull(account.getId());
        assertEquals(subscriptionRegion, account.getIamRegion());
        assertEquals(validSubscriptionId, account.getSubscriptionId());
        assertEquals(roles, account.getRoles());
        assertEquals(AccountStatus.ENABLED, account.getStatus());
        assertNull(account.getHistoricalId());
        assertNotNull(account.getPasswordHash());
        assertEquals(passwordExpired, account.getPasswordExpired());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertNotNull(aiaActual.getAccountId());
        assertEquals(subscriptionRegion, aiaActual.getIamRegion());
        assertEquals(validSubscriptionId, aiaActual.getSubscriptionId());
        assertEquals(honorificPrefix, aiaActual.getHonorificPrefix());
        assertEquals(givenName, aiaActual.getGivenName());
        assertEquals(familyName, aiaActual.getFamilyName());
        assertNull(aiaActual.getHonorificSuffix());
        assertEquals(emailAddress, aiaActual.getPrimaryEmail());
        assertEquals(affiliation, aiaActual.getAffiliation());
        assertEquals(jobTitle, aiaActual.getJobTitle());
        assertEquals(ImmutableSet.of(emailAddress), aiaActual.getEmail());

        AccountHash ahActual = ahCaptor.getValue();
        assertEquals(account.getId(), ahActual.getAccountId());
        assertEquals(subscriptionRegion, ahActual.getIamRegion());
        assertEquals(Hashing.email(emailAddress), ahActual.getHash());
    }

    @Test
    public void provision_no_identity() throws Exception {
        AccountAdapter adapter = accountService.provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles,
                null, null, null, null, null, null, null, null, null, AuthenticationType.BRONTE);

        // verify calls.
        verify(accountGateway).persistBlocking(any(Account.class));
        verify(accountGateway).persistBlocking(any(AccountIdentityAttributes.class));
        verify(accountGateway, times(0)).persistBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));

        assertNotNull(adapter.getAccount());
        assertNull(adapter.getIdentityAttributes().getGivenName());
        assertNull(adapter.getIdentityAttributes().getFamilyName());
    }

    @Test
    public void provision_no_email_password() throws Exception {
        AccountAdapter adapter = accountService.provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles,
                honorificPrefix, givenName, familyName, honorificSuffix, null,
                null, null, affiliation, jobTitle, AuthenticationType.BRONTE);

        // verify calls.
        verify(accountGateway).persistBlocking(any(Account.class));
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        verify(accountGateway, never()).persistBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));

        assertNull(adapter.getAccount().getPasswordHash());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertNull(aiaActual.getEmail());
    }

    @Test
    public void provision_with_signup_values() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        AccountAdapter adapter = accountService.provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles,
                honorificPrefix, givenName, familyName, honorificSuffix,
                emailAddress, clearTextPassword, passwordExpired, affiliation,
                jobTitle, signupSubjectArea, signupGoal, signupOffering);

        // verify calls.
        verify(accountGateway).persistBlocking(any(Account.class));
        verify(accountGateway).persistBlocking(any(AccountIdentityAttributes.class));
        verify(accountGateway).persistBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));

        // signup values become shadow attributes.
        verify(accountGateway, atLeast(4)).persistBlocking(any(AccountShadowAttribute.class));

        assertNotNull(adapter.getAccount());
    }

    @Test
    public void provision_email_duplicate_with_signup_values() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(new Account()));

        assertThrows(ConflictException.class, () -> accountService
                .provision(AccountProvisionSource.SIGNUP, validSubscriptionId, roles, honorificPrefix, givenName,
                        familyName, honorificSuffix, emailAddress, clearTextPassword, passwordExpired, affiliation,
                        jobTitle, signupSubjectArea, signupGoal, signupOffering));
    }

    @Test
    public void provision_lti() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        AccountAdapter adapter = accountService.provision(validSubscriptionId, ltiUser, affiliation, "lms.origin");

        //
        verify(accountGateway).persistBlocking(any(Account.class));
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        verify(accountGateway).persistBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountShadowAttribute.class));

        Account account = adapter.getAccount();
        assertEquals(roles, account.getRoles());
        assertNull(account.getPasswordHash());
        assertNull(account.getPasswordExpired());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertEquals(givenName, aiaActual.getGivenName());
        assertEquals(familyName, aiaActual.getFamilyName());
        assertEquals(emailAddress, aiaActual.getPrimaryEmail());
        assertEquals(affiliation, aiaActual.getAffiliation());
        assertNull(aiaActual.getJobTitle());
        assertEquals(ImmutableSet.of(emailAddress), aiaActual.getEmail());
    }

    @Test
    public void provision_lti_fullname() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        ltiUser.setGivenName(null);
        ltiUser.setFamilyName(null);

        accountService.provision(validSubscriptionId, ltiUser, affiliation, "lms.origin");

        //
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertEquals(givenName, aiaActual.getGivenName());
        assertEquals(familyName, aiaActual.getFamilyName());
    }

    @Test
    public void provision_lti_fullname_no_spaces() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        ltiUser.setGivenName(null);
        ltiUser.setFamilyName(null);
        String glued = givenName + familyName;
        ltiUser.setFullName(glued);

        accountService.provision(validSubscriptionId, ltiUser, affiliation, "lms.origin");

        //
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertEquals(glued, aiaActual.getGivenName());
        assertNull(aiaActual.getFamilyName());
    }

    @Test
    public void provision_lti_fullname_no_email() throws Exception {
        ltiUser.setEmail(null);

        accountService.provision(validSubscriptionId, ltiUser, affiliation, "lms.origin");

        //
        ArgumentCaptor<AccountIdentityAttributes> aiaCaptor = ArgumentCaptor.forClass(AccountIdentityAttributes.class);
        verify(accountGateway).persistBlocking(aiaCaptor.capture());
        verify(accountGateway, times(0)).persistBlocking(any(AccountHash.class));

        AccountIdentityAttributes aiaActual = aiaCaptor.getValue();
        assertNull(aiaActual.getEmail());
    }

    @Test
    public void provision_lti_invalid_subscription() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                accountService.provision(invalidSubscriptionId, ltiUser, affiliation, "lms.origin"));
    }

    @Test
    public void provisionGuest() throws Exception {
        Account account = accountService.provisionGuest(AccountProvisionSource.ACTIVATION, "/some/url",
                validSubscriptionId);

        verify(accountGateway).persistBlocking(account);
        verify(accountGateway, atLeast(2)).persistBlocking(any(AccountShadowAttribute.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));

        assertEquals(Region.GLOBAL, account.getIamRegion());
        assertEquals(validSubscriptionId, account.getSubscriptionId());
        assertEquals(ImmutableSet.of(AccountRole.STUDENT_GUEST), account.getRoles());
        assertEquals(AccountStatus.ENABLED, account.getStatus());
        assertNull(account.getHistoricalId());
    }

   @Test
    public void provisionGuest_invalid_subscription() throws Exception {
       assertThrows(IllegalArgumentException.class, () -> accountService
               .provisionGuest(AccountProvisionSource.ACTIVATION, "/some/url", invalidSubscriptionId));
    }

    @Test
    public void provisionGuest_source_required() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.provisionGuest(null, "/some/url", validSubscriptionId));
    }

    @Test
    public void provisionGuest_url_required() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.provisionGuest(AccountProvisionSource.ACTIVATION, null, validSubscriptionId));
    }

    @Test
    public void provisionGuest_subscription_required() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.provisionGuest(AccountProvisionSource.ACTIVATION, "/some/url", null));
    }

    @Test
    public void setIdentityNames() {
        accountService.setIdentityNames(validAccountId, honorificPrefix.toUpperCase(), givenName.toUpperCase(),
                familyName.toUpperCase(), "B");

        verify(accountGateway).setIdentityNamesBlocking(eq(subscriptionRegion), eq(validAccountId),
                eq(honorificPrefix.toUpperCase()), eq(givenName.toUpperCase()),
                eq(familyName.toUpperCase()), eq("B"));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setIdentityNames_invalid_account() {
        assertThrows(IllegalArgumentException.class, () -> accountService
                .setIdentityNames(invalidAccountId, honorificPrefix.toUpperCase(), givenName.toUpperCase(),
                        familyName.toUpperCase(), "B"));
    }

    @Test
    public void setIdentityNames_account_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService
                .setIdentityNames(null, honorificPrefix.toUpperCase(), givenName.toUpperCase(),
                        familyName.toUpperCase(), null));
    }

    @Test
    public void disable() {
        accountService.disable(UUIDs.timeBased(), validAccountId);

        verify(accountGateway).mutateStatusBlocking(eq(validAccountId), eq(AccountStatus.DISABLED));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void disable_multi() {
        UUID two = UUIDs.timeBased();
        UUID three = UUIDs.timeBased();
        when(accountGateway.findAccountById(two)).thenReturn(Flux.just(new Account().setId(two)));
        when(accountGateway.findAccountById(three)).thenReturn(Flux.just(new Account().setId(three)));

        accountService.disable(UUIDs.timeBased(), validAccountId, invalidAccountId, two, three);

        verify(accountGateway).mutateStatusBlocking(eq(validAccountId), eq(AccountStatus.DISABLED));
        verify(accountGateway, never()).mutateStatusBlocking(eq(invalidAccountId), eq(AccountStatus.DISABLED));
        verify(accountGateway).mutateStatusBlocking(eq(two), eq(AccountStatus.DISABLED));
        verify(accountGateway).mutateStatusBlocking(eq(three), eq(AccountStatus.DISABLED));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void disable_none() {
        accountService.disable(UUIDs.timeBased(), invalidAccountId);

        verify(accountGateway, never()).mutateStatusBlocking(any(UUID.class), eq(AccountStatus.DISABLED));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void disable_callerAccountId_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.disable(null, validAccountId));
    }

    @Test
    public void disable_accountId_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.disable(null, validAccountId));
    }

    @Test
    public void enable() {
        accountService.enable(UUIDs.timeBased(), validAccountId);

        verify(accountGateway).mutateStatusBlocking(eq(validAccountId), eq(AccountStatus.ENABLED));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void enable_multi() {
        UUID two = UUIDs.timeBased();
        UUID three = UUIDs.timeBased();
        when(accountGateway.findAccountById(two)).thenReturn(Flux.just(new Account().setId(two)));
        when(accountGateway.findAccountById(three)).thenReturn(Flux.just(new Account().setId(three)));

        accountService.enable(UUIDs.timeBased(), validAccountId, invalidAccountId, two, three);

        verify(accountGateway).mutateStatusBlocking(eq(validAccountId), eq(AccountStatus.ENABLED));
        verify(accountGateway, never()).mutateStatusBlocking(eq(invalidAccountId), eq(AccountStatus.ENABLED));
        verify(accountGateway).mutateStatusBlocking(eq(two), eq(AccountStatus.ENABLED));
        verify(accountGateway).mutateStatusBlocking(eq(three), eq(AccountStatus.ENABLED));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void enabled_none() {
        accountService.enable(UUIDs.timeBased(), invalidAccountId);

        verify(accountGateway, never()).mutateStatusBlocking(any(UUID.class), eq(AccountStatus.ENABLED));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void enable_accountIds_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.enable(null, (UUID[]) null));
    }

    @Test
    public void setVerifiedEmail() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        accountService.setVerifiedEmail(validAccountId, replacementEmail);

        verify(accountGateway).removeEmailBlocking(subscriptionRegion, validAccountId, emailAddress);
        verify(accountGateway).deleteBlocking(any(AccountHash.class));
        verify(accountGateway).addEmailBlocking(subscriptionRegion, validAccountId, replacementEmail);
        verify(accountGateway).persistBlocking(any(AccountHash.class));
        verify(accountGateway).setPrimaryEmailBlocking(subscriptionRegion, validAccountId, replacementEmail);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setVerifiedEmail_with_multiple() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        dbAccountIdentity.setEmail(ImmutableSet.of(emailAddress, secondaryEmail));

        accountService.setVerifiedEmail(validAccountId, replacementEmail);

        verify(accountGateway).removeEmailBlocking(subscriptionRegion, validAccountId, emailAddress);
        verify(accountGateway).removeEmailBlocking(subscriptionRegion, validAccountId, secondaryEmail);
        verify(accountGateway, times(2)).deleteBlocking(any(AccountHash.class));
        verify(accountGateway).addEmailBlocking(subscriptionRegion, validAccountId, replacementEmail);
        verify(accountGateway).persistBlocking(any(AccountHash.class));
        verify(accountGateway).setPrimaryEmailBlocking(subscriptionRegion, validAccountId, replacementEmail);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setVerifiedEmail_email_duplicate() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(new Account()));

        assertThrows(ConflictException.class, () -> accountService.setVerifiedEmail(validAccountId, emailAddress));
    }

    @Test
    public void setVerifiedEmail_invalid_account() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setVerifiedEmail(invalidAccountId, emailAddress));
    }

    @Test
    public void setVerifiedEmail_accountId_required() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.setVerifiedEmail(null, emailAddress));
    }

    @Test
    public void setVerifiedEmail_email_required() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.setVerifiedEmail(validAccountId, null));
    }

    @Test
    public void setVerifiedEmail_email_required_empty() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.setVerifiedEmail(validAccountId, ""));
    }

    @Test
    public void addVerifiedEmail() throws Exception {
        when(accountGateway.findAccountByHash(anyString())).thenReturn(Flux.empty());

        accountService.addVerifiedEmail(validAccountId, secondaryEmail);

        verify(accountGateway).addEmailBlocking(subscriptionRegion, validAccountId, secondaryEmail);
        verify(accountGateway).persistBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void addVerifiedEmail_email_required() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.addVerifiedEmail(validAccountId, null));
    }

    @Test
    public void addVerifiedEmail_accountId_required() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.addVerifiedEmail(null, secondaryEmail));
    }

    @Test
    public void addVerifiedEmail_email_duplicate() throws Exception {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(new Account()));

        assertThrows(ConflictException.class, () -> accountService.addVerifiedEmail(validAccountId, emailAddress));
    }

    @Test
    public void addVerifiedEmail_accountId_invalid() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addVerifiedEmail(invalidAccountId, emailAddress));
    }

    @Test
    public void removeEmail() {
        dbAccountIdentity.setEmail(ImmutableSet.of(emailAddress, secondaryEmail));

        accountService.removeEmail(validAccountId, secondaryEmail);

        verify(accountGateway).removeEmailBlocking(subscriptionRegion, validAccountId, secondaryEmail);
        verify(accountGateway).deleteBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeEmail_primary() {
        accountService.removeEmail(validAccountId, emailAddress);

        verify(accountGateway).setPrimaryEmailBlocking(subscriptionRegion, validAccountId, null);
        verify(accountGateway).removeEmailBlocking(subscriptionRegion, validAccountId, emailAddress);
        verify(accountGateway).deleteBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeEmail_case_insensitive() {
        dbAccountIdentity.setEmail(ImmutableSet.of(emailAddress, secondaryEmail));

        accountService.removeEmail(validAccountId, secondaryEmail.toUpperCase());

        verify(accountGateway).removeEmailBlocking(subscriptionRegion, validAccountId, secondaryEmail);
        verify(accountGateway).deleteBlocking(any(AccountHash.class));
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeEmail_not_in_system() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.removeEmail(validAccountId, replacementEmail));

        verify(accountGateway, never()).removeEmailBlocking(any(Region.class), any(UUID.class), any(String.class));
        verify(accountGateway, never()).deleteBlocking(any(AccountHash.class));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeEmail_not_on_account() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.removeEmail(validAccountId, replacementEmail));

        verify(accountGateway, never()).removeEmailBlocking(any(Region.class), any(UUID.class), any(String.class));
        verify(accountGateway, never()).deleteBlocking(any(AccountHash.class));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeEmail_accountId_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.removeEmail(null, emailAddress));
    }

    @Test
    public void removeEmail_email_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.removeEmail(validAccountId, null));
    }

    @Test
    public void removeEmail_accountId_invalid() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.removeEmail(invalidAccountId, emailAddress));
    }

    @Test
    public void setPrimaryEmail() {
        dbAccountIdentity.setPrimaryEmail(emailAddress);
        dbAccountIdentity.setEmail(ImmutableSet.of(emailAddress, secondaryEmail));

        accountService.setPrimaryEmail(validAccountId, secondaryEmail);

        verify(accountGateway).setPrimaryEmailBlocking(subscriptionRegion, validAccountId, secondaryEmail);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPrimaryEmail_not_in_account() {
        dbAccountIdentity.setPrimaryEmail(emailAddress);
        dbAccountIdentity.setEmail(ImmutableSet.of(emailAddress, secondaryEmail));

        assertThrows(IllegalArgumentException.class, () -> accountService.setPrimaryEmail(validAccountId, replacementEmail));
    }

    @Test
    public void setPrimaryEmail_not_case_sensitive() {
        dbAccountIdentity.setPrimaryEmail(emailAddress);
        dbAccountIdentity.setEmail(ImmutableSet.of(emailAddress, secondaryEmail));

        accountService.setPrimaryEmail(validAccountId, secondaryEmail.toUpperCase());
    }

    @Test
    public void setPrimaryEmail_account_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setPrimaryEmail(null, emailAddress));
    }

    @Test
    public void setPrimaryEmail_email_can_be_null() {
        accountService.setPrimaryEmail(validAccountId, null);

        verify(accountGateway).setPrimaryEmailBlocking(subscriptionRegion, validAccountId, null);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPrimaryEmail_account_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setPrimaryEmail(invalidAccountId, emailAddress));
    }

    @Test
    public void setRoles() {
        accountService.setRoles(UUIDs.timeBased(), replacementRoles, validAccountId);

        verify(accountGateway).setRolesBlocking(validAccountId, replacementRoles);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setRoles_ignore_invalid() {
        accountService.setRoles(UUIDs.timeBased(), replacementRoles, invalidAccountId);

        verify(accountGateway, never()).setRolesBlocking(any(UUID.class), anySet());
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setRoles_caller_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setRoles(null, replacementRoles, validAccountId));
    }

    @Test
    public void setRoles_roles_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setRoles(UUIDs.timeBased(), null, validAccountId));
    }

    @Test
    public void setRoles_roles_empty_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setRoles(UUIDs.timeBased(), ImmutableSet.of(), validAccountId));

        verify(accountGateway, never()).setRolesBlocking(any(UUID.class), anySet());
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setRoles_account_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setRoles(UUIDs.timeBased(), replacementRoles, (UUID[]) null));
    }

    @Test
    public void addRole() {
        accountService.addRole(UUIDs.timeBased(), AccountRole.STUDENT_GUEST, validAccountId);

        verify(accountGateway).addRoleBlocking(validAccountId, AccountRole.STUDENT_GUEST);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void addRole_ignore_invalid() {
        accountService.addRole(UUIDs.timeBased(), AccountRole.STUDENT_GUEST, invalidAccountId);

        verify(accountGateway, never()).addRoleBlocking(any(UUID.class), any(AccountRole.class));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void addRole_role_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.addRole(null, null, validAccountId));
    }

    @Test
    public void addRole_account_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addRole(null, AccountRole.STUDENT_GUEST, (UUID[]) null));
    }

    @Test
    public void removeRole() {
        accountService.removeRole(UUIDs.timeBased(), AccountRole.STUDENT_GUEST, validAccountId);

        verify(accountGateway).removeRoleBlocking(validAccountId, AccountRole.STUDENT_GUEST);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeRole_invalid_account_ignored() {
        accountService.removeRole(UUIDs.timeBased(), AccountRole.STUDENT_GUEST, invalidAccountId);

        verify(accountGateway, never()).removeRoleBlocking(any(UUID.class), any(AccountRole.class));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void removeRole_caller_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.removeRole(null, AccountRole.STUDENT_GUEST, validAccountId));
    }

    @Test
    public void removeRole_role_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.removeRole(UUIDs.timeBased(), null, validAccountId));
    }

    @Test
    public void removeRole_account_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.removeRole(UUIDs.timeBased(), AccountRole.STUDENT_GUEST, (UUID[]) null));
    }

    @Test
    public void addAction() {
        accountService.addAction(validAccountId, AccountAction.Action.OPENED_AUTHOR);

        verify(accountGateway).persistBlocking(any(AccountAction.class));
        verify(accountGateway, never()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void addAction_invalid_account() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addAction(invalidAccountId, AccountAction.Action.OPENED_AUTHOR));
    }

    @Test
    public void addAction_account_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addAction(null, AccountAction.Action.OPENED_AUTHOR));
    }

    @Test
    public void addAction_action_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.addAction(validAccountId, null));
    }

    @Test
    public void addLogEntry() {
        accountService.addLogEntry(validAccountId, AccountLogEntry.Action.PII_CHANGE, null, "message");

        verify(accountGateway).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void addLogEntry_invalid_account() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addLogEntry(invalidAccountId,
                        AccountLogEntry.Action.PII_CHANGE, null, "message"));
    }

    @Test
    public void addLogEntry_account_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addLogEntry((UUID) null, AccountLogEntry.Action.PII_CHANGE, null, "message"));
    }

    @Test
    public void addLogEntry_action_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addLogEntry(validAccountId, null, null, "message"));
    }

    @Test
    public void addLogEntry_message_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.addLogEntry(validAccountId, AccountLogEntry.Action.PII_CHANGE, null, null));
    }

    @Test
    public void findById() {

        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.just(dbAccount));

        Flux<Account> account = accountService.findById(validAccountId);

        assertSame(dbAccount, account.blockFirst());
    }

    @Test
    public void findById_ignore_invalid() {
        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.just(dbAccount));
        when(accountGateway.findAccountById(invalidAccountId)).thenReturn(Flux.empty());

        Flux<Account> accounts = accountService.findById(validAccountId, invalidAccountId);
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void findById_account_invalid() {
        when(accountGateway.findAccountById(invalidAccountId)).thenReturn(Flux.empty());

        Flux<Account> accounts = accountService.findById(invalidAccountId);
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findById_accounts_empty() {
        Flux<Account> accounts = accountService.findById();
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findById_account_null() {
        assertThrows(IllegalArgumentException.class, () -> accountService.findById((UUID[]) null));
    }

    @Test
    public void findByHistoricalId() {
        Flux<Account> accounts = accountService.findByHistoricalId(validAccountHistoricalId);
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertEquals(dbAccount, results.get(0));
    }

    @Test
    public void findByHistoricalId_ignore_invalid() {
        Flux<Account> accounts = accountService.findByHistoricalId(validAccountHistoricalId, invalidAccountHistoricalId);
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertTrue(results.size() == 1);
    }

    @Test
    public void findByHistoricalId_account_invalid() {
        Flux<Account> accounts = accountService.findByHistoricalId(invalidAccountHistoricalId);
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findByHistoricalId_accounts_empty() {
        Flux<Account> accounts = accountService.findByHistoricalId();
        List<Account> results = accounts.collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findByHistoricalId_account_null() {
        assertThrows(IllegalArgumentException.class, () -> accountService.findByHistoricalId((Long[]) null));
    }

    @Test
    public void findByEmail() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));
        List<Account> results = accountService.findByEmail(emailAddress).collectList().block();
        assertNotNull(results);
        assertEquals(dbAccount, results.get(0));
    }

    @Test
    public void findByEmail_case_insensitive() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));
        List<Account> results = accountService.findByEmail(emailAddress.toUpperCase()).collectList().block();
        assertNotNull(results);
        assertEquals(dbAccount, results.get(0));
    }

    @Test
    public void findByEmail_leading_trailing_spaces() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));
        List<Account> results = accountService.findByEmail("   " + emailAddress + "  ").collectList().block();
        assertNotNull(results);
        assertEquals(dbAccount, results.get(0));
    }

    @Test
    public void findByEmail_ignore_invalid() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));
        when(accountGateway.findAccountByHash(Hashing.email(replacementEmail))).thenReturn(Flux.empty());
        List<Account> results = accountService.findByEmail(emailAddress, replacementEmail).collectList().block();
        assertNotNull(results);
        assertTrue(results.size() == 1);
    }

    @Test
    public void findByEmail_account_invalid() {
        when(accountGateway.findAccountByHash(Hashing.email(replacementEmail))).thenReturn(Flux.empty());
        List<Account> results = accountService.findByEmail(replacementEmail).collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findByEmail_account_null() {
        assertThrows(IllegalArgumentException.class, () -> accountService.findByEmail((String[]) null));
    }

    @Test
    public void findByEmail_accounts_empty() {
        List<Account> results = accountService.findByEmail(new String[0]).collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findBySubscription() {
        List<Account> results = accountService.findBySubscription(validSubscriptionId).collectList().block();
        assertNotNull(results);
        assertEquals(dbAccount, results.get(0));
    }

    @Test
    public void findBySubscription_subscription_null() {
        assertThrows(IllegalArgumentException.class, () -> accountService.findBySubscription((UUID[]) null));
    }

    @Test
    public void findBySubscription_subscriptions_empty() {
        List<Account> results = accountService.findBySubscription(new UUID[0]).collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findBySubscription_subscription_invalid() {
        when(accountGateway.findAccountsBySubscription(invalidSubscriptionId)).thenReturn(Flux.empty());
        List<Account> results = accountService.findBySubscription(invalidSubscriptionId).collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findWithIdentityById() {
        List<AccountAdapter> results = accountService.findWithIdentityById(validAccountId).collectList().block();
        assertNotNull(results);
        assertEquals(dbAccount, results.get(0).getAccount());
        assertEquals(dbAccountIdentity, results.get(0).getIdentityAttributes());
    }

    @Test
    public void findWithIdentityById_no_identity() {
        // force no identity to be found
        when(accountGateway.findAccountIdentityById(any(), eq(validAccountId))).thenReturn(Flux.empty());
        List<AccountAdapter> results = accountService.findWithIdentityById(validAccountId).collectList().block();
        // the stream is empty therefore the adapter is not zipped
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findWithIdentityById_no_avatars() {
        when(avatarGateway.findAvatarInfoByAccountId(any(Region.class), eq(validAccountId))).thenReturn(Flux.empty());
        List<AccountAdapter> results = accountService.findWithIdentityById(validAccountId).collectList().block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
        AccountAdapter adapter = results.get(0);
        assertTrue(adapter.getAvatars().isEmpty());
    }

    @Test
    public void findWithIdentityById_no_accountLogEntries() {
        when(accountGateway.findAccountLogEntry(any(Region.class), eq(validAccountId))).thenReturn(Flux.empty());
        List<AccountAdapter> results = accountService.findWithIdentityById(validAccountId).collectList().block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
        AccountAdapter adapter = results.get(0);
        assertTrue(adapter.getLegacyLogActions().isEmpty());
    }

    @Test
    public void findWithIdentityById_no_accountActions() {
        when(accountGateway.findActionsByAccountId(validAccountId)).thenReturn(Flux.empty());
        List<AccountAdapter> results = accountService.findWithIdentityById(validAccountId).collectList().block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
        AccountAdapter adapter = results.get(0);
        assertTrue(adapter.getActions().isEmpty());
    }

    @Test
    public void findWithIdentityById_account_invalid() {
        List<AccountAdapter> results = accountService.findWithIdentityById(invalidAccountId).collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
//
    @Test
    public void findWithIdentityById_accounts_empty() {
        Boolean hasElements = accountService.findWithIdentityById(new UUID[0]).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findWithIdentityById_account_null() {
        assertThrows(IllegalArgumentException.class, () -> accountService.findWithIdentityById((UUID[]) null));
    }

    @Test
    public void findWithIdentityByHistoricalId() {
        AccountAdapter result = accountService.findWithIdentityByHistoricalId(validAccountHistoricalId).blockFirst();

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(
                            () -> assertEquals(dbAccount, result.getAccount()),
                            () -> assertEquals(dbAccountIdentity, result.getIdentityAttributes())
                    );
                }
        );
    }

    @Test
    public void findWithIdentityByHistoricalId_account_invalid() {
        Boolean hasElements = accountService.findWithIdentityByHistoricalId(invalidAccountHistoricalId).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findWithIdentityByHistoricalId_account_null() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.findWithIdentityByHistoricalId((Long[]) null));
    }

    @Test
    public void findWithIdentityByHistoricalId_accounts_empty() {
        Boolean hasElements = accountService.findWithIdentityByHistoricalId(new Long[0]).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findWithIdentityByEmail() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));

        AccountAdapter result = accountService.findWithIdentityByEmail(emailAddress).blockFirst();

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(
                            () -> assertEquals(dbAccount, result.getAccount()),
                            () -> assertEquals(dbAccountIdentity, result.getIdentityAttributes())
                    );
                }
        );
    }

    @Test
    public void findWithIdentityByEmail_case_insensitive() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));

        AccountAdapter result = accountService.findWithIdentityByEmail(emailAddress.toUpperCase()).blockFirst();

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(
                            () -> assertEquals(dbAccount, result.getAccount()),
                            () -> assertEquals(dbAccountIdentity, result.getIdentityAttributes())
                    );
                }
        );
    }

    @Test
    public void findWithIdentityByEmail_leading_trailing_spaces() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));

        AccountAdapter result = accountService.findWithIdentityByEmail("   " + emailAddress + "  ").blockFirst();

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(
                            () -> assertEquals(dbAccount, result.getAccount()),
                            () -> assertEquals(dbAccountIdentity, result.getIdentityAttributes())
                    );
                }
        );
    }

    @Test
    public void findWithIdentityByEmail_ignore_invalid() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));
        when(accountGateway.findAccountByHash(Hashing.email(replacementEmail))).thenReturn(Flux.empty());

        Long count = accountService.findWithIdentityByEmail(emailAddress, replacementEmail).count().block();

        assertTrue(count != null && count == 1);
    }

    @Test
    public void findWithIdentityByEmail_account_invalid() {
        when(accountGateway.findAccountByHash(Hashing.email(replacementEmail))).thenReturn(Flux.empty());

        Boolean hasElements = accountService.findWithIdentityByEmail(replacementEmail).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findWithIdentityByEmail_account_null() {
        assertThrows(IllegalArgumentException.class, () -> accountService.findWithIdentityByEmail((String[]) null));
    }

    @Test
    public void findWithIdentityByEmail_accounts_empty() {
        Boolean hasElements = accountService.findWithIdentityByEmail(new String[0]).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findWithIdentityBySubscription() {
        AccountAdapter result = accountService.findWithIdentityBySubscription(validSubscriptionId).blockFirst();

        assertAll(() -> {
                    assertNotNull(result);
                    assertAll(
                            () -> assertEquals(dbAccount, result.getAccount()),
                            () -> assertEquals(dbAccountIdentity, result.getIdentityAttributes())
                    );
                }
        );
    }

    @Test
    public void findWithIdentityBySubscription_subscription_null() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.findWithIdentityBySubscription((UUID[]) null));
    }

    @Test
    public void findWithIdentityBySubscription_subscriptions_empty() {
        Boolean hasElements = accountService.findWithIdentityBySubscription(new UUID[0]).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findWithIdentityBySubscription_subscription_invalid() {
        when(accountGateway.findAccountsBySubscription(invalidSubscriptionId)).thenReturn(Flux.empty());

        Boolean hasElements = accountService.findWithIdentityBySubscription(invalidSubscriptionId).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findLogEntriesByAccountId() {
        Boolean hasElements = accountService.findLogEntriesByAccountId(validAccountId).hasElements().block();

        assertTrue(hasElements != null && hasElements);
    }

    @Test
    public void findLogEntriesByAccountId_account_invalid() {
        Boolean hasElements = accountService.findLogEntriesByAccountId(invalidAccountId).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findShadowAttribute() {
        Boolean hasElements = accountService.findShadowAttributes(validAccountId).hasElements().block();

        assertTrue(hasElements != null && hasElements);
    }

    @Test
    public void findShadowAttribute_account_invalid() {
        Boolean hasElements = accountService.findShadowAttributes(invalidAccountId).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void findShadowAttribute_with_name() {
        Boolean hasElements =
                accountService.findShadowAttributes(AccountShadowAttributeName.GEO_COUNTRY_CODE, validAccountId).hasElements().block();

        assertTrue(hasElements != null && hasElements);
    }

    /*
     * "Credential" services.
     */

    @Test
    public void verifyByEmailAndPassword() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));

        boolean result = accountService.verifyByEmailAndPassword(emailAddress, clearTextPassword);

        assertTrue(result);
    }

    @Test
    public void verifyByEmailAndPassword_email_case_insensitive() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));

        boolean result = accountService.verifyByEmailAndPassword(emailAddress.toUpperCase(), clearTextPassword);

        assertTrue(result);
    }

    @Test
    public void verifyByEmailAndPassword_password_case_sensitive() {
        when(accountGateway.findAccountByHash(emailAddressHash)).thenReturn(Flux.just(dbAccount));

        boolean result = accountService.verifyByEmailAndPassword(emailAddress, clearTextPassword.toUpperCase());

        assertFalse(result);
    }

    @Test
    public void verifyByEmailAndPassword_email_invalid() {
        when(accountGateway.findAccountByHash(Hashing.email(replacementEmail))).thenReturn(Flux.empty());

        boolean result = accountService.verifyByEmailAndPassword(replacementEmail, clearTextPassword);

        assertFalse(result);
    }

    @Test
    public void verifyByEmailAndPassword_email_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.verifyByEmailAndPassword(null, clearTextPassword));
    }

    @Test
    public void verifyByEmailAndPassword_password_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.verifyByEmailAndPassword(emailAddress, null));
    }

    @Test
    public void setPassword() {
        accountService.setPassword(validAccountId, clearTextPassword);

        verify(accountGateway).setPasswordFieldsBlocking(eq(validAccountId), any(String.class), eq(false), isNull());
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPassword_account_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setPassword(invalidAccountId, clearTextPassword));
    }

    @Test
    public void setPassword_must_reset() {
        accountService.setPassword(validAccountId, clearTextPassword, true);

        verify(accountGateway).setPasswordFieldsBlocking(eq(validAccountId), any(String.class), eq(true), isNull());
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPassword_account_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setPassword(null, clearTextPassword, false));
    }

    @Test
    public void setPassword_password_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setPassword(validAccountId, null, false));
    }

    @Test
    public void setPasswordExpired() {
        accountService.setPasswordExpired(validAccountId, true);

        verify(accountGateway).setPasswordFieldsBlocking(validAccountId, passwordHash, true, null);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPasswordExpired_account_invalid() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setPasswordExpired(invalidAccountId, true));
    }

    @Test
    public void setPasswordExpired_account_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setPasswordExpired(null, true));
    }

    @Test
    public void setTemporaryPassword() {
        accountService.setTemporaryPassword(validAccountId, passwordTemporary);

        verify(accountGateway).setPasswordFieldsBlocking(validAccountId, passwordHash, true, passwordTemporary);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPassword_clears_temporary() {
        dbAccount.setPasswordTemporary(passwordTemporary);
        accountService.setPassword(validAccountId, clearTextPassword, true);

        verify(accountGateway).setPasswordFieldsBlocking(eq(validAccountId), any(String.class), eq(true), isNull());
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setPasswordExpired_keeps_temporary() {
        dbAccount.setPasswordTemporary(passwordTemporary);
        accountService.setPasswordExpired(validAccountId, true);

        verify(accountGateway).setPasswordFieldsBlocking(validAccountId, passwordHash, true, passwordTemporary);
        verify(accountGateway, atLeastOnce()).persistBlocking(any(AccountLogEntry.class));
    }

    @Test
    public void setTemporaryPassword_account_required() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setTemporaryPassword(null, passwordTemporary));
    }

    @Test
    public void setTemporaryPassword_password_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setTemporaryPassword(validAccountId, null));
    }

    @Test
    public void setPrimaryEmail_camelCase() {
        UUID accountId = UUID.fromString("fc9f171a-0f9c-4722-b88f-08b6f93584d6");
        String emailAddress = "SomeEmail@dev.dev";

        mockAccountAdapter(accountId, emailAddress);
        accountService.setPrimaryEmail(accountId, emailAddress.toLowerCase());
        // this test should not throw any exception
    }

    @Test
    public void setPrimaryEmail_lowerCase() {
        UUID accountId = UUID.fromString("fc9f171a-0f9c-4722-b88f-08b6f93584d6");
        String emailAddress = "someemail@dev.dev";

        mockAccountAdapter(accountId, emailAddress);
        accountService.setPrimaryEmail(accountId, emailAddress.toLowerCase());
        // this test should not throw any exception
    }

    @Test
    public void setPrimaryEmail_upperCase() {
        UUID accountId = UUID.fromString("fc9f171a-0f9c-4722-b88f-08b6f93584d6");
        String emailAddress = "SOMEEMAIL@DEV.DEV";

        mockAccountAdapter(accountId, emailAddress);
        accountService.setPrimaryEmail(accountId, emailAddress.toLowerCase());
        // this test should not throw any exception
    }

    @Test
    public void setPrimaryEmail_notMatchingEmail() {
        UUID accountId = UUID.fromString("fc9f171a-0f9c-4722-b88f-08b6f93584d6");
        String emailAddress = "SomeEmail@dev.dev";

        mockAccountAdapter(accountId, emailAddress);
        assertThrows(IllegalArgumentException.class,
                () -> accountService.setPrimaryEmail(accountId, "anotherEmail@dev.dev".toLowerCase()));
        // this test should throw the exception
    }

    private void mockAccountAdapter(UUID accountId, String email) {
        List<AccountAvatar> avatars = new ArrayList<>();
        List<AccountAction> actions = new ArrayList<>();
        List<AccountLogEntry> accountLogEntries = new ArrayList<>();
        Account account = mock(Account.class);
        AccountIdentityAttributes identity = mock(AccountIdentityAttributes.class);

        when(identity.getAccountId()).thenReturn(accountId);
        when(identity.getEmail()).thenReturn(Sets.newHashSet(email));
        when(identity.getPrimaryEmail()).thenReturn(email);
        when(account.getIamRegion()).thenReturn(subscriptionRegion);
        when(account.getId()).thenReturn(accountId);
        when(accountGateway.findAccountById(accountId))
                .thenReturn(Flux.just(account));
        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.just(identity));

        when(avatarGateway.findAvatarInfoByAccountId(subscriptionRegion, accountId))
                .thenReturn(Flux.just(avatars).flatMapIterable(one->one));
        when(accountGateway.findActionsByAccountId(accountId))
                .thenReturn(Flux.just(actions).flatMapIterable(one->one));

        when(accountGateway.findAccountLogEntry(subscriptionRegion, accountId))
                .thenReturn(Flux.just(accountLogEntries).flatMapIterable(one->one));
    }

    @Test
    void findByBearerToken() {
        String token = "Uh5MQcKRREWps_WubUaijVwguoVIUTBp";
        Account expected = new Account();
        when(credentialGateway.findBearerToken(token))
                .thenReturn(Mono.just(new BearerToken().setAccountId(validAccountId)));
        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.just(expected));

        Account result = accountService.findByBearerToken(token).block();

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void findByBearerToken_TokenNotValid() {
        String token = "Uh5MQcKRREWps_WubUaijVwguoVIUTBp";
        when(credentialGateway.findBearerToken(token)).thenReturn(Mono.empty());

        Account result = accountService.findByBearerToken(token).block();

        assertEquals(null, result);
    }

    @Test
    void getAccountPayload_identityNotFound() {
        Account account = new Account().setIamRegion(Region.AU).setId(validAccountId);
        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId())).thenReturn(Flux.empty());
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.empty());

        assertNotNull(accountService.getAccountPayload(account).block());
    }

    @Test
    void getAccountPayload_avatarNotFound() {
        Account account = new Account()
                .setIamRegion(Region.US)
                .setId(validAccountId)
                .setRoles(roles)
                .setSubscriptionId(validSubscriptionId);

        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.just(buildAccountIdentityAttributes()));
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.empty());

        AccountPayload payload = accountService.getAccountPayload(account).block();

        assertAll(()->{
            assertNotNull(payload);
            assertEquals(emailAddress, payload.getPrimaryEmail());
            assertEquals(validAccountId, payload.getAccountId());
            assertEquals(familyName, payload.getFamilyName());
            assertEquals(givenName, payload.getGivenName());
            assertEquals(validSubscriptionId, payload.getSubscriptionId());
            assertEquals(honorificPrefix, payload.getHonorificPrefix());
            assertEquals(honorificSuffix, payload.getHonorificSuffix());
            assertEquals(affiliation, payload.getAffiliation());
            assertEquals(Region.US, payload.getIamRegion());
            assertEquals(jobTitle, payload.getJobTitle());
            assertEquals(roles, payload.getRoles());
            assertNull(payload.getAvatarSmall());
        });
    }

    @Test
    void getAccountPayload() {
        Account account = buildAccount();

        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.just(buildAccountIdentityAttributes()));
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.just(new AccountAvatar().setData("data").setMimeType("png")));

        AccountPayload payload = accountService.getAccountPayload(account).block();

        assertAll(()->{
            assertNotNull(payload);
            assertEquals(emailAddress, payload.getPrimaryEmail());
            assertEquals(validAccountId, payload.getAccountId());
            assertEquals(familyName, payload.getFamilyName());
            assertEquals(givenName, payload.getGivenName());
            assertEquals(validSubscriptionId, payload.getSubscriptionId());
            assertEquals(honorificPrefix, payload.getHonorificPrefix());
            assertEquals(honorificSuffix, payload.getHonorificSuffix());
            assertEquals(affiliation, payload.getAffiliation());
            assertEquals(Region.US, payload.getIamRegion());
            assertEquals(jobTitle, payload.getJobTitle());
            assertEquals(roles, payload.getRoles());
            assertNotNull(payload.getAvatarSmall());
        });
    }

    @Test
    void getAccountSummaryPayload_noIdentityFound() {
        Account account = new Account().setIamRegion(Region.AU).setId(validAccountId);
        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId())).thenReturn(Flux.empty());
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.empty());

        assertNotNull(accountService.getAccountSummaryPayload(account).block());
    }

    @Test
    void getAccountSummaryPayload_noAvatarFound() {
        Account account = new Account()
                .setIamRegion(Region.US)
                .setId(validAccountId);

        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.just(buildAccountIdentityAttributes()));
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.empty());

        AccountSummaryPayload payload = accountService.getAccountSummaryPayload(account).block();

        assertAll(()->{
            assertNotNull(payload);
            assertEquals(emailAddress, payload.getPrimaryEmail());
            assertEquals(validAccountId, payload.getAccountId());
            assertEquals(familyName, payload.getFamilyName());
            assertEquals(givenName, payload.getGivenName());
            assertEquals(validSubscriptionId, payload.getSubscriptionId());
            assertNull(payload.getAvatarSmall());
        });
    }

    @Test
    void getAccountSummaryPayload() {
        Account account = buildAccount();

        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.just(buildAccountIdentityAttributes()));
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.just(new AccountAvatar().setData("data").setMimeType("png")));

        AccountSummaryPayload payload = accountService.getAccountSummaryPayload(account).block();

        assertAll(()->{
            assertNotNull(payload);
            assertEquals(emailAddress, payload.getPrimaryEmail());
            assertEquals(validAccountId, payload.getAccountId());
            assertEquals(familyName, payload.getFamilyName());
            assertEquals(givenName, payload.getGivenName());
            assertEquals(validSubscriptionId, payload.getSubscriptionId());
            assertNotNull(payload.getAvatarSmall());
        });
    }

    @Test
    void getAccountPayload_byAccountId_accountNotFound() {
        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.empty());
        assertNull(accountService.getAccountPayload(validAccountId).block());
    }

    @Test
    void getAccountPayload_byAccountId() {
        Account account = buildAccount();

        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.just(account));
        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.empty());
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.empty());

        AccountPayload result = accountService.getAccountPayload(validAccountId).block();
        assertNotNull(result);
        assertEquals(account.getId(), result.getAccountId());

        verify(accountGateway, atLeastOnce()).findAccountIdentityById(account.getIamRegion(), account.getId());
        verify(avatarGateway, atLeastOnce()).findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL);
    }

    private AccountIdentityAttributes buildAccountIdentityAttributes() {
        return new AccountIdentityAttributes()
                .setAccountId(validAccountId)
                .setAffiliation(affiliation)
                .setFamilyName(familyName)
                .setGivenName(givenName)
                .setHonorificPrefix(honorificPrefix)
                .setHonorificSuffix(honorificSuffix)
                .setJobTitle(jobTitle)
                .setPrimaryEmail(emailAddress)
                .setSubscriptionId(validSubscriptionId)
                .setEmail(Sets.newHashSet(emailAddress));
    }

    private Account buildAccount() {
        return buildAccount(validAccountId);
    }

    private Account buildAccount(UUID accountId) {
        return new Account()
                .setIamRegion(Region.US)
                .setId(accountId)
                .setRoles(roles)
                .setSubscriptionId(validSubscriptionId);
    }

    @Test
    void getCollaboratorPayload_accountNotFound() {
        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.empty());
        assertNull(accountService.getCollaboratorPayload(validAccountId, PermissionLevel.REVIEWER).block());
    }

    @Test
    void getCollaboratorPayload() {
        Account account = buildAccount();
        when(accountGateway.findAccountById(validAccountId)).thenReturn(Flux.just(account));
        when(accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId()))
                .thenReturn(Flux.empty());
        when(avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(), AccountAvatar.Size.SMALL))
                .thenReturn(Flux.empty());

        CollaboratorPayload collaboratorPayload = accountService
                .getCollaboratorPayload(validAccountId, PermissionLevel.REVIEWER).block();

        assertNotNull(collaboratorPayload);
        assertEquals(PermissionLevel.REVIEWER, collaboratorPayload.getPermissionLevel());
    }

    @Test
    void migrateAccount_noRoles() {
        UUID newSubscriptionId = UUID.randomUUID();
        UUID oldSubscriptionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Set<AccountRole> roles = Sets.newHashSet();

        Account accountToMigrate = new Account()
                .setId(accountId)
                .setSubscriptionId(oldSubscriptionId);

        Throwable t = assertThrows(IllegalArgumentException.class, () -> accountService.migrateAccountTo(accountToMigrate, newSubscriptionId, roles));
        assertEquals("at least 1 role required", t.getMessage());

        verify(accountGateway, never()).updateSubscriptionAndRoles(accountToMigrate, newSubscriptionId, roles);
        verify(accountGateway, never()).findAccountById(accountId);
    }

    @Test
    void migrateAccount_noSubscription() {
        UUID newSubscriptionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.DEVELOPER);

        Account accountToMigrate = new Account()
                .setId(accountId);

        Throwable t = assertThrows(IllegalArgumentException.class, () -> accountService.migrateAccountTo(accountToMigrate, newSubscriptionId, roles));
        assertEquals("subscriptionId is required", t.getMessage());

        verify(accountGateway, never()).updateSubscriptionAndRoles(accountToMigrate, newSubscriptionId, roles);
        verify(accountGateway, never()).findAccountById(accountId);
    }

    @Test
    void migrateAccount() {
        UUID newSubscriptionId = UUID.randomUUID();
        UUID oldSubscriptionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.DEVELOPER);

        Account accountToMigrate = new Account()
                .setId(accountId)
                .setSubscriptionId(oldSubscriptionId)
                .setRoles(Sets.newHashSet(AccountRole.DEVELOPER));

        Account migrated = new Account()
                .setId(accountId)
                .setSubscriptionId(newSubscriptionId)
                .setRoles(Sets.newHashSet(AccountRole.DEVELOPER));

        when(accountGateway.findAccountById(accountToMigrate.getId())).thenReturn(Flux.just(migrated));
        when(accountGateway.updateSubscriptionAndRoles(accountToMigrate, newSubscriptionId, roles)).thenReturn(Flux.just(new Void[]{}));
        Account updated = accountService.migrateAccountTo(accountToMigrate, newSubscriptionId, roles).block();
        assertNotNull(updated);
        assertEquals(migrated.getId(), updated.getId());
        assertEquals(migrated.getSubscriptionId(), updated.getSubscriptionId());

        verify(accountGateway, atLeastOnce()).updateSubscriptionAndRoles(accountToMigrate, newSubscriptionId, roles);
        verify(accountGateway, atLeastOnce()).findAccountById(migrated.getId());
    }

    @Test
    void getAccountSummaryPayloads_allReturned() {
        int limit = 2;
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        UUID accountIdThree = UUID.randomUUID();

        when(accountGateway.findAccountById(accountIdOne))
                .thenReturn(Flux.just(buildAccount(accountIdOne)));

        when(accountGateway.findAccountById(accountIdTwo))
                .thenReturn(Flux.just(buildAccount(accountIdTwo)));

        when(accountGateway.findAccountById(accountIdThree))
                .thenReturn(Flux.just(buildAccount(accountIdThree)));

        when(avatarGateway.findAvatarByAccountId(any(Region.class), any(UUID.class), any(AccountAvatar.Size.class)))
                .thenReturn(Flux.just(new AccountAvatar()));

        when(accountGateway.findAccountIdentityById(any(Region.class), any(UUID.class)))
        .thenReturn(Flux.just(new AccountIdentityAttributes()));

        List<AccountSummaryPayload> all = accountService
                .getAccountSummaryPayloads(limit, Flux.just(accountIdOne, accountIdTwo, accountIdThree))
                .collectList()
                .block();

        assertAll(()->{
            assertNotNull(all);
            assertEquals(limit, all.size());
        });
    }

    @Test
    void getAccountSummaryPayloads_oneIsSkipped() {
        int limit = 3;
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        UUID accountIdThree = UUID.randomUUID();

        TestPublisher<Account> accountTestPublisher = TestPublisher.create();
        Flux<Account> error = accountTestPublisher.error(new NoSuchElementException("not found")).flux();

        when(accountGateway.findAccountById(accountIdOne))
                .thenReturn(Flux.just(buildAccount(accountIdOne)));

        when(accountGateway.findAccountById(accountIdTwo))
                .thenReturn(error);

        when(accountGateway.findAccountById(accountIdThree))
                .thenReturn(Flux.just(buildAccount(accountIdThree)));

        when(avatarGateway.findAvatarByAccountId(any(Region.class), any(UUID.class), any(AccountAvatar.Size.class)))
                .thenReturn(Flux.just(new AccountAvatar()));

        when(accountGateway.findAccountIdentityById(any(Region.class), any(UUID.class)))
                .thenReturn(Flux.just(new AccountIdentityAttributes()));

        List<AccountSummaryPayload> all = accountService
                .getAccountSummaryPayloads(limit, Flux.just(accountIdOne, accountIdTwo, accountIdThree))
                .collectList()
                .block();

        assertAll(()->{
            assertNotNull(all);
            assertEquals(limit - 1, all.size());
        });
    }

    @Test
    void nameParse_allNull() {
        String[] actual = accountService.deriveGivenAndFamilyNames(null, null, null);
        assertEquals(2, actual.length);
        assertNull(actual[0]);
        assertNull(actual[1]);
    }

    @Test
    void nameParse_trim() {
        String[] actual = accountService.deriveGivenAndFamilyNames(" jack", "white ", null);
        assertEquals("jack", actual[0]);
        assertEquals("white", actual[1]);
    }

    @Test
    void nameParse_full_easy() {
        String[] actual = accountService.deriveGivenAndFamilyNames(null, null, "jack white");
        assertEquals("jack", actual[0]);
        assertEquals("white", actual[1]);
    }

    @Test
    void nameParse_full_fallback() {
        String[] actual = accountService.deriveGivenAndFamilyNames(null, null, "jack");
        assertEquals("jack", actual[0]);
        assertNull(actual[1]);
    }

    @Test
    void nameParse_double_barreled_full() {
        String[] actual = accountService.deriveGivenAndFamilyNames(null, null, "Kelly Van Der Beek");
        assertEquals("Kelly", actual[0]);
        assertEquals("Van Der Beek", actual[1]);
    }

    @Test
    public void setAccountPassword() {
        accountService.setAccountPassword(validAccountId, clearTextPassword);

        verify(accountGateway).setPasswordFields(eq(validAccountId), any(String.class), eq(false), isNull());
        verify(accountGateway, atLeastOnce()).persist(any(AccountLogEntry.class));
    }

    @Test
    public void setAccountPassword_account_invalid() {
        assertThrows(IllegalArgumentException.class,
                     () -> accountService.setAccountPassword(invalidAccountId, clearTextPassword));
    }

    @Test
    public void setAccountPassword_must_reset() {
        accountService.setAccountPassword(validAccountId, clearTextPassword, true);

        verify(accountGateway).setPasswordFields(eq(validAccountId), any(String.class), eq(true), isNull());
        verify(accountGateway, atLeastOnce()).persist(any(AccountLogEntry.class));
    }

    @Test
    public void setAccountPassword_account_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setAccountPassword(null, clearTextPassword, false));
    }

    @Test
    public void setAccountPassword_password_required() {
        assertThrows(IllegalArgumentException.class, () -> accountService.setAccountPassword(validAccountId, null, false));
    }

    @Test
    public void verifyPassword() {
        assertTrue(accountService.verifyPassword(clearTextPassword, passwordHash));
    }

    @Test
    public void verifyPassword_password_required() {
        assertThrows(IllegalArgumentFault.class, () -> accountService.verifyPassword(null, passwordHash));
    }

    @Test
    public void verifyPassword_hash_required() {
        assertThrows(IllegalArgumentFault.class, () -> accountService.verifyPassword(clearTextPassword, null));
    }

    @Test
    public void addShadowAttributeFlux(){

        when(accountGateway.findAccountById(dbAccount.getId())).thenReturn(Flux.just(dbAccount));
        accountService.addShadowAttribute(dbAccount.getId(), AccountShadowAttributeName.ID_ZOHO, "ID_ZOHO", AccountShadowAttributeSource.SYSTEM);
        verify(accountGateway).findAccountById(dbAccount.getId());
    }

    @Test
    public void addShadowAttributeFlux_accountNullCheck(){

        assertThrows(IllegalArgumentFault.class, () ->
                accountService.addShadowAttribute(null, AccountShadowAttributeName.ID_ZOHO, "ID_ZOHO", AccountShadowAttributeSource.SYSTEM));
    }

    @Test
    public void addShadowAttributeFlux_shadowAttributeNullCheck(){

        assertThrows(IllegalArgumentFault.class, () ->
                accountService.addShadowAttribute(dbAccount.getId(), null, "ID_ZOHO", AccountShadowAttributeSource.SYSTEM));
    }

    @Test
    public void addShadowAttributeFlux_valueNullCheck(){

        assertThrows(IllegalArgumentFault.class, () ->
                accountService.addShadowAttribute(null, AccountShadowAttributeName.ID_ZOHO, null, AccountShadowAttributeSource.SYSTEM));
    }

    @Test
    public void addShadowAttributeFlux_shadowAttributeSourceNullCheck(){

        assertThrows(IllegalArgumentFault.class, () ->
                accountService.addShadowAttribute(null, AccountShadowAttributeName.ID_ZOHO, "ID_ZOHO", null));
    }

    @Test
    public void deleteShadowAttributes() {
        when(accountGateway.deleteShadowAttributes(dbAccount.getIamRegion(), dbAccount.getId(), AccountShadowAttributeName.ID_ZOHO)).thenReturn(Flux.just());
        accountService.deleteShadowAttributes(dbAccount.getIamRegion(), dbAccount.getId(), AccountShadowAttributeName.ID_ZOHO);
        verify(accountGateway).deleteShadowAttributes(dbAccount.getIamRegion(), dbAccount.getId(), AccountShadowAttributeName.ID_ZOHO);
    }

    @Test
    public void deleteShadowAttributes_revionNull() {
        assertThrows(IllegalArgumentFault.class, () ->
                accountService.deleteShadowAttributes(null, dbAccount.getId(), AccountShadowAttributeName.ID_ZOHO));
    }

    @Test
    public void deleteShadowAttributes_accountIdNull() {
        assertThrows(IllegalArgumentFault.class, () ->
                accountService.deleteShadowAttributes(dbAccount.getIamRegion(), null, AccountShadowAttributeName.ID_ZOHO));
    }

    @Test
    public void deleteShadowAttributes_shadowAttributeNull() {
        assertThrows(IllegalArgumentFault.class, () ->
                accountService.deleteShadowAttributes(dbAccount.getIamRegion(), dbAccount.getId(), null));
    }
}
