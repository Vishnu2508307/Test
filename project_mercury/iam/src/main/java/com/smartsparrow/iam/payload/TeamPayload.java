package com.smartsparrow.iam.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamPayload {

    private UUID teamId;
    private String name;
    @JsonProperty("accountSummaries")
    private List<AccountSummaryPayload> accountSummaryPayloads;
    private Integer count;

    /**
     * Method to build the TeamPayload
     * @param teamId - Team id
     * @param name - Team name
     * @param accountSummaryPayloads - contains the summary of the accounts belonging to a team
     * @param count - total count of accounts that belong to the team
     * @return
     */
    public static TeamPayload from(@Nonnull UUID teamId,
                                   @Nonnull String name,
                                   @Nonnull List<AccountSummaryPayload> accountSummaryPayloads,
                                   @Nonnull Integer count){
        return new TeamPayload()
                .setTeamId(teamId)
                .setName(name)
                .setAccountSummaryPayloads(accountSummaryPayloads)
                .setCount(count);
    }

    public UUID getTeamId() {
        return teamId;
    }

    public TeamPayload setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public String getName() {
        return name;
    }

    public TeamPayload setName(String name) {
        this.name = name;
        return this;
    }

    public List<AccountSummaryPayload> getAccountSummaryPayloads() {
        return accountSummaryPayloads;
    }

    public TeamPayload setAccountSummaryPayloads(List<AccountSummaryPayload> accountSummaryPayloads) {
        this.accountSummaryPayloads = accountSummaryPayloads;
        return this;
    }


    public Integer getCount() {
        return count;
    }

    public TeamPayload setCount(Integer count) {
        this.count = count;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamPayload that = (TeamPayload) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(accountSummaryPayloads, that.accountSummaryPayloads) &&
                Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, name, accountSummaryPayloads, count);
    }

    @Override
    public String toString() {
        return "TeamPayload{" +
                "id=" + teamId +
                ", name='" + name + '\'' +
                ", accountSummaryPayloads=" + accountSummaryPayloads +
                ", count=" + count +
                '}';
    }
}
