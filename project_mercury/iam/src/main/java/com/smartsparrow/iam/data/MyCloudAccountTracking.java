package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

public class MyCloudAccountTracking {

    private UUID accountId;
    private String myCloudUserId;

    public UUID getAccountId() {
        return accountId;
    }

    public MyCloudAccountTracking setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getMyCloudUserId() {
        return myCloudUserId;
    }

    public MyCloudAccountTracking setMyCloudUserId(String myCloudUserId) {
        this.myCloudUserId = myCloudUserId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyCloudAccountTracking that = (MyCloudAccountTracking) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(myCloudUserId, that.myCloudUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, myCloudUserId);
    }

    @Override
    public String toString() {
        return "MyCloudAccountTracking{" +
                "accountId=" + accountId +
                ", myCloudUserId='" + myCloudUserId + '\'' +
                '}';
    }
}
