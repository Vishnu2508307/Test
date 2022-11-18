package com.smartsparrow.ext_http.route;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;

import com.smartsparrow.ext_http.wiring.ExternalHttpConfig;

public class ExternalHttpRoute extends RouteBuilder {

    public static final String SUBMIT_REQUEST = "direct:submit_request";
    public static final String RETRY_QUEUE = "direct:submit_retry_queue";

    @Inject
    private ExternalHttpConfig externalHttpConfig;

    public void configure() {

        // Note: Use Dynamic destinations as these AWS endpoints are managed by Terraform.

        // process events sent to the "Submit" topic.
        from(SUBMIT_REQUEST)
                .toD("aws-sns://" + externalHttpConfig.getSubmitTopicNameOrArn());

        // process messages to be sent to the retry/delay queue
        from(RETRY_QUEUE)
                .toD("aws-sqs://" + externalHttpConfig.getDelayQueueNameOrArn());

    }

}
