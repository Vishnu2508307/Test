package com.smartsparrow.iam;

import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountIdentityAttributes;

/**
 * Stubber for common IAM data.
 */
public class IamDataStub {

    public static final UUID STUDENT_A_ID = UUIDs.timeBased();
    public static final Account STUDENT_A = new Account().setId(STUDENT_A_ID);

    public static final UUID INSTRUCTOR_A_ID = UUIDs.timeBased();
    public static final Account INSTRUCTOR_A = new Account().setId(INSTRUCTOR_A_ID);

    //
    public static final List<UUID> ALL_STUDENT_IDS = Lists.newArrayList(STUDENT_A_ID);
    public static final List<Account> ALL_STUDENTS = Lists.newArrayList(STUDENT_A);

    public static AccountIdentityAttributes buildAttributes() {
        return new AccountIdentityAttributes()
                .setAccountId(UUID.randomUUID())
                .setPrimaryEmail("dev@dev.dev")
                .setGivenName("Homer")
                .setFamilyName("Simpson");
    }
}
