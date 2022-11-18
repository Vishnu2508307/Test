package com.smartsparrow.export.route;

import static com.smartsparrow.dataevent.RouteUri.DIRECT;

import javax.inject.Inject;

import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.component.jackson.JacksonDataFormat;

import com.smartsparrow.export.data.ExportRequestNotification;

public class CoursewareExportProducerRoute extends CoursewareExportRoute {

    public static final String SUBMIT_EXPORT_REQUEST = "submit-export_request";

    private static final String EXPORT_REQUEST_NOTIFICATION_ENRICHER = "direct:export-request-notification-enricher";
    private static final String SNS_OUTPUT_OFFLOADER = "direct:sns-output-extended-payload-offloader";
    private static final String S3_KEY = "s3Key";
    public static final String NOTIFICATION_ATTRIBUTE = "notificationId";
    public static final String EXPORT_ATTRIBUTE = "exportId";
    private static final String EXPORT_ERROR = "export.error";


    @Inject
    private ExportRequestNotificationEnricher exportRequestNotificationEnricher;

    public void configure() {

        // prep mappers form json un/marshalling
        final JacksonDataFormat requestNotificationMapper = new JacksonDataFormat(ExportRequestNotification.class);

        // process events sent to the "Submit" topic.
        from("reactive-streams:" + SUBMIT_EXPORT_REQUEST)
                .onException(Throwable.class)
                .to(DIRECT + EXPORT_ERROR)
                .stop()
                .end()
                .setHeader(NOTIFICATION_ATTRIBUTE, simple("${body.notificationId.toString()}"))
                .setHeader(EXPORT_ATTRIBUTE, simple("${body.exportId.toString()}"))
                // set the notification attribute for the error route
                .setProperty(NOTIFICATION_ATTRIBUTE, simple("${body.notificationId}"))
                .enrich(EXPORT_REQUEST_NOTIFICATION_ENRICHER, new NewestExchangeAggregationStrategy())
                .marshal(requestNotificationMapper)
                .enrich(SNS_OUTPUT_OFFLOADER, new NewestExchangeAggregationStrategy())
                .toD("aws-sns://" + exportConfig.getSubmitTopicNameOrArn());

        // enrich the object.
        from(EXPORT_REQUEST_NOTIFICATION_ENRICHER)
                .bean(exportRequestNotificationEnricher);

        // enrich the produced notification
        from(SNS_OUTPUT_OFFLOADER)
                .choice()
                // check if the body is too big for SNS -> 131072 == 128 * 1024 == 128kb
                .when(simple("${body.length} >= 131072"))
                    .setHeader(S3_KEY, simple("${in.header.exportId}/${in.header.notificationId}/out.json"))
                    // write contents to S3
                    .setHeader(S3Constants.KEY, simple("${in.header.s3key}"))
                    .setHeader(S3Constants.CONTENT_TYPE, simple("${in.header.Content-Type}"))
                    .toD("aws-s3://" + exportConfig.getSnippetBucketName())
                    // set body to an empty json (string field), so it fits in the wire and triggers the lambda
                    .setBody(constant("{}"));
    }

}
