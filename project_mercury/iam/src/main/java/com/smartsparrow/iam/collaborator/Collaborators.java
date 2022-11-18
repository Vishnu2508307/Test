package com.smartsparrow.iam.collaborator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;

public class Collaborators {

    private List<TeamCollaboratorPayload> teams;
    private List<AccountCollaboratorPayload> accounts;

    public List<TeamCollaboratorPayload> getTeams() {
        return teams;
    }

    public Collaborators setTeams(List<TeamCollaboratorPayload> teams) {
        this.teams = teams;
        return this;
    }

    public List<AccountCollaboratorPayload> getAccounts() {
        return accounts;
    }

    public Collaborators setAccounts(List<AccountCollaboratorPayload> accounts) {
        this.accounts = accounts;
        return this;
    }

    public Collaborators add(CollaboratorPayload collaborator) {
        if (collaborator instanceof TeamCollaboratorPayload) {
            if (teams == null) {
                teams = new ArrayList<>();
            }
            teams.add((TeamCollaboratorPayload) collaborator);
        } else {
            if (accounts == null) {
                accounts = new ArrayList<>();
            }
            accounts.add((AccountCollaboratorPayload) collaborator);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collaborators that = (Collaborators) o;
        return Objects.equals(teams, that.teams) &&
                Objects.equals(accounts, that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teams, accounts);
    }

    @Override
    public String toString() {
        return "Collaborators{" +
                "teams=" + teams +
                ", accounts=" + accounts +
                '}';
    }
}
