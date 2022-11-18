package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.util.Enums;

class LearnerRedirectByKeyMutator extends SimpleTableMutator<LearnerRedirect> {

    @Override
    public String getUpsertQuery(final LearnerRedirect mutation) {
        return "INSERT INTO learner_redirect.by_key ("
                + "  redirect_type"
                + ", redirect_key"
                + ", id"
                + ", version"
                + ", destination_path"
                + ") VALUES (?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final LearnerRedirect mutation) {
        stmt.bind(Enums.asString(mutation.getType()),
                  mutation.getKey(),
                  mutation.getId(),
                  mutation.getVersion(),
                  mutation.getDestinationPath());
    }

    @Override
    public String getDeleteQuery(LearnerRedirect mutation) {
        return "DELETE FROM learner_redirect.by_key" +
                " WHERE redirect_type = ?" +
                " AND redirect_key = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, LearnerRedirect mutation) {
        stmt.bind(Enums.asString(mutation.getType()), mutation.getKey());
    }
}
