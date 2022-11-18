package com.smartsparrow.rtm.message.recv.courseware;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListAccountSummaryMessage extends ReceivedMessage{

    private List<UUID> accountIds;

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListAccountSummaryMessage that = (ListAccountSummaryMessage) o;
        return Objects.equals(accountIds, that.accountIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds);
    }

    @Override
    public String toString() {
        return "ListAccountSummaryMessage{" +
                "accountIds=" + accountIds +
                '}';
    }
}
