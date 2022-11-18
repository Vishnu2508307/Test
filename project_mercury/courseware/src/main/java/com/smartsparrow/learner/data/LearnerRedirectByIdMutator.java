package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.util.Enums;

class LearnerRedirectByIdMutator extends SimpleTableMutator<LearnerRedirect> {

    @Override
    public String getUpsertQuery(final LearnerRedirect mutation) {
        return "INSERT INTO learner_redirect.by_id ("
                + "  id"
                + ", version"
                + ", redirect_type"
                + ", redirect_key"
                + ", destination_path"
                + ") VALUES (?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final LearnerRedirect mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getVersion(),
                  Enums.asString(mutation.getType()),
                  mutation.getKey(),
                  mutation.getDestinationPath());
    }

}
