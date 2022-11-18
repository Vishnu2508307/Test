package com.smartsparrow.learner.route;

import com.smartsparrow.ext_http.service.ErrorNotification;
import com.smartsparrow.learner.data.GradePassbackGateway;
import com.smartsparrow.learner.data.GradePassbackNotification;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import javax.inject.Inject;

public class GradePassbackErrorHandler {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GradePassbackErrorHandler.class);

    public static final String LEARNER_GRADE_PASSBACK_ERROR_BODY = "learner.grade.passback.error.body";

    final private GradePassbackGateway gradePassbackGateway;

    @Inject
    public GradePassbackErrorHandler(GradePassbackGateway gradePassbackGateway) {
        this.gradePassbackGateway = gradePassbackGateway;
    }

    /**
     * Handle the grade passback external call result.
     *
     * @param exchange the camel exchange
     */
    @Handler
    public void handle(Exchange exchange) {
        // get the error notification
        ErrorNotification errorNotification = exchange.getProperty(LEARNER_GRADE_PASSBACK_ERROR_BODY, ErrorNotification.class);

        gradePassbackGateway.findNotifcationById(errorNotification.getState().getNotificationId())
                .flatMap(gradePassbackNotification -> {
                    gradePassbackNotification
                            .setStatus(GradePassbackNotification.Status.FAILURE)
                            .setCompletedAt(UUIDs.timeBased());
                    return gradePassbackGateway.persist(gradePassbackNotification)
                            .singleOrEmpty();
                })
                .subscribe();
    }
}
