package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

/**
 * Track events which occur on accounts (not what was done);
 *
 * For example:
 *  - the user endorsed a lesson.
 *  -
 */
public class AccountAction {

    public enum Action {
        ACCOUNT_VALIDATED,
        AUTHOR_HELP,
        ENDORSED_WS_LESSON,
        OPENED_AUTHOR,
        OPENED_BEST_WEBSITE,
        OPENED_PREVIEW,
        OPENED_WS,
        OPENED_WS_ANALYTICS,
        OPENED_WS_LESSON,
        OPENED_WS_LOOP,
        OPENED_WS_LOOPED_LESSON,
        TOUR_AUTHOR,
        TOUR_INTRO,
        TOUR_WS,
        CREATED;
    }

    // the account id
    private UUID accountId;
    // the action performed
    private Action action;
    // a time uuid of when it happened.
    private UUID id;

    public AccountAction() {
    }

    public UUID getAccountId() {
        return accountId;
    }

    public AccountAction setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public AccountAction setAction(Action action) {
        this.action = action;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public AccountAction setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountAction that = (AccountAction) o;
        return Objects.equals(accountId, that.accountId) && action == that.action && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, action, id);
    }

    /*
     * Note: Added a timestamp convert of the id.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("accountId", accountId).add("action", action).add("id", id)
                .add("id.timestamp", id.timestamp()).toString();
    }
}
