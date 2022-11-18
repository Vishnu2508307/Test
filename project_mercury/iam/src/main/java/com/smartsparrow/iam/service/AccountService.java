package com.smartsparrow.iam.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.data.AccountAvatarGateway;
import com.smartsparrow.iam.data.AccountGateway;
import com.smartsparrow.iam.data.CredentialGateway;
import com.smartsparrow.iam.data.CredentialsType;
import com.smartsparrow.iam.data.FederatedIdentity;
import com.smartsparrow.iam.data.FederationGateway;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.data.IesAccountTrackingGateway;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.Emails;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.Passwords;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import blackboard.blti.message.Role;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Service to manage Accounts.
 * <p>
 * Most calls perform an underlying read (findById) prior to processing. This is to verify the existence of the accounts
 * as well as determining the proper data region to read/write from.
 */
@Singleton
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    //
    private final AccountGateway accountGateway;
    private final AccountAvatarGateway avatarGateway;
    private final SubscriptionService subscriptionService;
    private final CredentialGateway credentialGateway;
    //    private final SubscriptionPermissionGateway subscriptionPermissionGateway;
    private final FederationGateway federationGateway;
    private final IesAccountTrackingGateway iesAccountTrackingGateway;

    @Inject
    public AccountService(AccountGateway accountGateway,
                          AccountAvatarGateway avatarGateway,
                          SubscriptionService subscriptionService,
                          CredentialGateway credentialGateway,
                          FederationGateway federationGateway,
                          IesAccountTrackingGateway iesAccountTrackingGateway) {
        this.accountGateway = accountGateway;
        this.avatarGateway = avatarGateway;
        this.subscriptionService = subscriptionService;
        this.credentialGateway = credentialGateway;
        this.federationGateway = federationGateway;
//        this.subscriptionPermissionGateway = subscriptionPermissionGateway;
        this.iesAccountTrackingGateway = iesAccountTrackingGateway;
    }

    /**
     * Provision an account. Creates new subscription for an account.
     *
     * @param source          the origin of the account creation
     * @param honorificPrefix the name prefix, e.g. Mr, Dr, Prof, Capt, etc.
     * @param givenName       the given name / first name
     * @param familyName      the family name / surname
     * @param honorificSuffix the name suffix, e.g. Jr, III, MD, etc.
     * @param email           the email address
     * @param plainPassword   a password in clear text
     * @param affiliation     the affiliation or university
     * @param jobTitle        the title
     * @param isInstructor    true - if instructor role, false - if student
     * @throws ConflictException when the email address already exists
     */
    public AccountAdapter provision(final AccountProvisionSource source,
                                    final String honorificPrefix,
                                    final String givenName,
                                    final String familyName,
                                    final String honorificSuffix,
                                    final String email,
                                    final String plainPassword,
                                    final String affiliation,
                                    final String jobTitle,
                                    final boolean isInstructor,
                                    final AuthenticationType authenticationType) throws ConflictException {
        checkArgument(Emails.isEmailValid(email), "Invalid email address");

        Set<AccountRole> roles = Sets.newHashSet(AccountRole.STUDENT);

        if (isInstructor) {
            roles.add(AccountRole.INSTRUCTOR);
            roles.add(AccountRole.AERO_INSTRUCTOR);
            roles.add(AccountRole.ADMIN);
            roles.add(AccountRole.DEVELOPER);
        }

        Subscription subscription = subscriptionService.create("Subscription for " + Hashing.email(email), Region.GLOBAL);

        return provision(source,
                         subscription.getId(),
                         roles,
                         honorificPrefix,
                         givenName,
                         familyName,
                         honorificSuffix,
                         email,
                         plainPassword,
                         false,
                         affiliation,
                         jobTitle,
                         authenticationType);

    }

    /**
     * Provision an account saving the email as an hash but without persisting any PII in the {@link AccountIdentityAttributes}
     * table
     *
     * @param source the provision source
     * @param email the account email
     * @param isInstructor flag to handle instructor case
     * @return a mono with the provisioned account
     */
    @SuppressWarnings("Duplicates")
    public Mono<Account> provision(final AccountProvisionSource source,
                                   final String email,
                                   final boolean isInstructor) {
        checkArgument(Emails.isEmailValid(email), "Invalid email address");
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.STUDENT);

        if (isInstructor) {
            roles.add(AccountRole.INSTRUCTOR);
            roles.add(AccountRole.AERO_INSTRUCTOR);
            roles.add(AccountRole.ADMIN);
            roles.add(AccountRole.DEVELOPER);
        }

        Subscription subscription = subscriptionService.create("Subscription for " + Hashing.email(email), Region.GLOBAL);

        //
        String _emailAddress = Emails.normalize(email);
        if (!Strings.isNullOrEmpty(_emailAddress)) {
            verifyEmailNotInUse(_emailAddress);
        }

        /*
         * Setup the main account
         */
        UUID accountId = UUIDs.timeBased();
        Account account = new Account()
                //
                .setId(accountId)
                .setIamRegion(subscription.getIamRegion())
                .setSubscriptionId(subscription.getId())
                .setRoles(roles)
                .setStatus(AccountStatus.ENABLED);

        /*
         * Setup identity attributes for this account.
         */
        AccountIdentityAttributes identityAttributes = new AccountIdentityAttributes()
                //
                .setAccountId(accountId)
                .setIamRegion(subscription.getIamRegion())
                .setSubscriptionId(subscription.getId());

        /*
         * Setup a email hash reference
         */
        AccountHash emailHash = null;
        if (!Strings.isNullOrEmpty(_emailAddress)) {
            identityAttributes.setEmail(ImmutableSet.of(_emailAddress));

            emailHash = new AccountHash()
                    //
                    .setIamRegion(subscription.getIamRegion())
                    .setAccountId(accountId)
                    .setHash(Hashing.email(_emailAddress));
        }

        // persist it.
        try {
            accountGateway.persistBlocking(account);
            accountGateway.persistBlocking(identityAttributes);
            if (emailHash != null) {
                accountGateway.persistBlocking(emailHash);
            }
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }

        //
        addShadowAttribute(account, AccountShadowAttributeName.PROVISION_SOURCE, source.name(),
                AccountShadowAttributeSource.REQUEST);

        String msg = String.format("Account provisioned via %s @ %s", source.name(),
                DateFormat.asRFC1123(UUIDs.unixTimestamp(account.getId())));
        addLogEntry(account, AccountLogEntry.Action.PROVISION, null, msg);

        return Mono.just(account);
    }

    /**
     * Provision an account.
     *
     * @param provisionSource   the origin of the account creation
     * @param subscriptionId    the subscription id
     * @param roles             the roles to assign
     * @param honorificPrefix   the name prefix, e.g. Mr, Dr, Prof, Capt, etc.
     * @param givenName         the given name / first name
     * @param familyName        the family name / surname
     * @param honorificSuffix   the name suffix, e.g. Jr, III, MD, etc.
     * @param emailAddress      the email address
     * @param clearTextPassword a password in clear text
     * @param passwordExpired   should the password be marked as expired
     * @param affiliation       the affiliation or university
     * @param jobTitle          the title
     * @return the provisioned Account with provided identity attributes
     * @throws ConflictException when the email address already exists or the subscription is invalid
     */
    @SuppressFBWarnings(value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
            justification = "FindBugs doesn't see that Strings.isNullOrEmpty checks on NPE")
    public AccountAdapter provision(final AccountProvisionSource provisionSource,
                                    final UUID subscriptionId,
                                    @Nullable final Set<AccountRole> roles,
                                    @Nullable final String honorificPrefix,
                                    @Nullable final String givenName,
                                    @Nullable final String familyName,
                                    @Nullable final String honorificSuffix,
                                    @Nullable final String emailAddress,
                                    @Nullable final String clearTextPassword,
                                    @Nullable final Boolean passwordExpired,
                                    @Nullable final String affiliation,
                                    @Nullable final String jobTitle,
                                    final AuthenticationType authenticationType) throws ConflictException {
        //
        Subscription subscription = verifyValidSubscription(subscriptionId);
        //
        String _emailAddress = Emails.normalize(emailAddress);
        if (!Strings.isNullOrEmpty(_emailAddress)) {
            verifyEmailNotInUse(_emailAddress);
        }

        /*
         * Setup the main account
         */
        UUID accountId = UUIDs.timeBased();
        Account account = new Account()
                //
                .setId(accountId)
                .setIamRegion(subscription.getIamRegion())
                .setSubscriptionId(subscription.getId())
                .setRoles(roles)
                .setStatus(AccountStatus.ENABLED);

        // TODO: Move password management into Credentials.
        String passwordHash = null;
        if (!Strings.isNullOrEmpty(clearTextPassword)) {
            passwordHash = Passwords.hash(clearTextPassword);
        }
        account.setPasswordExpired(passwordExpired);
        account.setPasswordHash(passwordHash);

        /*
         * Setup identity attributes for this account.
         */
        AccountIdentityAttributes identityAttributes = new AccountIdentityAttributes()
                //
                .setAccountId(accountId)
                .setIamRegion(subscription.getIamRegion())
                .setSubscriptionId(subscription.getId())
                .setHonorificPrefix(honorificPrefix)
                .setGivenName(givenName)
                .setFamilyName(familyName)
                .setHonorificSuffix(honorificSuffix)
                .setPrimaryEmail(_emailAddress)
                .setAffiliation(affiliation)
                .setJobTitle(jobTitle);

        /*
         * Setup a email hash reference
         */
        AccountHash emailHash = null;
        if (!Strings.isNullOrEmpty(_emailAddress)) {
            identityAttributes.setEmail(ImmutableSet.of(_emailAddress));

            emailHash = new AccountHash()
                    //
                    .setIamRegion(subscription.getIamRegion())
                    .setAccountId(accountId)
                    .setHash(Hashing.email(_emailAddress));
        }

        // persist it.
        try {
            accountGateway.persistBlocking(account);
            accountGateway.persistBlocking(identityAttributes);
            if (emailHash != null) {
                accountGateway.persistBlocking(emailHash);
            }
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }

        //
        addShadowAttribute(account, AccountShadowAttributeName.PROVISION_SOURCE, provisionSource.name(),
                AccountShadowAttributeSource.REQUEST);

        String msg = String.format("Account provisioned via %s @ %s", provisionSource.name(),
                DateFormat.asRFC1123(UUIDs.unixTimestamp(account.getId())));
        addLogEntry(account, AccountLogEntry.Action.PROVISION, null, msg);

        // persist credentials type
        CredentialsType credentialsType = new CredentialsType()
                .setAuthenticationType(authenticationType)
                .setAccountId(accountId);
        if (!Strings.isNullOrEmpty(_emailAddress)) {
            credentialsType.setHash(Hashing.email(_emailAddress));
        }
        credentialGateway.persistCredentialsTypeByAccount(credentialsType).block();

        return new AccountAdapter() //
                .setAccount(account) //
                .setIdentityAttributes(identityAttributes);
    }

    /**
     * Provision an account.
     *
     * @param provisionSource   the origin of the account creation
     * @param subscriptionId    the subscription id
     * @param roles             the roles to assign
     * @param honorificPrefix   the name prefix, e.g. Mr, Dr, Prof, Capt, etc.
     * @param givenName         the given name / first name
     * @param familyName        the family name / surname
     * @param honorificSuffix   the name suffix, e.g. Jr, III, MD, etc.
     * @param emailAddress      the email address
     * @param clearTextPassword a password in clear text
     * @param passwordExpired   should the password be marked as expired
     * @param affiliation       the affiliation or university
     * @param jobTitle          the title
     * @param signupSubjectArea field from signup form
     * @param signupGoal        field from signup form
     * @param signupOffering    field from signup form
     * @return the provisioned Account with provided identity attributes
     * @throws ConflictException when the email address already exists.
     */
    @SuppressFBWarnings(value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
            justification = "FindBugs doesn't see that Strings.isNullOrEmpty prevents NPE")
    public AccountAdapter provision(final AccountProvisionSource provisionSource,
                                    final UUID subscriptionId,
                                    @Nullable final Set<AccountRole> roles,
                                    @Nullable final String honorificPrefix,
                                    @Nullable final String givenName,
                                    @Nullable final String familyName,
                                    @Nullable final String honorificSuffix,
                                    @Nullable final String emailAddress,
                                    @Nullable final String clearTextPassword,
                                    @Nullable final Boolean passwordExpired,
                                    @Nullable final String affiliation,
                                    @Nullable final String jobTitle,
                                    @Nullable final String signupSubjectArea,
                                    @Nullable final String signupGoal,
                                    @Nullable final String signupOffering) throws ConflictException {

        // provision the account.
        AccountAdapter adapter = provision(provisionSource, subscriptionId, roles, honorificPrefix, givenName,
                familyName, honorificSuffix, emailAddress, clearTextPassword,
                passwordExpired, affiliation, jobTitle, AuthenticationType.BRONTE);

        /*
         * add method specific shadow attributes.
         */
        Account account = adapter.getAccount();
        AccountShadowAttributeSource source = AccountShadowAttributeSource.REQUEST;

        if (!Strings.isNullOrEmpty(signupSubjectArea)) {
            addShadowAttribute(account, AccountShadowAttributeName.SIGNUP_SUBJECT_AREA, signupSubjectArea, source);
        }

        if (!Strings.isNullOrEmpty(signupGoal)) {
            addShadowAttribute(account, AccountShadowAttributeName.SIGNUP_GOAL, signupGoal, source);
        }

        if (!Strings.isNullOrEmpty(signupOffering)) {
            addShadowAttribute(account, AccountShadowAttributeName.SIGNUP_OFFERING, signupOffering, source);
        }

        return adapter;
    }

    /**
     * Provision an account by using the user from an LTI message.
     *
     * @param subscriptionId the subscription id
     * @param ltiUser        the user to use for fields
     * @param affiliation    the account organization; e.g. the tool_consumer_instance_name field in LTI/OAuth.
     * @param originLMS      the LMS making the request; e.g. the tool_consumer_instance_guid field in LTI/OAuth.
     * @return the provisioned Account with provided identity attributes
     * @throws ConflictException when the email address already exists.
     */
    @SuppressFBWarnings(value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
            justification = "FindBugs doesn't see that Strings.isNullOrEmpty prevents NPE")
    public AccountAdapter provision(final UUID subscriptionId,
                                    final blackboard.blti.message.User ltiUser,
                                    @Nullable final String affiliation,
                                    @Nullable final String originLMS) throws ConflictException {
        Preconditions.checkArgument(subscriptionId != null, "missing subscription id");
        Preconditions.checkArgument(ltiUser != null, "missing LTI user");

        //
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.STUDENT);
        // determine if the request is from an Instructor.
        if (ltiUser.isInRole(Role.CONTEXT_NAMESPACE, Role.INSTRUCTOR) || ltiUser.isInRole(Role.CONTEXT_NAMESPACE,
                Role.CONTENT_DEVELOPER)
                || ltiUser.isInRole(Role.CONTEXT_NAMESPACE, Role.ADMINISTRATOR) || ltiUser.isInRole(
                Role.CONTEXT_NAMESPACE, Role.TEACHING_ASSISTANT)) {
            roles.add(AccountRole.INSTRUCTOR);
        }

        // parse out the names.
        String[] names = parseNamesFromLTIUser(ltiUser);
        String givenName = names[0];
        String familyName = names[1];

        //
        AccountAdapter adapter = provision(AccountProvisionSource.LTI, subscriptionId, roles, null, givenName,
                familyName, null, ltiUser.getEmail(), null, null, affiliation, null, AuthenticationType.LTI);

        /*
         * add method specific shadow attributes.
         */
        Account account = adapter.getAccount();
        addShadowAttribute(account, AccountShadowAttributeName.PROVISION_SOURCE,
                AccountShadowAttributeSource.LTI.name(), AccountShadowAttributeSource.LTI);

        if (!Strings.isNullOrEmpty(originLMS)) {
            addShadowAttribute(account, AccountShadowAttributeName.LMS_HOSTNAME, originLMS,
                    AccountShadowAttributeSource.LTI);
        }

        return adapter;
    }

    /**
     * Provision a guest account, adds the STUDENT_GUEST role to the user.
     *
     * @param provisionSource the source, e.g. the URL that triggered the provision
     * @param url             the url accessed by the guest
     * @param subscriptionId  the subscription id for this guest.
     * @return the provisioned Account
     * @throws ConflictException if the subscription is invalid
     */
    public Account provisionGuest(final AccountProvisionSource provisionSource, final String url, final UUID subscriptionId)
            throws ConflictException {
        Preconditions.checkArgument(provisionSource != null, "missing provision source");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "missing url");
        Subscription subscription = verifyValidSubscription(subscriptionId);

        //
        UUID accountId = UUIDs.timeBased();
        Account account = new Account()
                //
                .setId(accountId)
                .setIamRegion(Region.GLOBAL)
                .setSubscriptionId(subscription.getId())
                .setRoles(ImmutableSet.of(AccountRole.STUDENT_GUEST))
                .setStatus(AccountStatus.ENABLED);

        //
        try {
            accountGateway.persistBlocking(account);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }

        //
        AccountShadowAttributeSource source = AccountShadowAttributeSource.REQUEST;
        addShadowAttribute(account, AccountShadowAttributeName.PROVISION_SOURCE, provisionSource.name(), source);
        addShadowAttribute(account, AccountShadowAttributeName.PROVISION_URL, url, source);

        //
        String msg = String.format("Account provisioned via %s @ %s", provisionSource.name(),
                DateFormat.asRFC1123(UUIDs.unixTimestamp(accountId)));
        addLogEntry(account, AccountLogEntry.Action.PROVISION, null, msg);

        return account;
    }

    /**
     * Change the names on the account.
     *
     * @param accountId       the account id
     * @param honorificPrefix the prefix, e.g. Dr., Prof., Ms., etc.
     * @param givenName       the account's given name
     * @param familyName      the account's family name
     * @param honorificSuffix the suffix, e.g. Jr., III.
     */
    public void setIdentityNames(final UUID accountId,
                                 final String honorificPrefix,
                                 final String givenName,
                                 final String familyName,
                                 final String honorificSuffix) {

        Account account = verifyValidAccount(accountId);

        try {
            accountGateway.setIdentityNamesBlocking(account.getIamRegion(), account.getId(), honorificPrefix, givenName,
                    familyName, honorificSuffix);
            //
            String msg = "Update to identity names";
            addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, msg);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
    }

    /**
     * Set the identity names based on an lti user.
     *
     * @param accountId
     * @param ltiUser
     */
    public void setIdentityNames(final UUID accountId, final blackboard.blti.message.User ltiUser) {
        Preconditions.checkArgument(accountId != null, "missing account id");
        Preconditions.checkArgument(ltiUser != null, "missing lti user");

        // parse out the names.
        String[] names = parseNamesFromLTIUser(ltiUser);
        String givenName = names[0];
        String familyName = names[1];

        setIdentityNames(accountId, null, givenName, familyName, null);
    }

    /**
     * Disable accounts by id
     *
     * @param callerAccountId the account making the request
     * @param accountIds      the account ids to disable.
     */
    public void disable(final UUID callerAccountId, final UUID... accountIds) {
        Preconditions.checkArgument(callerAccountId != null, "missing caller account id");
        Preconditions.checkArgument(accountIds != null && accountIds.length != 0, "missing account ids");
        //
        findById(accountIds)
                //
                .toIterable()
                //
                .forEach(account -> {
                    //
                    try {
                        accountGateway.mutateStatusBlocking(account.getId(), AccountStatus.DISABLED);
                        //
                        String msg = "Account disabled";
                        addLogEntry(account, AccountLogEntry.Action.STATUS_CHANGE, callerAccountId, msg);
                    } catch (Throwable t) {
                        log.error("an unexpected error occurred", t);
                        throw Exceptions.bubble(t);
                    }
                });
    }

    /**
     * Enable accounts by id
     *
     * @param callerAccountId the account making the request (optional)
     * @param accountIds      the account ids to disable.
     */
    @Deprecated
    public void enable(@Nullable final UUID callerAccountId, final UUID... accountIds) {
        Preconditions.checkArgument(accountIds != null && accountIds.length != 0, "missing account ids");
        //
        findById(accountIds)
                //
                .toIterable()
                //
                .forEach(account -> {
                    //
                    try {
                        accountGateway.mutateStatusBlocking(account.getId(), AccountStatus.ENABLED);
                        //
                        String msg = "Account enabled";
                        addLogEntry(account, AccountLogEntry.Action.STATUS_CHANGE, callerAccountId, msg);
                    } catch (Throwable t) {
                        log.error("an unexpected error occurred", t);
                        throw Exceptions.bubble(t);
                    }
                });
    }

    /**
     * Override any previous emails and set the specified email to account (incl primary address)
     *
     * @param accountId    the account id
     * @param emailAddress the verified email address
     * @throws ConflictException        if the email address already exists
     * @throws IllegalArgumentException if the account id is incorrect
     */
    public void setVerifiedEmail(final UUID accountId, final String emailAddress) throws ConflictException {
        AccountAdapter adapter = verifyValidAccountWithIdentityById(accountId);

        String _emailAddress = Emails.normalize(emailAddress);
        verifyEmailNotInUse(_emailAddress);

        // unwrap.
        Account account = adapter.getAccount();
        AccountIdentityAttributes identityAttributes = adapter.getIdentityAttributes();

        // Grab the current emails.
        Set<String> accountEmails = new HashSet<>();
        if (identityAttributes != null) {
            accountEmails.addAll(identityAttributes.getEmail());
            // ^ will contain the primary address.
        }

        // remove the primary and any previous emails + hashes
        for (String accountEmail : accountEmails) {
            removeEmail(adapter, accountEmail);
        }

        // set the primary
        setPrimaryEmail(adapter, _emailAddress);

        // add the email + hashes
        addVerifiedEmail(account, _emailAddress);
    }

    /**
     * Add an additional verified email to an account
     *
     * @param accountId  the account id
     * @param emailToAdd the verified email address
     * @throws ConflictException if the email address already exists
     */
    public void addVerifiedEmail(final UUID accountId, final String emailToAdd) throws ConflictException {
        Account account = verifyValidAccount(accountId);

        addVerifiedEmail(account, emailToAdd);
    }

    /**
     * (Internal) Add a verified email address to an account
     *
     * @param account    the account
     * @param emailToAdd the email
     * @throws ConflictException if the email already exists
     */
    private void addVerifiedEmail(final Account account, final String emailToAdd) throws ConflictException {
        String _emailToAdd = Emails.normalize(emailToAdd);
        verifyEmailNotInUse(_emailToAdd);

        AccountHash emailHash = new AccountHash()
                //
                .setIamRegion(account.getIamRegion()) //
                .setAccountId(account.getId())        //
                .setHash(Hashing.email(_emailToAdd));

        try {
            accountGateway.addEmailBlocking(account.getIamRegion(), account.getId(), _emailToAdd);
            accountGateway.persistBlocking(emailHash);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }

        String msg = String.format("Added email address: %s", _emailToAdd);
        addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, msg);
    }

    /**
     * Remove an email address from an account; will NULL the primary email if the same.
     *
     * @param accountId    the account id
     * @param emailAddress the email address
     */
    public void removeEmail(final UUID accountId, final String emailAddress) {
        AccountAdapter adapter = verifyValidAccountWithIdentityById(accountId);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(emailAddress));

        removeEmail(adapter, emailAddress);
    }

    /**
     * (Internal) Remove an email address from an account; will NULL the primary email if the same.
     *
     * @param adapter       the account adapter
     * @param emailToRemove the email address
     * @throws IllegalArgumentException if the email does not exist on the account
     */
    @SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF",
            justification = "FindBugs doesn't see that Strings.isNullOrEmpty prevents NPE")
    private void removeEmail(final AccountAdapter adapter, final String emailToRemove) {

        // Improvement: this method is probably more defensive than it needs to be.

        String _emailToRemove = Emails.normalize(emailToRemove);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(_emailToRemove));

        // unwrap and validate
        AccountIdentityAttributes identityAttributes = adapter.getIdentityAttributes();
        Account account = adapter.getAccount();
        if (identityAttributes == null) {
            // nothing to remove;
            if (log.isDebugEnabled()) {
                log.debug("account {} has no identity attributes, therefore no emails that can be removed", account.getId());
            }
            return;
        }

        // Find the address to remove from all of them.
        String mercyEmail = null;
        if (identityAttributes.getEmail().contains(_emailToRemove)) {
            // exact match.
            mercyEmail = _emailToRemove;
        } else {
            // handle the case [john@gmail.com vs John@Gmail.com] could different
            for (String email : identityAttributes.getEmail()) {
                if (email.equalsIgnoreCase(_emailToRemove)) {
                    mercyEmail = email;
                    break;
                }
            }
        }

        // was something found to remove?
        if (Strings.isNullOrEmpty(mercyEmail)) {
            throw new IllegalArgumentException("no such email address");
        }

        // is it the also set to the primary? null it if so.
        if (!Strings.isNullOrEmpty(identityAttributes.getPrimaryEmail()) //
                && identityAttributes.getPrimaryEmail().equalsIgnoreCase(mercyEmail)) {
            // clear the primary!
            setPrimaryEmail(adapter, null);
            addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, "Removed matching primary email address");
        }

        // delete the hash.
        AccountHash hash = new AccountHash().setIamRegion(account.getIamRegion())
                .setAccountId(account.getId())
                .setHash(Hashing.email(mercyEmail));

        try {
            accountGateway.removeEmailBlocking(account.getIamRegion(), account.getId(), mercyEmail);
            accountGateway.deleteBlocking(hash);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }

        String msg = String.format("Removed email address: %s", mercyEmail);
        addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, msg);
    }

    /**
     * Set the specified address as the primary email address on an account. This method will error if the supplied
     * email address is not already a listed email in the account
     *
     * @param accountId    the account id
     * @param emailAddress the email address; using null to set no primary email is accepted.
     * @throws IllegalArgumentException when the email address is not part of the account already.
     */
    public void setPrimaryEmail(final UUID accountId, @Nullable final String emailAddress) {
        AccountAdapter adapter = verifyValidAccountWithIdentityById(accountId);

        String sanitizedEmail = Emails.normalize(emailAddress);
        if (sanitizedEmail != null //
                && !identityContainsEmail(adapter, sanitizedEmail)) {
            throw new IllegalArgumentException("invalid supplied email address");
        }

        setPrimaryEmail(adapter, emailAddress);
    }

    /**
     * Check if an identity contains the supplied email.
     *
     * @param identityAttributes the identity attributes
     * @param email the email address to check
     * @return true if the email address already exists within this identity
     */
    public boolean identityContainsEmail(final AccountIdentityAttributes identityAttributes, final String email) {
        String normalizedEmail = Emails.normalize(email);
        // be a bit defensive.
        if(identityAttributes == null || identityAttributes.getEmail() == null) {
            return false;
        }
        return identityAttributes.getEmail().stream()
                .anyMatch(e -> email.equals(normalizedEmail));
    }

    private boolean identityContainsEmail(AccountAdapter adapter, String sanitizedEmail) {
        return adapter.getIdentityAttributes().getEmail().stream()
                .anyMatch(email -> email.equalsIgnoreCase(sanitizedEmail));
    }

    /**
     * (Internal) set the primary email, it does NOT need be on the account already.
     *
     * @param adapter        the account adapter
     * @param sanitizedEmail the sanitized email address
     */
    private void setPrimaryEmail(final AccountAdapter adapter, @Nullable final String sanitizedEmail) {

        // null is a valid case for the email.

        Account account = adapter.getAccount();
        try {
            accountGateway.setPrimaryEmailBlocking(account.getIamRegion(), account.getId(), sanitizedEmail);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }

        String msg = String.format("Updated primary email address to: %s", sanitizedEmail);
        addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, msg);
    }

    /**
     * Set the roles on the supplied accounts.
     *
     * @param callerAccountId the account making the request
     * @param roles           the roles to set
     * @param accountIds      the account ids
     */
    public void setRoles(final UUID callerAccountId, final Set<AccountRole> roles, final UUID... accountIds) {
        Preconditions.checkArgument(callerAccountId != null, "missing caller account id");
        Preconditions.checkArgument(roles != null && !roles.isEmpty(), "missing roles");
        Preconditions.checkArgument(accountIds != null && accountIds.length != 0, "missing account ids");
        //
        findById(accountIds)
                //
                .toIterable() // don't return until complete.
                .forEach(account -> {
                    try {
                        accountGateway.setRolesBlocking(account.getId(), roles);
                        //
                        String msg = String.format("Set roles to %s", roles);
                        addLogEntry(account, AccountLogEntry.Action.ROLE_CHANGE, callerAccountId, msg);
                    } catch (Throwable t) {
                        log.error("an unexpected error occurred", t);
                        throw Exceptions.propagate(t);
                    }
                });
    }

    /**
     * Add a role to the supplied accounts.
     *
     * @param callerAccountId the account making the request (optional)
     * @param role            the role to add
     * @param accountIds      the account ids
     */
    @Trace(async = true)
    public void addRole(@Nullable final UUID callerAccountId, final AccountRole role, final UUID... accountIds) {
        Preconditions.checkArgument(role != null, "missing role");
        Preconditions.checkArgument(accountIds != null && accountIds.length != 0, "missing account ids");
        //
        findById(accountIds)
                .doOnEach(ReactiveTransaction.linkOnNext())
                //
                .toIterable() // don't return until complete.
                .forEach(account -> {
                    //
                    try {
                        accountGateway.addRoleBlocking(account.getId(), role);
                    } catch (Throwable t) {
                        log.error("an unexpected error occurred", t);
                        throw Exceptions.propagate(t);
                    }
                    //
                    String msg = String.format("Add role %s", role);
                    addLogEntry(account, AccountLogEntry.Action.ROLE_CHANGE, callerAccountId, msg);
                });
    }

    /**
     * Remove a role from the supplied accounts.
     *
     * @param callerAccountId the account making the request (optional)
     * @param role            the role to remove
     * @param accountIds      the account ids
     */
    @Trace(async = true)
    public void removeRole(@Nullable final UUID callerAccountId, final AccountRole role, final UUID... accountIds) {
        Preconditions.checkArgument(callerAccountId != null, "missing caller account id");
        Preconditions.checkArgument(role != null, "missing role");
        Preconditions.checkArgument(accountIds != null && accountIds.length != 0, "missing account ids");
        //
        findById(accountIds)
                .doOnEach(ReactiveTransaction.linkOnNext())
                //
                .toIterable() // don't return until complete.
                .forEach(account -> {
                    try {
                        accountGateway.removeRoleBlocking(account.getId(), role);
                        //
                        String msg = String.format("Remove role %s", role);
                        addLogEntry(account, AccountLogEntry.Action.ROLE_CHANGE, callerAccountId, msg);
                    } catch (Throwable t) {
                        log.error("an unexpected error occurred", t);
                        throw Exceptions.propagate(t);
                    }
                });
    }

    /**
     * Record an action performed by an account
     *
     * @param accountId the account ids
     * @param action    the action performed
     */
    public void addAction(final UUID accountId, final AccountAction.Action action) {
        Preconditions.checkArgument(action != null, "missing action");

        Account account = verifyValidAccount(accountId);

        AccountAction aa = new AccountAction() //
                .setAccountId(account.getId()) //
                .setAction(action) //
                .setId(UUIDs.timeBased());

        try {
            accountGateway.persistBlocking(aa);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
    }

    /**
     * Add a log entry for an account.
     *
     * @param account    the account
     * @param action     the action taken
     * @param onBehalfOf if a user performed an action on behalf of this account (optional)
     * @param message    the message
     */
    void addLogEntry(final Account account,
                     final AccountLogEntry.Action action,
                     @Nullable final UUID onBehalfOf,
                     final String message) {
        //
        AccountLogEntry entry = new AccountLogEntry().setAccountId(account.getId())
                .setIamRegion(account.getIamRegion())
                .setId(UUIDs.timeBased())
                .setAction(action)
                .setOnBehalfOf(onBehalfOf)
                .setMessage(message);
        try {
            accountGateway.persistBlocking(entry);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
    }

    /**
     * Add a log entry for an account
     *
     * @param accountId  the account id
     * @param action     the action taken
     * @param onBehalfOf if a user performed an action on behalf of this accountId (optional)
     * @param message    the message
     */
    public void addLogEntry(final UUID accountId, final AccountLogEntry.Action action, @Nullable final UUID onBehalfOf, final String message) {
        Preconditions.checkArgument(action != null, "missing action");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "missing message");
        Account account = verifyValidAccount(accountId);

        addLogEntry(account, action, onBehalfOf, message);
    }

    /**
     * (Internal) add a Shadow Attribute.
     *
     * @param account the account
     * @param name    the name of the attribute
     * @param value   the value of the attribute
     * @param source  the source deriving this attribute
     */
    private void addShadowAttribute(final Account account,
                                    final AccountShadowAttributeName name,
                                    final String value,
                                    final AccountShadowAttributeSource source) {
        // NB: this is private because adding by an Account is not part of the expected workflow; ok to change later

        // create an attribute who's source data will be appended (not set)
        AccountShadowAttribute shadowAttribute = new AccountShadowAttribute()
                //
                .setAccountId(account.getId())
                .setIamRegion(account.getIamRegion())
                .setAttribute(name)
                .setValue(value)
                .setSource(ImmutableMap.of(UUIDs.timeBased(), source));
        //
        try {
            accountGateway.persistBlocking(shadowAttribute);
            //
            String msg = String.format("Added shadow attribute %s", shadowAttribute.getAttribute());
            addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, msg);
        } catch (Throwable t) {
            //
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
    }

    /**
     * Migrate a user account to a different region.
     *
     * @param accountId the account id to migrate
     * @param toRegion  the region to migrate the data to.
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "FindBugs doesn't see that Preconditions.checkArgument prevents NPE")
    @Deprecated
    public void migrateToRegion(final UUID accountId, final Region toRegion) {
        Preconditions.checkArgument(accountId != null, "missing account");
        Preconditions.checkArgument(toRegion != null, "missing region");

        Account account = findById(accountId).blockFirst();
        Preconditions.checkArgument(account != null, "missing account for id " + accountId);
        Region fromRegion = account.getIamRegion();
        Preconditions.checkArgument(!fromRegion.equals(toRegion), "region already " + toRegion);

        String logMsg = String.format("Changing region from %s to %s", fromRegion, toRegion);
        addLogEntry(account, AccountLogEntry.Action.REGION_CHANGE, null, logMsg);

        // log that we are starting to migrate.
        logMsg = String.format("Data migration starting @ %s", DateFormat.asRFC1123(System.currentTimeMillis()));
        addLogEntry(account, AccountLogEntry.Action.REGION_CHANGE_BEGIN, null, logMsg);

        /*
         * Copy the region specific data from one region to another.
         */

        // Identity
        AccountIdentityAttributes idAttrs = accountGateway.findAccountIdentityById(fromRegion, account.getId()).blockFirst();
        if (idAttrs != null) {
            // > copy
            idAttrs.setIamRegion(toRegion);
            accountGateway.persistBlocking(idAttrs);
            log.info("{} copied identity attributes to {}", accountId, toRegion);
            // > delete
            idAttrs.setIamRegion(fromRegion);
            accountGateway.deleteBlocking(idAttrs);
            log.info("{} deleted identity attributes from {}", accountId, fromRegion);
        }

        // Avatar
        Iterable<AccountAvatar> accountAvatars = avatarGateway.findAvatarByAccountId(fromRegion, accountId).toIterable();
        // > copy
        accountAvatars.forEach(accountAvatar -> {
            accountAvatar.setIamRegion(toRegion);
            avatarGateway.persistBlocking(accountAvatar);
            log.info("{} copied avatar {} to {}", accountId, accountAvatar.getName(), toRegion);
        });
        // > delete
        avatarGateway.deleteAllAvatarsBlocking(fromRegion, accountId);
        log.info("{} deleted all avatars from {}", accountId, fromRegion);

        // Log
        Iterable<AccountLogEntry> accountLogEntries = findLogEntries(account).toIterable();
        // > copy
        accountLogEntries.forEach(accountLogEntry -> {
            accountLogEntry.setIamRegion(toRegion);
            accountGateway.persistBlocking(accountLogEntry);
            log.info("{} copied log entry {} to {}", accountId, accountLogEntry.getId(), toRegion);
        });
        // > delete
        accountGateway.deleteAllLogEntriesBlocking(fromRegion, accountId);
        log.info("{} deleted all log entries from {}", accountId, fromRegion);

        // Shadow
        Iterable<AccountShadowAttribute> accountShadowAttributes = findShadowAttributes(account).toIterable();
        // > copy
        accountShadowAttributes.forEach(accountShadowAttribute -> {
            accountShadowAttribute.setIamRegion(toRegion);
            accountGateway.persistBlocking(accountShadowAttribute);
            log.info("{} copied shadow entry {} to {}", accountId, accountShadowAttribute.getAttribute(), toRegion);
        });
        // > delete
        accountGateway.deleteAllShadowAttributesBlocking(fromRegion, accountId);
        log.info("{} deleted all shadow entries from {}", accountId, fromRegion);

        // Set the account region.
        account.setIamRegion(toRegion);
        accountGateway.persistBlocking(account);
        logMsg = String.format("Changed IAM Region to %s", toRegion);
        addLogEntry(account, AccountLogEntry.Action.PII_CHANGE, null, logMsg);

        // log the migration complete.
        logMsg = String.format("Changed region from %s to %s @ %s", fromRegion, toRegion,
                DateFormat.asRFC1123(System.currentTimeMillis()));
        addLogEntry(account, AccountLogEntry.Action.REGION_CHANGE_COMPLETE, null, logMsg);
    }

    /*
     * FINDERS
     */

    /**
     * Find accounts by id
     *
     * @param accountIds the account ids
     * @return an @{code Flux} of an accounts represented by their id, not necessarily in the same order.
     */
    @Trace(async = true)
    public Flux<Account> findById(final UUID... accountIds) {
        Preconditions.checkArgument(accountIds != null, "missing account ids");
        return Flux.just(accountIds)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(accountGateway::findAccountById);
    }

    /**
     * Find accounts by using a historical id
     *
     * @param historicalIds the account's historical ids
     * @return an @{code Flux} of an accounts represented by their historical id, not necessarily in the same order.
     */
    public Flux<Account> findByHistoricalId(final Long... historicalIds) {
        Preconditions.checkArgument(historicalIds != null, "missing historical ids");
        return Flux.just(historicalIds) //
                .flatMap(accountGateway::findAccountByHistoricalId);
    }

    /**
     * Find accounts by using an email address.
     *
     * @param emails the account's emails
     * @return an @{code Flux} of an accounts represented by their email, not necessarily in the same order.
     */
    @Trace(async = true)
    public Flux<Account> findByEmail(final String... emails) {
        Preconditions.checkArgument(emails != null, "missing emails");
        return Flux.just(emails)
                .map(Emails::normalize)
                .map(Hashing::email)
                .flatMap(accountGateway::findAccountByHash)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the accounts tied to a particular subscription.
     *
     * @param subscriptionIds the subscription ids
     * @return an @{code Flux} of an accounts associated to the subscription, not grouped.
     */
    @Trace(async = true)
    public Flux<Account> findBySubscription(final UUID... subscriptionIds) {
        Preconditions.checkArgument(subscriptionIds != null, "missing subscription ids");
        return Flux.just(subscriptionIds)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(accountGateway::findAccountsBySubscription);
    }

    /**
     * Find the account identity by historical ids.
     *
     * @param historicalIds a single or multiple historical user ids
     * @return a {@link Mono} Map historicalId and AccountIdentityAttributes
     */
    public Mono<Map<String, AccountIdentityAttributes>> findIdentityByHistoricalIdToMap(final Long... historicalIds) {
        return findByHistoricalId(historicalIds)
                .collectMap(one -> String.valueOf(one.getHistoricalId()), two -> accountGateway
                        .findAccountIdentityById(two.getIamRegion(), two.getId()).blockFirst());
    }

    /**
     * Find the account identity by historical id.
     *
     * @param historicalId a single historical id
     * @return an @{code Flux} AccountIdentityAttributes
     */
    public Flux<AccountIdentityAttributes> findIdentityByHistoricalId(final Long historicalId) {
        return findByHistoricalId(historicalId)
                .flatMap(one -> accountGateway.findAccountIdentityById(one.getIamRegion(), one.getId()));
    }

    /**
     * Find an Account and Identity Attributes
     *
     * @param accounts the accounts
     * @return an @{code Flux} of an accounts and identity of the supplied accounts
     */
    private Flux<AccountAdapter> findWithIdentity(final Account... accounts) {
        // from each of the accounts, use the region to find their identity attributes.
        return Flux.just(accounts)
                //
                .flatMap(account -> {
                    // get the identity for the account.
                    Mono<AccountIdentityAttributes> idAttrs = accountGateway
                            .findAccountIdentityById(account.getIamRegion(), account.getId())
                            // if the stream is empty the zip will terminate and return no results
                            .singleOrEmpty();
                    // get the actions
                    Mono<List<AccountAction>> actions = accountGateway.findActionsByAccountId(account.getId())
                            .collectList();
                    // get the avatars
                    Mono<List<AccountAvatar>> avatars = avatarGateway
                            .findAvatarInfoByAccountId(account.getIamRegion(), account.getId()).collectList();
                    // get the latest value of certain shadow attributes as "legacy" values.
                    // Note: these are not normally returned, but the frontend current depends on some of these values
                    //       new ones should generally NOT be added.
                    Mono<Map<AccountShadowAttributeName, AccountShadowAttribute>> shadowLegacy = Flux
                            .just(AccountShadowAttributeName.GEO_COUNTRY_CODE, //
                                    AccountShadowAttributeName.SIGNUP_GOAL,  //
                                    AccountShadowAttributeName.SIGNUP_OFFERING, //
                                    AccountShadowAttributeName.SIGNUP_SUBJECT_AREA, //
                                    AccountShadowAttributeName.ID_ZOHO)
                            .flatMap(name -> accountGateway.findAccountShadowAttribute(account.getIamRegion(), //
                                    account.getId(),  //
                                    name)
                                    // only get the latest.
                                    .take(1)).collectMap(attr -> attr.getAttribute());
                    // more legacy attributes, that were converted to Log actions.
                    Mono<List<AccountLogEntry>> logLegacy = accountGateway
                            .findAccountLogEntry(account.getIamRegion(), account.getId())
                            .filter(logEntry -> Objects.nonNull(logEntry.getAction()))
                            .filter(logEntry -> logEntry.getAction().equals(AccountLogEntry.Action.LOGIN)  //
                                    || logEntry.getAction().equals(AccountLogEntry.Action.LOGIN_LTI)) //
                            .collectList();

                    // package together.
                    return Mono.zip(Mono.just(account), idAttrs, actions, avatars, shadowLegacy, logLegacy)
                            //
                            .map(tupple6 -> new AccountAdapter()
                                    //
                                    .setAccount(tupple6.getT1())
                                    .setIdentityAttributes(tupple6.getT2())
                                    .setActions(tupple6.getT3())
                                    .setAvatars(tupple6.getT4())
                                    .setLegacyAttributes(tupple6.getT5())
                                    .setLegacyLogActions(tupple6.getT6()))

                            //
                            .doOnError(t -> {
                                if (log.isDebugEnabled()) {
                                    log.debug("an unexpected error occurred", t);
                                }
                                throw Exceptions.propagate(t);
                            });
                });
    }

    /**
     * Find Account and Identity Attributes by account id
     *
     * @param accountIds the account ids
     * @return an @{code Flux} of an accounts and identity represented by their id, not necessarily in the same order.
     */
    public Flux<AccountAdapter> findWithIdentityById(final UUID... accountIds) {
        return findById(accountIds).flatMap(this::findWithIdentity);
    }

    /**
     * Find Account and Identity Attributes by historical id
     *
     * @param historicalIds the historical ids
     * @return an @{code Flux} of an accounts and identity represented by their historical id, not necessarily in the same order.
     */
    public Flux<AccountAdapter> findWithIdentityByHistoricalId(final Long... historicalIds) {
        return findByHistoricalId(historicalIds).flatMap(this::findWithIdentity);
    }

    /**
     * Find Account and Identity Attributes by email
     *
     * @param emails the account's emails
     * @return an @{code Flux} of an accounts and identity represented by their email, not necessarily in the same order.
     */
    public Flux<AccountAdapter> findWithIdentityByEmail(final String... emails) {
        return findByEmail(emails).flatMap(this::findWithIdentity);
    }

    /**
     * Find Account and Identity Attributes for all accounts on a particular subscription
     *
     * @param subscriptionIds the subscription ids
     * @return an @{code Flux} of an accounts and identity associated to the subscription, not grouped.
     */
    public Flux<AccountAdapter> findWithIdentityBySubscription(final UUID... subscriptionIds) {
        return findBySubscription(subscriptionIds).flatMap(this::findWithIdentity);
    }

    /**
     * Find the log entries
     *
     * @param accountIds the account ids
     * @return @{code Flux} of log entries for the specified accounts, not necessarily in the same order as supplied.
     */
    public Flux<AccountLogEntry> findLogEntriesByAccountId(final UUID... accountIds) {
        Preconditions.checkArgument(accountIds != null, "missing account ids");
        return findById(accountIds) //
                .flatMap(account -> findLogEntries(account)) //
                .doOnError(t -> {
                    log.error("an unexpected error occurred", t);
                    throw Exceptions.propagate(t);
                });
    }

    /**
     * Find the log entries for an account
     *
     * @param account the account
     * @return @{code Flux} of log entries for the account
     */
    Flux<AccountLogEntry> findLogEntries(final Account account) {
        Preconditions.checkArgument(account != null, "missing account");
        return accountGateway.findAccountLogEntry(account.getIamRegion(), account.getId());
    }

    /**
     * Find all the shadow attributes on the supplied accountIds
     *
     * @param accountIds the account ids
     * @return an @{code Flux} of shadow attributes for the specified accounts, not in the same order as supplied.
     */
    public Flux<AccountShadowAttribute> findShadowAttributes(final UUID... accountIds) {
        Preconditions.checkArgument(accountIds != null, "missing account ids");
        return findById(accountIds) //
                .flatMap(this::findShadowAttributes);
    }

    /**
     * Find the shadow attributes for a given account.
     *
     * @param account the account
     * @return an @{code Flux} of shadow attributes for the specified account
     */
    Flux<AccountShadowAttribute> findShadowAttributes(final Account account) {
        Preconditions.checkArgument(account != null, "missing account");
        return accountGateway.findAccountShadowAttribute(account.getIamRegion(), account.getId());
    }

    /**
     * Find a specific shadow attribute for the supplied accounts.
     *
     * @param name       the name of the shadow attribute
     * @param accountIds the account ids
     * @return an ${code Flux} of specified shadow attributes for the specified accounts
     */
    public Flux<AccountShadowAttribute> findShadowAttributes(final AccountShadowAttributeName name,
                                                             final UUID... accountIds) {
        Preconditions.checkArgument(accountIds != null, "missing account ids");
        return findById(accountIds)
                //
                .flatMap(acct -> accountGateway.findAccountShadowAttribute(acct.getIamRegion(), acct.getId(), name));
    }

    /**
     * Find a specific shadow attribute for an account
     *
     * @param account the account to find the shadow attribute for
     * @param name    the name of the shadow attribute to find
     * @return a mono of account shadow attribute
     * @throws NotFoundFault when the shadow attribute is not found
     */
    public Mono<AccountShadowAttribute> findShadowAttribute(final Account account, final AccountShadowAttributeName name) {
        return accountGateway.findAccountShadowAttribute(account.getIamRegion(), account.getId(), name)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault("this account does not have access to AERO");
                });
    }

    /*
     *
     * These methods should be refactored into a Credentials service.
     *
     */

    /**
     * Verify the account using the email address and password.
     *
     * @param emailAddress      the account email address
     * @param clearTextPassword the password as supplied by the user
     * @return true if the password verifies, false otherwise
     */
    public boolean verifyByEmailAndPassword(final String emailAddress, final String clearTextPassword) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(emailAddress), "missing email");
        // don't allow verification with no password (as it can be null underlying)
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clearTextPassword), "missing clearTextPassword");

        Account account = findByEmail(emailAddress).blockFirst();
        return account != null && Passwords.verify(clearTextPassword, account.getPasswordHash());
    }

    /**
     * Set the password on an account, clearing the temporary password.
     *
     * @param accountId         the account id
     * @param clearTextPassword the password as supplied by the user
     * @param passwordExpired   the password expiration flag
     */
    public void setPassword(final UUID accountId, final String clearTextPassword, final boolean passwordExpired) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clearTextPassword), "missing clearTextPassword");

        Account account = verifyValidAccount(accountId);
        String passwordHash = Passwords.hash(clearTextPassword);
        setPasswordFields(account, passwordHash, passwordExpired, null);
        addLogEntry(account, AccountLogEntry.Action.PASSWORD_CHANGE, null, "Password updated");
    }

    /**
     * Set a temporary password on the account, also sets password expired to true.
     *
     * @param accountId             the account id
     * @param clearTextTempPassword the temporary password to set
     */
    public void setTemporaryPassword(final UUID accountId, final String clearTextTempPassword) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clearTextTempPassword),
                "missing clearTextTemporaryPassword");

        Account account = verifyValidAccount(accountId);
        setPasswordFields(account, account.getPasswordHash(), true, clearTextTempPassword);
        addLogEntry(accountId, AccountLogEntry.Action.PASSWORD_CHANGE, null, "Set temporary password");
    }

    /**
     * Set the password on an account and set the password expired to false/no. Clears any temporary password.
     *
     * @param accountId         the account id
     * @param clearTextPassword the password as supplied by the user
     */
    public void setPassword(final UUID accountId, final String clearTextPassword) {
        setPassword(accountId, clearTextPassword, false);
    }

    /**
     * Set the password expired flag on an account, retains the temporary password if set.
     *
     * @param accountId       the account id
     * @param passwordExpired the password expiration flag
     */
    public void setPasswordExpired(final UUID accountId, final boolean passwordExpired) {
        Preconditions.checkArgument(accountId != null, "missing account id");

        Account account = verifyValidAccount(accountId);
        setPasswordFields(account, account.getPasswordHash(), passwordExpired, account.getPasswordTemporary());

        String msg = String.format("Set password expired %B", passwordExpired);
        addLogEntry(account, AccountLogEntry.Action.PASSWORD_CHANGE, null, msg);
    }

    // helper to normalize the setting of password field processing.
    // does not:
    //  1. Perform argument validation (including does perform account verification)
    //  2. Create log entries, users of this method should do that.
    private void setPasswordFields(final Account account,
                                   final String passwordHash,
                                   final boolean passwordExpired,
                                   final String passwordTemporary) {
        try {
            accountGateway.setPasswordFieldsBlocking(account.getId(), passwordHash, passwordExpired, passwordTemporary);
        } catch (Throwable t) {
            log.error("an unexpected error occurred", t);
            throw Exceptions.propagate(t);
        }
    }

    /*
     *  Helpers
     *
     */

    private Subscription verifyValidSubscription(final UUID subscriptionId) throws IllegalArgumentException {
        Preconditions.checkArgument(subscriptionId != null, "missing subscription id");
        Subscription subscription = subscriptionService.find(subscriptionId).blockFirst();
        if (subscription == null) {
            throw new IllegalArgumentException("invalid subscription");
        }
        return subscription;
    }

    /**
     * Verify an Account by ID.
     *
     * @param accountId the account id
     * @return the account represented by that id
     * @throws IllegalArgumentException if the account id is invalid
     */
    Account verifyValidAccount(final UUID accountId) throws IllegalArgumentException {
        Preconditions.checkArgument(accountId != null, "missing account id");
        Account account = findById(accountId).blockFirst();
        if (account == null) {
            throw new IllegalArgumentException("invalid account");
        }
        return account;
    }

    private AccountAdapter verifyValidAccountWithIdentityById(final UUID accountId) throws IllegalArgumentException {
        Preconditions.checkArgument(accountId != null, "missing account id");
        AccountAdapter adapter = findWithIdentityById(accountId).blockFirst();
        if (adapter == null) {
            throw new IllegalArgumentException("invalid account");
        }
        return adapter;
    }

    private void verifyEmailNotInUse(final String email) throws ConflictException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(email), "missing email");
        if (findByEmail(email).blockFirst() != null) {
            throw new ConflictException("email address exists");
        }
    }

    /**
     * Parse the names from a provided LTI user
     *
     * @param ltiUser the LTI user
     * @return an array (of size 2) of [givenName, familyName]
     */
    public static String[] parseNamesFromLTIUser(final blackboard.blti.message.User ltiUser) {
        Preconditions.checkArgument(ltiUser != null, "missing LTI user");

        // parse out the names.
        String givenName = ltiUser.getGivenName();
        String familyName = ltiUser.getFamilyName();
        if (Strings.isNullOrEmpty(givenName) || Strings.isNullOrEmpty(familyName)) {
            // try to parse it from the fullname.
            String fullName = ltiUser.getFullName();
            if (!Strings.isNullOrEmpty(fullName)) {
                fullName = fullName.trim();
                String[] parts = fullName.split(" ", 2);
                if (parts.length == 2) {
                    givenName = parts[0];
                    familyName = parts[1];
                } else {
                    // give up.
                    givenName = fullName;
                }
            }
        }
        givenName = (givenName == null ? null : Strings.emptyToNull(givenName.trim()));
        familyName = (familyName == null ? null : Strings.emptyToNull(familyName.trim()));

        return new String[]{givenName, familyName};
    }

    /**
     * Parse the names from a provided LTI user
     *
     * @param account the user account
     * @return an @{code Flux} of an AccountIdentityAttributes
     */
    @Trace(async = true)
    public Flux<AccountIdentityAttributes> findIdentityByAccount(final Account account) {
        return accountGateway.findAccountIdentityById(account.getIamRegion(), account.getId())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find account {@link Account} associated with given bearer token. If token is expired, account will not be found.
     *
     * @param token a bearer token
     * @return Account if bearer token is valid and is not expired; null otherwise
     */
    @Deprecated
    public Account findByBearerTokenBlocking(final String token) {
        checkNotNull(token);

        Mono<Account> account = credentialGateway.findBearerToken(token)
                .flatMap(t -> findById(t.getAccountId()).singleOrEmpty());

        return account.block();
    }

    /**
     * Find account {@link Account} associated with given bearer token. If token is expired, account will not be found.
     *
     * @param token the bearer token to find the account with
     * @return a mono of account
     * @throws NotFoundFault when the account is not found, meaning the token is expired
     */
    @Trace(async = true)
    public Mono<Account> findByBearerToken(@Nonnull final String token) {
        return credentialGateway.findBearerToken(token)
                .flatMap(t -> findById(t.getAccountId())
                        .single()
                        .doOnError(NoSuchElementException.class, ex -> {
                            throw new NotFoundFault(String.format("account not found with token %s", token));
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find an account by bearer token
     *
     * @param token the bearer token to find the account with
     * @return a mono of account
     * @throws UnauthorizedFault when the account is not found for the bearer token (meaning the token is expired)
     */
    @Trace(async = true)
    public Mono<Account> findAccountByToken(final String token) {
        affirmArgument(token != null, "token is required");

        return findByBearerToken(token)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NotFoundFault.class, ex -> {
                    throw new UnauthorizedFault("invalid token");
                });
    }

    /**
     * Find an account by email
     *
     * @param email the email to find the account for
     * @return a mono of account
     * @throws UnauthorizedFault when the account is not found for the given email
     */
    @Trace(async = true)
    public Mono<Account> findAccountByEmail(@Nonnull final String email) {
        return findByEmail(email)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new UnauthorizedFault("invalid credentials");
                });
    }

    /**
     * Find an account by email
     *
     * @param email the email to find the account for
     * @return a mono of account or empty if does not exist
     */
    @Trace(async = true)
    public Mono<Account> findAccountByEmailOrEmpty(@Nonnull final String email) {
        return findByEmail(email)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the identity by account id
     *
     * @param id the account id
     * @return a {@link Mono} of account identity attributes
     */
    public Mono<AccountIdentityAttributes> findIdentityById(final UUID id) {
        return findById(id)
                .flatMap(one -> accountGateway.findAccountIdentityById(one.getIamRegion(), one.getId()))
                .singleOrEmpty();
    }

    /**
     * Fetch account info and return a payload object. Both {@link AccountIdentityAttributes} and
     * {@link AccountAvatar} are defaulted if an empty stream is returned. This keeps the
     * {@link Mono#zip(Mono, Mono, Mono)} from returning an empty stream and ensures the account payload is never
     * <code>null</code>
     *
     * @param account the {@link Account} object to get the payload for
     * @return a {@link AccountPayload}
     */
    @Trace(async = true)
    public Mono<AccountPayload> getAccountPayload(@Nonnull final Account account) {
        return Mono.zip(Mono.just(account), getPartialAccountAdapter(account))
                .map(tuple2 -> {
                    AccountAdapter accountAdapter = tuple2.getT2();
                    return AccountPayload.from(tuple2.getT1(), accountAdapter.getIdentityAttributes(),
                            accountAdapter.getAvatars().get(0), AuthenticationType.BRONTE);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch account info and return a payload object. If the {@link Account} is not found an empty stream is returned.
     *
     * @param accountId the id of the account to fetch
     * @return a {@link AccountPayload}
     */
    @Trace(async = true)
    public Mono<AccountPayload> getAccountPayload(final @Nonnull UUID accountId) {
        Mono<Account> account = findById(accountId).singleOrEmpty();

        return account.flatMap(this::getAccountPayload)
                .doOnSuccess((payload) -> {
                    if (payload == null) {
                        log.warn("Account not found for id {}", accountId);
                    }
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Maps a plugin collaborator to a {@link AccountCollaboratorPayload} object. The method intentionally returns a
     * {@link Mono#empty()} stream when the account payload is <code>null</code>. Only valid account should be returned.
     *
     * @param accountId       the account id of the collaborator
     * @param permissionLevel the permission level of the collaborator
     * @return a {@link Mono} of {@link AccountCollaboratorPayload}
     */
    @Trace(async = true)
    public Mono<AccountCollaboratorPayload> getCollaboratorPayload(final UUID accountId, final PermissionLevel permissionLevel) {
        Mono<AccountPayload> accountPayload = getAccountPayload(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return accountPayload.map(acc -> AccountCollaboratorPayload.from(acc, permissionLevel))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Maps an account object to a {@link AccountSummaryPayload}. The method calls internally
     * {@link AccountService#getPartialAccountAdapter(Account)} which returns a {@link Mono} of {@link Tuple2} containing
     * account identity attributes and account avatar.
     *
     * @param account the account to convert
     * @return a {@link Mono} of {@link AccountSummaryPayload}
     */
    public Mono<AccountSummaryPayload> getAccountSummaryPayload(@Nonnull final Account account) {
        return getPartialAccountAdapter(account)
                .map(accountAdapter -> AccountSummaryPayload.from(accountAdapter.getIdentityAttributes(),
                        accountAdapter.getAvatars().get(0)));
    }

    /**
     * Maps an accountId to a {@link AccountSummaryPayload}. The method calls internally
     * {@link AccountService#findById(UUID...)} which gets returns a {@link Flux<Account>} from which a {@link Mono<Account>} is got by calling single
     * which is passed to {@link AccountService#getPartialAccountAdapter(Account)} which returns a {@link Mono} of {@link Tuple2} containing
     * account identity attributes and account avatar.
     *
     * @param accountId the accountId to convert
     * @return a {@link Mono} of {@link AccountSummaryPayload}
     */
    @Trace(async = true)
    public Mono<AccountSummaryPayload> getAccountSummaryPayload(@Nonnull final UUID accountId) {
        return findById(accountId)
                .single()
                .map(account -> getPartialAccountAdapter(account)
                        .map(accountAdapter -> AccountSummaryPayload.from(accountAdapter.getIdentityAttributes(),
                                accountAdapter.getAvatars().get(0)))
                ).flatMap(accountSummaryPayloadMono -> accountSummaryPayloadMono)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Get a flux of account summaries payload from a flux of account ids. If for any reason the payload is not built
     * for a given account, the account is skipped and the exception logged. This method could return an empty flux but
     * will never error.
     *
     * @param collaboratorsLimit the limit of collaborators payload to get from the flux
     * @param collaboratorIds    the bollaborators id to get the glux for
     * @return a flux of account summaries
     */
    @Trace(async = true)
    public Flux<AccountSummaryPayload> getAccountSummaryPayloads(final int collaboratorsLimit, final Flux<UUID> collaboratorIds) {
        return collaboratorIds
                .take(collaboratorsLimit) // Limit responses as requested
                .flatMap(collaboratorId -> getAccountSummaryPayload(collaboratorId)
                        .onErrorResume(throwable -> {

                            if (log.isDebugEnabled()) {
                                log.debug("Error while fetching account summary payload for {} {}",
                                        collaboratorId, throwable.getMessage());
                            }

                            return Mono.empty();
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch account identity attributes and account avatar given an account object. Important to notice that this
     * method will return an account adapter where the only set values are:
     * <br>{@link AccountAdapter#getIdentityAttributes()} this is defaulted to an empty object when not found.
     * <br>{@link AccountAdapter#getAvatars()}. Get avatars will contain only the {@link AccountAvatar.Size#SMALL}
     * if found or otherwise defaulted to a list with an empty avatar object.
     * <br> This ensures the method will never throw a null pointer exception when the fields are accessed
     *
     * @param account the account to find identity and avatar for
     * @return a {@link Mono} of {@link AccountAdapter}
     */
    @Trace(async = true)
    public Mono<AccountAdapter> getPartialAccountAdapter(@Nonnull final Account account) {
        Mono<AccountAvatar> avatarMono = avatarGateway.findAvatarByAccountId(account.getIamRegion(), account.getId(),
                AccountAvatar.Size.SMALL)
                .defaultIfEmpty(new AccountAvatar())
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<AccountIdentityAttributes> identityMono = findIdentityByAccount(account)
                .defaultIfEmpty(new AccountIdentityAttributes())
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());

        return Mono.zip(identityMono, avatarMono)
                .map(one -> new AccountAdapter()
                        .setIdentityAttributes(one.getT1())
                        .setAvatars(Lists.newArrayList(one.getT2())));
    }


    /**
     * Migrates an account to an existing subscription and assign the account the roles passed as argument.
     *
     * @param account        the account to migrate
     * @param subscriptionId the id of the subscription to migrate the account to
     * @param roles          the new roles to assign to the account in the new subscription
     * @return a {@link Mono} of {@link Account} with the updated subscription id
     * @throws IllegalArgumentException when either the subscriptionId is null or the roles field is either null or empty
     */
    public Mono<Account> migrateAccountTo(@Nonnull final Account account, final UUID subscriptionId, final Set<AccountRole> roles)
            throws IllegalArgumentException {
        checkArgument(account.getSubscriptionId() != null, "subscriptionId is required");
        checkArgument(roles != null && !roles.isEmpty(),
                "at least 1 role required");
        // keep the subscription id of the account
        accountGateway.updateSubscriptionAndRoles(account, subscriptionId, roles)
                .doOnError(t -> {
                    log.error("Error while migrating account `{}` to subscription `{}` {}",
                            account.getId(), subscriptionId, t.getMessage());
                    throw Exceptions.propagate(t);
                }).blockLast();

        return findById(account.getId()).singleOrEmpty();
    }

    /**
     * Find an account by a Federated login.
     *
     * @param subscriptionId the subscription id
     * @param clientId       remote client id (or the IDP id)
     * @param subjectId      Locally unique and never reassigned identifier within the Issuer for the End-User
     * @return the account or {@link Mono#empty()} if not found
     */
    public Mono<Account> findByFederation(@Nonnull final UUID subscriptionId,
                                          @Nonnull final String clientId,
                                          @Nonnull final String subjectId) {

        affirmNotNull(subscriptionId, "missing subscriptionId");
        affirmArgumentNotNullOrEmpty(clientId, "missing clientId");
        affirmArgumentNotNullOrEmpty(subjectId, "missing subjectId");

        return federationGateway.fetchByFederation(subscriptionId, clientId, subjectId)
                .map(FederatedIdentity::getAccountId)
                .flatMapMany(this::findById)
                .singleOrEmpty();
    }

    /**
     * Add an association between a federated system and ours
     *
     * @param subscriptionId the subscription id
     * @param clientId       remote client id (or the IDP id)
     * @param subjectId      Locally unique and never reassigned identifier within the Issuer for the End-User
     * @param accountId      the account id
     */
    public Mono<FederatedIdentity> addFederation(@Nonnull final UUID subscriptionId,
                                                 @Nonnull final String clientId,
                                                 @Nonnull final String subjectId,
                                                 @Nonnull final UUID accountId) {

        affirmNotNull(subscriptionId, "missing subscriptionId");
        affirmArgumentNotNullOrEmpty(clientId, "missing clientId");
        affirmArgumentNotNullOrEmpty(subjectId, "missing subjectId");
        affirmNotNull(accountId, "missing accountId");

        FederatedIdentity identity = new FederatedIdentity()
                .setSubscriptionId(subscriptionId)
                .setClientId(clientId)
                .setSubjectId(subjectId)
                .setAccountId(accountId);

        return federationGateway.persist(identity)
                .then(Mono.just(identity));
    }

    /**
     * Find the pearsonUid for an account id
     *
     * @param accountId the account id to find the pearson id for
     * @return a mono with the pearsonUid or an empty stream when not found
     */
    public Mono<String> findIESId(final UUID accountId) {
        return iesAccountTrackingGateway.findIesUserId(accountId)
                .map(IESAccountTracking::getIesUserId);
    }

    /**
     * Given any of the following names, derive what the given and family names should be
     *
     * @param given the given name, can be null
     * @param family the family name, can be null
     * @param full the full name (i.e. given + family), can be null
     * @return an array (of size 2) of [givenName, familyName]
     */
    public String[] deriveGivenAndFamilyNames(final String given,
                                              final String family,
                                              final String full) {
        String retGiven = given;
        String retFamily = family;
        //
        if (isNullOrEmpty(given) && isNullOrEmpty(family)) {
            // try to parse it from the full.
            if (!isNullOrEmpty(full)) {
                String fullName = full.trim();
                // this is a naive approach; but needs to handle cases like:
                //   Kelly Van Der Beek -> ["Kelly", "Van Der Beek"]
                String[] parts = fullName.split(" ", 2);
                if (parts.length == 2) {
                    retGiven = parts[0];
                    retFamily = parts[1];
                } else {
                    // give up.
                    retGiven = fullName;
                }
            }
        }
        // trim.
        retGiven = (retGiven == null ? null : Strings.emptyToNull(retGiven.trim()));
        retFamily = (retFamily == null ? null : Strings.emptyToNull(retFamily.trim()));

        return new String[]{retGiven, retFamily};
    }

    /**
     * Set the password on an account and set the password expired to false/no. Clears any temporary password.
     * Doesn't use blocking
     *
     * @param accountId         the account id
     * @param clearTextPassword the password as supplied by the user
     */
    @Trace(async = true)
    public Flux<Void> setAccountPassword(final UUID accountId, final String clearTextPassword) {
        return setAccountPassword(accountId, clearTextPassword, false)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Set the password on an account, clearing the temporary password.
     * Doesn't use blocking
     *
     * @param accountId         the account id
     * @param clearTextPassword the password as supplied by the user
     * @param passwordExpired   the password expiration flag
     */
    @Trace(async = true)
    public Flux<Void> setAccountPassword(final UUID accountId, final String clearTextPassword, final boolean passwordExpired) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clearTextPassword), "missing clearTextPassword");

        Account account = verifyValidAccount(accountId);
        String passwordHash = Passwords.hash(clearTextPassword);
        return Flux.merge(
            setAccountPasswordFields(account, passwordHash, passwordExpired, null),
            addAccountLogEntry(account, AccountLogEntry.Action.PASSWORD_CHANGE, null, "Password updated")
        ).doOnEach(ReactiveTransaction.linkOnNext());
    }

    // helper to normalize the setting of password field processing.
    // does not:
    //  1. Perform argument validation (including does perform account verification)
    //  2. Create log entries, users of this method should do that.
    // Does not use blocking
    @Trace(async = true)
    private Flux<Void> setAccountPasswordFields(final Account account,
                                                final String passwordHash,
                                                final boolean passwordExpired,
                                                final String passwordTemporary) {
        return accountGateway.setPasswordFields(account.getId(), passwordHash, passwordExpired, passwordTemporary);
    }


    /**
     * Add a log entry for an account, without using blocking
     *
     * @param account    the account
     * @param action     the action taken
     * @param onBehalfOf if a user performed an action on behalf of this account (optional)
     * @param message    the message
     */
    @Trace(async = true)
    Flux<Void> addAccountLogEntry(final Account account,
                                  final AccountLogEntry.Action action,
                                  @Nullable final UUID onBehalfOf,
                                  final String message) {
        AccountLogEntry entry = new AccountLogEntry().setAccountId(account.getId())
                .setIamRegion(account.getIamRegion())
                .setId(UUIDs.timeBased())
                .setAction(action)
                .setOnBehalfOf(onBehalfOf)
                .setMessage(message);
        return accountGateway.persist(entry);
    }

    /**
     * Verify the provided password matches the accounts password
     *
     * @param clearTextPassword the password as supplied by the user
     * @Param passwordHash      the existing password hash
     * @return true if the password verifies, false otherwise
     */
    public boolean verifyPassword(final String clearTextPassword, final String passwordHash) {
        affirmArgument(!Strings.isNullOrEmpty(clearTextPassword), "missing clearTextPassword");
        // don't allow verification with no password (as it can be null underlying)
        affirmArgument(!Strings.isNullOrEmpty(passwordHash), "missing passwordHash");

        return Passwords.verify(clearTextPassword, passwordHash);
    }


    /**
     * Add a shadow attribute to an account.
     *
     * @param accountId the account id
     * @param accountShadowAttributeName      the name of the shadow attribute
     * @param value     the value of the shadow attribute
     * @param source    the source of this information
     */
    public Flux<Void> addShadowAttribute(final UUID accountId,
                                         final AccountShadowAttributeName accountShadowAttributeName,
                                         final String value,
                                         final AccountShadowAttributeSource source) {
        affirmNotNull(accountShadowAttributeName, "missing accountShadowAttributeName");
        affirmNotNull(accountId, "missing accountId");
        affirmNotNull(value, "missing value");
        affirmNotNull(value, "missing accountShadowAttributeSource");

        return accountGateway.findAccountById(accountId)
                .flatMap(account -> {
                    return accountGateway.persist(new AccountShadowAttribute().setAccountId(account.getId())
                                                          .setAttribute(accountShadowAttributeName)
                                                          .setIamRegion(account.getIamRegion())
                                                          .setValue(value)
                                                          .setSource(ImmutableMap.of(UUIDs.timeBased(), source)));
                });
    }

    /**
     * Deletes all the shadow attributes for the given account id
     * @param region account region
     * @param accountId account id
     * @param accountShadowAttribute account shadow attribute
     */
    public Flux<Void> deleteShadowAttributes(final Region region, final UUID accountId, final AccountShadowAttributeName accountShadowAttribute) {
        affirmNotNull(region, "missing region");
        affirmNotNull(accountId, "missing accountId");
        affirmNotNull(accountShadowAttribute, "missing accountShadowAttribute");
        return accountGateway.deleteShadowAttributes(region, accountId, accountShadowAttribute);
    }
}
