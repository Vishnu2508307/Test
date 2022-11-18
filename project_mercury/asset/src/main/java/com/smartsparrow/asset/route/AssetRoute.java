package com.smartsparrow.asset.route;

import javax.inject.Inject;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;

import com.smartsparrow.asset.lang.AssetRouteValidationException;
import com.smartsparrow.config.AssetConfig;

public class AssetRoute extends RouteBuilder {

    public static final String UPLOAD_ASSET_ROUTE  = "direct:upload_asset";
    public static final String SUBMIT_ASSET_RESIZE_REQUEST = "direct:submit_asset_resize_request";
    public static final String ASSET_RESIZE_RETRY_QUEUE = "direct:submit_asset_resize_retry_queue";

    public static final String FILE_NAME = "fileName";
    public static final String CONTENT_TYPE = "contentType";

    @Inject
    private AssetConfig assetConfig;

    @Override
    public void configure() {

        final String params = String.format("?prefix=%s/", assetConfig.getPrefix());

        from(UPLOAD_ASSET_ROUTE)
                .process(validateHeaderExists(FILE_NAME))
                .log(simple("uploading the asset... ${in.header.fileName} ").getText())
                .setHeader(S3Constants.KEY, simple("assets/${in.header.fileName}"))
                .setHeader(S3Constants.CONTENT_TYPE, simple("${in.header.contentType}"))
                .id("AssetUpload")
                .to("aws-s3:" + assetConfig.getBucketName() + params);

        // process events sent to the "Submit" topic.
        from(SUBMIT_ASSET_RESIZE_REQUEST)
                .toD("aws-sns://" + assetConfig.getSubmitTopicNameOrArn());

        // process messages to be sent to the retry/delay queue
        from(ASSET_RESIZE_RETRY_QUEUE)
                .toD("aws-sqs://" + assetConfig.getDelayQueueNameOrArn());

    }

    private Processor validateHeaderExists(final String headerName) {
        return exchange -> {
            if (exchange.getIn().getHeader(headerName) == null) {
                exchange.setException(new AssetRouteValidationException(String.format("%s header missing from inbound message", headerName)));
            }
        };
    }
}
