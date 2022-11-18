package com.smartsparrow.workspace.route;

import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL_ERROR_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL_RESULT_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PUSH_ERROR_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PUSH_RESULT_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.RS;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;

public class AlfrescoCoursewareRoute extends RouteBuilder {

    public static final String ALFRESCO_NODE_PUSH_RESULT_BODY = "alfresco.node.push.result.body";
    public static final String ALFRESCO_NODE_PULL_RESULT_BODY = "alfresco.node.pull.result.body";
    public static final String ALFRESCO_NODE_PUSH_ERROR_BODY = "alfresco.node.push.error.body";
    public static final String ALFRESCO_NODE_PULL_ERROR_BODY = "alfresco.node.pull.error.body";

    private final AlfrescoAssetPushResultHandler alfrescoAssetPushResultHandler;
    private final AlfrescoAssetPullResultHandler alfrescoAssetPullResultHandler;
    private final AlfrescoAssetPushErrorHandler alfrescoAssetPushErrorHandler;
    private final AlfrescoAssetPullErrorHandler alfrescoAssetPullErrorHandler;

    @Inject
    public AlfrescoCoursewareRoute(AlfrescoAssetPushResultHandler alfrescoAssetPushResultHandler,
                                   AlfrescoAssetPullResultHandler alfrescoAssetPullResultHandler,
                                   AlfrescoAssetPushErrorHandler alfrescoAssetPushErrorHandler,
                                   AlfrescoAssetPullErrorHandler alfrescoAssetPullErrorHandler) {
        this.alfrescoAssetPushResultHandler = alfrescoAssetPushResultHandler;
        this.alfrescoAssetPullResultHandler = alfrescoAssetPullResultHandler;
        this.alfrescoAssetPushErrorHandler = alfrescoAssetPushErrorHandler;
        this.alfrescoAssetPullErrorHandler = alfrescoAssetPullErrorHandler;
    }

    @Override
    public void configure() {
        /*
         * This route takes care of handling the ResultNotification from
         * the alfresco push external call
         */
        from(RS + ALFRESCO_NODE_PUSH_RESULT_HANDLER)
                // set result notification
                .setProperty(ALFRESCO_NODE_PUSH_RESULT_BODY, body())
                // set the id
                .id(ALFRESCO_NODE_PUSH_RESULT_HANDLER)
                // handle the route via bean handler
                .bean(alfrescoAssetPushResultHandler);

        /*
         * This route takes care of handling the ResultNotification from
         * the alfresco pull external call
         */
        from(RS + ALFRESCO_NODE_PULL_RESULT_HANDLER)
                // set result notification
                .setProperty(ALFRESCO_NODE_PULL_RESULT_BODY, body())
                // set the id
                .id(ALFRESCO_NODE_PULL_RESULT_HANDLER)
                // handle the route via bean handler
                .bean(alfrescoAssetPullResultHandler);

        /*
         * This route takes care of handling the ErrorNotification from
         * the alfresco push external call
         */
        from(RS + ALFRESCO_NODE_PUSH_ERROR_HANDLER)
                // set error notification
                .setProperty(ALFRESCO_NODE_PUSH_ERROR_BODY, body())
                // set the id
                .id(ALFRESCO_NODE_PUSH_ERROR_HANDLER)
                // handle the route via bean handler
                .bean(alfrescoAssetPushErrorHandler);

        /*
         * This route takes care of handling the ErrorNotification from
         * the alfresco pull external call
         */
        from(RS + ALFRESCO_NODE_PULL_ERROR_HANDLER)
                // set error notification
                .setProperty(ALFRESCO_NODE_PULL_ERROR_BODY, body())
                // set the id
                .id(ALFRESCO_NODE_PULL_ERROR_HANDLER)
                // handle the route via bean handler
                .bean(alfrescoAssetPullErrorHandler);
    }
}
