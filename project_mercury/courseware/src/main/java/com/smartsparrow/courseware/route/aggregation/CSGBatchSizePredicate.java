package com.smartsparrow.courseware.route.aggregation;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.commons.collections.CollectionUtils;

import com.smartsparrow.learner.searchable.CsgIndexRequestBuilder;
import com.smartsparrow.learner.searchable.LearnerSearchableDocumentIndexBodyWrapper;

public class CSGBatchSizePredicate implements Predicate {

    public int size;

    public CSGBatchSizePredicate(Integer size) {
        this.size = size;
    }

    @Override
    public boolean matches(Exchange exchange) {
        if (exchange != null) {
            List<LearnerSearchableDocumentIndexBodyWrapper> list = exchange.getIn().getBody(
                    CsgIndexRequestBuilder.class).getFields();
            if ( CollectionUtils.isNotEmpty(list) && list.size() == size) {
                return true;
            }
        }
        return false;
    }
}
