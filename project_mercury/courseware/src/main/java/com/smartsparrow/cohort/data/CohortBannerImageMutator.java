package com.smartsparrow.cohort.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortBannerImageMutator  extends SimpleTableMutator<CohortBannerImage> {

    @Override
    public String getUpsertQuery(CohortBannerImage mutation) {
        return "INSERT INTO cohort.banner_image_by_cohort_size (" +
                "cohort_id, " +
                "size, " +
                "banner_image) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CohortBannerImage mutation) {
        stmt.bind(mutation.getCohortId(),
                mutation.getSize().name(),
                mutation.getBannerImage());
    }
}
