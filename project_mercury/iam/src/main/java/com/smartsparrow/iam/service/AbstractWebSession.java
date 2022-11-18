package com.smartsparrow.iam.service;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractWebSession<T extends WebToken> implements WebSession<T> {

    final Account account;

    protected AbstractWebSession(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractWebSession<?> that = (AbstractWebSession<?>) o;
        return Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    @Override
    public String toString() {
        return "AbstractWebSession{" +
                "account=" + account +
                '}';
    }
}
