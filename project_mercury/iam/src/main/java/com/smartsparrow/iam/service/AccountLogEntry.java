package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

/**
 * Log entries related to IAM, for auditing purposes.
 *
 * WARNING: The message may contain PII, log this information carefully.
 *
 * Example:
 *  - User X updated PII
 *  - Support Sam (on behalf of) login as User Y
 */
public class AccountLogEntry {

    public enum Action {
    	//
        ACCOUNT_INFO_REQUESTED,
        ACCOUNT_VALIDATED,
        //
        BEARER_TOKEN_GENERATED,

        //
        EMAIL_ADD,
        EMAIL_VERIFY_SENT,
        EMAIL_VERIFIED,

        //
        LOGIN,
        LOGIN_LTI,
        LOGOUT,

        //
        PASSWORD_CHANGE,
        PASSWORD_RESET,
        PASSWORD_RESET_SENT,

        //
        PII_CHANGE,

        //
        PROVISION,
        PROVISION_FROM_AICC,
        PROVISION_FROM_LTI,
        PROVISION_FROM_MIGRATION,

        //
        REGION_CHANGE,
        REGION_CHANGE_BEGIN,
        REGION_CHANGE_COMPLETE,

        //
        ROLE_CHANGE,

        //
        STATUS_CHANGE,

        //
        SUBSCRIPTION_CHANGE,

        //
        ERROR

        // WARNING: CHANGES HERE ALSO NEED TO BE DONE ON THE AELP.
    }

    // the account identifier
    private UUID accountId;

    // the iam data region
    private Region iamRegion;

    // the id of the log entry
    private UUID id;

    // the type of action performed
    private Action action;

    // (Optional) the account id performing the action on behalf of this account
    private UUID onBehalfOf;

    // (Optional) a freeform message about the action
    private String message;

    public AccountLogEntry() {
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountLogEntry setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public Region getIamRegion() {
        return iamRegion;
    }

    public AccountLogEntry setIamRegion(Region iamRegion) {
        this.iamRegion = iamRegion;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public AccountLogEntry setId(UUID id) {
        this.id = id;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public AccountLogEntry setAction(Action action) {
        this.action = action;
        return this;
    }

    public UUID getOnBehalfOf() {
        return onBehalfOf;
    }

    public AccountLogEntry setOnBehalfOf(UUID onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public AccountLogEntry setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountLogEntry that = (AccountLogEntry) o;
        return Objects.equals(accountId, that.accountId) && iamRegion == that.iamRegion && Objects.equals(id, that.id)
                && action == that.action && Objects.equals(onBehalfOf, that.onBehalfOf) && Objects
                .equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, iamRegion, id, action, onBehalfOf, message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("accountId", accountId).add("id", id).add("action", action)
                .add("onBehalfOf", onBehalfOf).add("message", message).toString();
    }
}
