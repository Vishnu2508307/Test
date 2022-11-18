package com.smartsparrow.export.route;

import static com.smartsparrow.dataevent.RouteUri.DIRECT;
import static com.smartsparrow.dataevent.RouteUri.RS;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;

import com.smartsparrow.export.service.ExportRouteErrorHandler;
import com.smartsparrow.export.wiring.ExportConfig;

public class CoursewareExportRoute extends RouteBuilder {

    public static final String SUBMIT_EXPORT_REQUEST = "submit-export_request";
    public static final String SUBMIT_EXPORT_FAILURE = "submit-export_failure";
    public static final String NOTIFICATION_ATTRIBUTE = "notificationId";
    private static final String EXPORT_ERROR = "export.error";

    @Inject
    protected ExportConfig exportConfig;

    @Inject
    private ExportRouteErrorHandler exportRouteErrorHandler;

    public void configure() {
        // if any of the above route fails it will get handled by this bean
        from(DIRECT + EXPORT_ERROR)
                .id("exportErrorHandler")
                .bean(exportRouteErrorHandler);


        //export notification sent to epub transform publication notification
        from(RS + SUBMIT_EXPORT_FAILURE)
                .toD("aws-sns://" + exportConfig.getSubmitExportFailureTopic());
    }

}
