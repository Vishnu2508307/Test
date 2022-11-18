package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ThemeByAccount {

    private UUID accountId;
    private UUID themeId;

    public UUID getAccountId() {
        return accountId;
    }

    public ThemeByAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getThemeId() {
        return themeId;
    }

    public ThemeByAccount setThemeId(UUID themeId) {
        this.themeId = themeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeByAccount that = (ThemeByAccount) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(themeId, that.themeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, themeId);
    }

    @Override
    public String toString() {
        return "ThemeByAccount{" +
                "accountId=" + accountId +
                ", themeId=" + themeId +
                '}';
    }
}
