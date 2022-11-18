package mercury.helpers.cohort;

import com.fasterxml.jackson.core.JsonProcessingException;

import mercury.common.PayloadBuilder;

public class CohortEnrollmentHelper {

    /**
     * Create a json string message for workspace.cohort.account.enroll
     *
     * @param cohortId the cohort to enroll the account to
     * @param accountId the account to enroll
     * @return a json string representation of the message request
     * @throws JsonProcessingException when failing to build the payload
     */
    public static String createCohortEnrollRequest(String cohortId, String accountId)
            throws JsonProcessingException {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.account.enroll")
                .addField("cohortId", cohortId)
                .addField("accountId", accountId)
                .build();
    }

    /**
     * Validate the response for a cohort enroll request with the supplied arguments
     *
     * @param cohortId the enrollment cohort id
     * @param accountId the enrolled account id
     * @param subscriptionId the enrolled subscription id
     * @return a json string describing the payload
     */
    public static String getCohortEnrollSuccessResponse(String cohortId, String accountId, String subscriptionId) {
        return "{" +
                "\"type\":\"workspace.cohort.account.enroll.ok\"," +
                "\"response\":{" +
                    "\"enrollment\":{" +
                        "\"cohortId\":\"" + cohortId + "\"," +
                        "\"enrolledAt\":\"@notEmpty()@\"," +
                        "\"enrollmentType\":\"INSTRUCTOR\"," +
                        "\"accountSummary\":{" +
                            "\"accountId\":\"" + accountId + "\"," +
                            "\"subscriptionId\":\"" + subscriptionId + "\"," +
                            "\"primaryEmail\":\"@notEmpty()@\"" +
                        "}" +
                    "}" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }

    /**
     * Create a cohort account disenroll request
     *
     * @param cohortId the cohort to disenroll the account from
     * @param accountId the account to disenroll
     * @return a json string representing the request
     * @throws JsonProcessingException when failing to build the payload
     */
    public static String createCohortDisenrollRequest(String cohortId, String accountId) throws JsonProcessingException {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.account.disenroll")
                .addField("cohortId", cohortId)
                .addField("accountId", accountId)
                .build();
    }

    /**
     * Validate the response for a cohort disenroll request
     *
     * @return the validated payload
     */
    public static String getCohortDisenrollSuccessResponse() {
        return "{\"type\":\"workspace.cohort.account.disenroll.ok\",\"replyTo\":\"@notEmpty()@\"}";
    }

    /**
     * Create a cohort enrollments list message request
     * @param cohortId the cohort id to list the enrollments for
     * @return a json string representation of the message request
     * @throws JsonProcessingException when failing to build the json string
     */
    public static String createCohortEnrollmentsListingRequest(String cohortId) throws JsonProcessingException {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.enrollment.list")
                .addField("cohortId", cohortId)
                .build();
    }

    /**
     * Validate an unauthorized cohort message response
     *
     * @param subType the message subtype for workspace.cohort.<subtype>
     * @return a json string representation of the error message
     */
    public static String getUnauthorizedResponse(String subType) {
        return "{" +
                    "\"type\":\"" + String.format("workspace.cohort.%s", subType) + "\"," +
                    "\"code\":401,\"message\":\"Unauthorized: Unauthorized permission level\"," +
                    "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    /**
     * Validate a cohort enrollments list response message
     *
     * @param cohortId the cohort id
     * @param accountId the account id that should be listed in the enrollments
     * @return a json string representation of the message response
     */
    public static String validateCohortEnrollmentsListResponse(String cohortId, String accountId) {
        return "{" +
                    "\"type\":\"workspace.cohort.enrollment.list.ok\"," +
                    "\"response\":{" +
                        "\"enrollments\":[{" +
                            "\"cohortId\":\"" + cohortId + "\"," +
                            "\"enrolledAt\":\"@notEmpty()@\"," +
                            "\"enrollmentType\":\"OPEN\"," +
                            "\"accountSummary\":{" +
                                "\"accountId\":\"" + accountId + "\"," +
                                "\"subscriptionId\":\"@notEmpty()@\"," +
                                "\"primaryEmail\":\"@notEmpty()@\"" +
                            "}" +
                        "}]" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }
}
