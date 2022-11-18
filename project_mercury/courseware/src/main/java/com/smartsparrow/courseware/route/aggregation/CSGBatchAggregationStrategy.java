package com.smartsparrow.courseware.route.aggregation;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.smartsparrow.learner.searchable.CsgIndexRequestBuilder;

public class CSGBatchAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {

        CsgIndexRequestBuilder newBody = newExchange.getIn().getBody(CsgIndexRequestBuilder.class);

        if (oldExchange == null) {
            return newExchange;
        }
        CsgIndexRequestBuilder oldBody = oldExchange.getIn().getBody(CsgIndexRequestBuilder.class);
        oldBody.getFields().addAll(newBody.getFields());
        oldExchange.getIn().setBody(oldBody);

        return oldExchange;
    }
}
