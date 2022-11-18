package com.smartsparrow.learner.route;

import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.learner.data.GradePassbackGateway;
import com.smartsparrow.learner.data.GradePassbackNotification;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import javax.inject.Inject;

public class GradePassbackResultHandler {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GradePassbackResultHandler.class);

    public static final String LEARNER_GRADE_PASSBACK_RESULT_BODY = "learner.grade.passback.result.body";

    final private GradePassbackGateway gradePassbackGateway;

    @Inject
    public GradePassbackResultHandler(GradePassbackGateway gradePassbackGateway) {
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
        ResultNotification resultNotification = exchange.getProperty(LEARNER_GRADE_PASSBACK_RESULT_BODY, ResultNotification.class);

        gradePassbackGateway.findNotifcationById(resultNotification.getState().getNotificationId())
            .flatMap(gradePassbackNotification -> {
                gradePassbackNotification
                        .setStatus(GradePassbackNotification.Status.SUCCESS)
                        .setCompletedAt(UUIDs.timeBased());
                return gradePassbackGateway.persist(gradePassbackNotification)
                        .singleOrEmpty();
            })
            .subscribe();
    }
}
