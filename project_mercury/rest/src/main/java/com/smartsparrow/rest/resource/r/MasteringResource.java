package com.smartsparrow.rest.resource.r;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.learner.data.GradePassbackAssignment;
import com.smartsparrow.learner.data.GradePassbackItem;
import com.smartsparrow.learner.data.LearnerGateway;
import com.smartsparrow.learner.service.GradePassbackService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Path("/mastering")
public class MasteringResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MasteringResource.class);

    private final GradePassbackService gradePassbackService;

    private final LearnerGateway learnerGateway;

    @Inject
    public MasteringResource(final GradePassbackService gradePassbackService,
                             final LearnerGateway learnerGateway) {
        this.gradePassbackService = gradePassbackService;
        this.learnerGateway = learnerGateway;
    }

    // following Api endpoints are only for verifying grade pass data before fully integrated with Mastering

    @GET
    @Path("/deployment/{deploymentId}/learner/{userId}/activity/{activityId}/gradepassbackassignment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLearnerGradePassbackAssignment(@PathParam("deploymentId") UUID deploymentId,
                                                      @PathParam("userId") UUID userId,
                                                      @PathParam("activityId") UUID activityId,
                                                      @FormDataParam("CustomGradingMethod") String method,
                                                      @FormDataParam("attempt") int attemptNum,
                                                      @FormDataParam("discipline") String discipline) {

        GradePassbackAssignment gradePassbackAssignment = gradePassbackService.getGradePassbackAssignment(deploymentId,
                                                                                userId,
                                                                                activityId,
                                                                                new LTIData()
                                                                                    .setAssignmentId(1243243433)
                                                                                    .setCourseId("565657575")
                                                                                    .setUserId("3434355535")
                                                                                    .setCustomGradingMethod(method)
                                                                                    .setAttemptLimit(attemptNum)
                                                                                    .setDiscipline(discipline)
                                                                                ).block();

        return Response.ok(gradePassbackAssignment).build();
    }

    @GET
    @Path("/deployment/{deploymentId}/learner/{userId}/assignment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLearnerAssignmentScoreAndProgress(@PathParam("deploymentId") UUID deploymentId,
                                                         @PathParam("userId") UUID userId,
                                                         @FormDataParam("CustomGradingMethod") String method) {

        GradePassbackAssignment gradePassbackAssignment = gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, method).block();

        return Response.ok(gradePassbackAssignment).build();
    }

    @GET
    @Path("/deployment/{deploymentId}/learner/{userId}/activity/{activityId}/assignmentItem")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLearnerAssignmentItemScoreAndProgress(@PathParam("deploymentId") UUID deploymentId,
                                                             @PathParam("userId") UUID userId,
                                                             @PathParam("activityId") UUID activityId,
                                                             @FormDataParam("CustomGradingMethod") String method) {

        GradePassbackItem gradePassbackItem =
                gradePassbackService.getAssignmentItemScoreAndProgress(deploymentId,
                                                                        userId,
                                                                        activityId,
                                                                        method).block();

        return Response.ok(gradePassbackItem).build();
    }

    @GET
    @Path("/deployment/{deploymentId}/totalQuestion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLearnerElements(@PathParam("deploymentId") UUID deploymentId) {

        Integer totalCount = gradePassbackService.getGradePassbackActionCountCheck(deploymentId).block();
        
        return Response.ok(totalCount).build();
    }

}
