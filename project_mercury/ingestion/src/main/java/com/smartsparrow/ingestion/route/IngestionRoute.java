package com.smartsparrow.ingestion.route;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import com.smartsparrow.ingestion.wiring.IngestionConfig;

public class IngestionRoute extends RouteBuilder {

    public static final String RS = "reactive-streams:";
    public static final String SUBMIT_INGESTION_AMBROSIA_REQUEST = "submit_ambrosia_request";
    public static final String SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST = "submit_adapter_epub_request";
    public static final String SUBMIT_INGESTION_ADAPTER_DOCX_REQUEST = "ingestion-adapter-docx-submit";
    public static final String SUBMIT_INGESTION_CANCEL_REQUEST = "ingestion-cancel-submit";

    @Inject
    private IngestionConfig ingestionConfig;

    @Override
    public void configure() {

        // Note: Use Dynamic destinations as these AWS endpoints are managed by Terraform.

        // process events sent to the "Submit" topic.
        from(RS + SUBMIT_INGESTION_AMBROSIA_REQUEST)
                .routeId(SUBMIT_INGESTION_AMBROSIA_REQUEST)
//              TODO: 11/6/21 the below did not work, removed (29-36) to unblock testing
                // add the token from the body to the header
//                .process(exchange -> {
//                    IngestionSummaryEventMessage message = exchange.getIn().getBody(IngestionSummaryEventMessage.class);
//                    exchange.getOut().setHeader("bearerToken", message.getBearerToken());
//                    exchange.getOut().setBody(message.getIngestionSummary());
//                })
//                // set the token to the authorization header
//                .setHeader("bearerToken", simple("${in.header.token}"))
                .marshal().json(JsonLibrary.Jackson)
                .toD("aws-sqs://" + ingestionConfig.getAmbrosiaIngestionQueueNameOrArn());


        from(RS + SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST)
                .routeId(SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST)
                .marshal().json(JsonLibrary.Jackson)
                .toD("aws-sqs://" + ingestionConfig.getAdapterEpubQueueNameOrArn());


        from(RS + SUBMIT_INGESTION_ADAPTER_DOCX_REQUEST)
                .routeId(SUBMIT_INGESTION_ADAPTER_DOCX_REQUEST)
                .marshal().json(JsonLibrary.Jackson)
                .toD("aws-sqs://" + ingestionConfig.getAdapterDocxQueueNameOrArn());

        from(RS + SUBMIT_INGESTION_CANCEL_REQUEST)
                .routeId(SUBMIT_INGESTION_CANCEL_REQUEST)
                .marshal().json(JsonLibrary.Jackson)
                .toD("aws-sqs://" + ingestionConfig.getIngestionCancelQueueNameOrArn());
    }
}
