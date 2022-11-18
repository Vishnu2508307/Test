package com.smartsparrow.learner.route;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;

import com.smartsparrow.courseware.route.aggregation.CSGBatchAggregationStrategy;
import com.smartsparrow.courseware.route.aggregation.CSGBatchSizePredicate;
import com.smartsparrow.courseware.wiring.CsgConfig;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.learner.searchable.CsgIndexRequestBuilder;

public class CSGIndexRoute extends RouteBuilder {

    public static final String CSG_SUBMIT_REQUEST = "csg_submit_request";

    @Inject
    CsgConfig csgConfig;

    @Inject
    ExternalHttpRequestService externalHttpRequestService;

    @Override
    public void configure() throws Exception {

        // CSG document update in a batch, if documents are less than batch size then wait for completion time
        from("reactive-streams:" + CSG_SUBMIT_REQUEST)
                .aggregate(simple("${body.getCorrelationId}"), new CSGBatchAggregationStrategy())
                .completionPredicate(new CSGBatchSizePredicate(csgConfig.getBatchSize()))
                .completionTimeout(csgConfig.getCompletionTime())
                .process(exchange -> {
                    CsgIndexRequestBuilder csgBuilder = exchange.getIn().getBody(CsgIndexRequestBuilder.class);

                    exchange.getIn().setHeader("requestPurpose", RequestPurpose.CSG_INDEX);
                    exchange.getIn().setBody(csgBuilder.build());
                })
                .bean(externalHttpRequestService);
    }
}
