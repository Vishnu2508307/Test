package com.smartsparrow.export.route;

import static com.smartsparrow.dataevent.RouteUri.DIRECT;
import static org.apache.camel.component.aws.sqs.SqsConstants.MESSAGE_ATTRIBUTES;

import java.util.Map;

import javax.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.util.CastUtils;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.service.ExportErrorQueueHandler;
import com.smartsparrow.export.service.ExportResultQueueHandler;
import com.smartsparrow.export.wiring.ExportConfig;
import com.smartsparrow.export.wiring.SnippetsStorage;
import com.smartsparrow.service.S3ClientService;

public class CoursewareExportConsumerRoute extends CoursewareExportRoute {

    private static final String SQS_INPUT_ENRICHER = "direct:sqs-input-extended-payload-enricher";
    private static final String S3_KEY = "s3Key";
    public static final String NOTIFICATION_ATTRIBUTE = "notificationId";
    private static final String EXPORT_ERROR = "export.error";

    @Inject
    private ExportConfig exportConfig;

    @Inject
    private ExportResultQueueHandler resultQueueHandler;

    @Inject
    private ExportErrorQueueHandler errorQueueHandler;

    @Inject
    private S3ClientService s3ClientService;

    public void configure() {

        // prep mappers form json un/marshalling
        final JacksonDataFormat resultNotificationMapper = new JacksonDataFormat(ExportResultNotification.class);
        final JacksonDataFormat errorNotificationMapper = new JacksonDataFormat(ExportErrorNotification.class);

        // Consume export snippet results
        // Need Camel 3 to add "?autoCreateQueue=false param
        String queueQueryParams = String.format("?messageAttributeNames=All&attributeNames=All&concurrentConsumers=%s&maxMessagesPerPoll=%s&waitTimeSeconds=%s",
        exportConfig.getConcurrentConsumers(), exportConfig.getMaxMessagesPerPoll(), exportConfig.getWaitTimeSeconds());
        from("aws-sqs:" + exportConfig.getResultQueueName() + queueQueryParams)
                .onException(Throwable.class)
                    .to(DIRECT + EXPORT_ERROR)
                    .stop()
                .end()
                .log(LoggingLevel.INFO, "Received export snippet result: ${body}")
                .setProperty(MESSAGE_ATTRIBUTES, simple("${in.header.CamelAwsSqsMessageAttributes}"))
                .enrich(SQS_INPUT_ENRICHER, new NewestExchangeAggregationStrategy())
                .unmarshal(resultNotificationMapper)
                // set the notification attribute for the error route
                .setProperty(NOTIFICATION_ATTRIBUTE, simple("${body.notificationId}"))
                .bean(resultQueueHandler);

        // Consume export snippet errors
        from("aws-sqs:" + exportConfig.getErrorQueueName() + queueQueryParams)
                .onException(Throwable.class)
                    .to(DIRECT + EXPORT_ERROR)
                    .stop()
                .end()
                .log(LoggingLevel.INFO, "Received export snippet ERROR: ${body}")
                .setProperty(MESSAGE_ATTRIBUTES, simple("${in.header.CamelAwsSqsMessageAttributes}"))
                .enrich(SQS_INPUT_ENRICHER, new NewestExchangeAggregationStrategy())
                .unmarshal(errorNotificationMapper)
                // set the notification attribute for the error route
                .setProperty(NOTIFICATION_ATTRIBUTE, simple("${body.notificationId}"))
                .bean(errorQueueHandler);

        // enrich the consumed notification
        from(SQS_INPUT_ENRICHER)
                // if there is an s3Key message attribute, set it as a header on the incoming message
                .process(exchange -> {
                    Map<Object, Object> attributes = CastUtils.cast(exchange.getIn().getHeader(MESSAGE_ATTRIBUTES, Map.class));
                    if (attributes.containsKey(S3_KEY)) {
                        exchange.getIn().setHeader(S3_KEY, attributes.get(S3_KEY));
                    }
                })
                .choice()
                    .when(exchange -> exchange.getIn().getHeader(S3_KEY, String.class) != null
                            && exportConfig.getSnippetsStorage() != SnippetsStorage.S3)
                    // an s3Key header exists and if the snippet storage is not S3, then
                    // this is a sign we must read the notification content from s3 and enrich the request
                    // and set it to the incoming message body
                    .process(exchange -> {
                        MessageAttributeValue key = exchange.getIn().getHeader(S3_KEY, MessageAttributeValue.class);
                        String notification = s3ClientService.read(exportConfig.getSnippetBucketName(), key.getStringValue());
                        exchange.getIn().setBody(notification);
                    });
    }
}
