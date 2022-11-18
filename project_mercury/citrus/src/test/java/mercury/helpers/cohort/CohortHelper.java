package mercury.helpers.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;

import mercury.common.PayloadBuilder;
import mercury.common.Variables;
import mercury.helpers.common.GenericMessageHelper;

public class CohortHelper {

    /**
     * Build a cohort create request message
     *
     * @param name the cohort name
     * @param enrollmentType the cohort enrollment Type
     * @return a json string representing the message request
     */
    public static String createCohortRequest(String name, String enrollmentType, String workspaceId, String productId) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.create");
        pb.addField("name", name);
        pb.addField("workspaceId", workspaceId);
        pb.addField("enrollmentType", enrollmentType);
        pb.addField("productId", productId);
        return pb.build();
    }

    /**
     * Build a Lti type cohort create request message
     *
     * @param name the cohort name
     * @param workspaceId the workspace id
     * @param ltiConsumerCredential the Lti consumer key and secret
     * @return a json string representing the message request
     */
    public static String createLtiCohortRequest(String name, String workspaceId, LtiConsumerCredential ltiConsumerCredential) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.create");
        pb.addField("name", name);
        pb.addField("workspaceId", workspaceId);
        pb.addField("enrollmentType", "LTI");
        pb.addField("ltiConsumerCredential", ltiConsumerCredential);
        return pb.build();
    }

    public static String createCohortRequest(Object name,
                                             Object enrollmentType,
                                             Object workspaceId) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.create");
        pb.addField("name", name);
        pb.addField("workspaceId", workspaceId);
        pb.addField("enrollmentType", enrollmentType);
        return pb.build();
    }

    @SuppressWarnings("unchecked")
    public static <T> String createCohortRequest(Map<String, T> fields ) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.create");
        pb.addAll((Map<String, Object>) fields);
        return pb.build();
    }

    /**
     * Validate the response for a cohort create request
     *
     * @param id the cohort Id
     * @param name the cohort name
     * @param enrollmentType the cohort enrollment type
     * @return a json string representing the response message
     */
    public static String createCohortResponse(String id, String name, String enrollmentType) {
        return "{" +
                "  \"type\": \"workspace.cohort.create.ok\"," +
                "  \"response\": {" +
                "    \"cohort\": {" +
                "      \"summary\": {" +
                "        \"cohortId\": \"" + id + "\"," +
                "        \"workspaceId\": \"@notEmpty()@\"," +
                "        \"name\": \"" + name + "\"," +
                "        \"enrollmentType\": \"" + enrollmentType + "\"," +
                "        \"createdAt\": \"@notEmpty()@\"," +
                "        \"creator\": \"@notEmpty()@\"" +
                "      }," +
                "      \"settings\": \"@notEmpty()@\"" +
                "    }" +
                "  }," +
                "  \"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    public static String createCohortPermissionFailResponse() {
        return "{" +
                "\"type\":\"workspace.cohort.create.error\"," +
                "\"code\":401," +
                "\"message\": \"Unauthorized: Unauthorized permission level\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";

    }

    /**
     * Build a cohort fetch request
     *
     * @param id the id of the cohort to fetch
     * @return a json string representing the request message
     */
    public static String getCohortRequest(String id) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.get");
        pb.addField("cohortId", id);
        return pb.build();
    }

    /**
     * Validate a cohort fetch response message
     *
     * @param id the cohort id
     * @param name the cohort name
     * @param enrollmentType the cohort enrollment type
     * @return a json string representing the response message
     */
    public static String getCohortResponse(String id, String name, String enrollmentType) {
        return "{" +
                "  \"type\": \"workspace.cohort.get.ok\"," +
                "  \"response\": {" +
                "    \"cohort\": {" +
                "      \"summary\": {" +
                "        \"cohortId\": \"" + id + "\"," +
                "        \"name\": \"" + name + "\"," +
                "        \"enrollmentType\": \"" + enrollmentType + "\"," +
                "        \"createdAt\": \"@notEmpty()@\"," +
                "        \"creator\": \"@notEmpty()@\"" +
                "      }" +
                "    }" +
                "  }," +
                "  \"replyTo\": \"@notEmpty()@\"" +
                "}";

    }

    public static String getCohortPermissionErrorResponse() {
        return "{" +
                "\"type\":\"workspace.cohort.get.error\"," +
                "\"code\":401," +
                "\"message\": \"Unauthorized: Unauthorized permission level\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";

    }

    /**
     * Validate a cohort error response message
     *
     * @param code the error code that the response should contain
     * @param message the error message that the response should contain
     * @return a json string representation of the error message
     */
    public static String getCohortErrorResponse(int code, String message) {
        return "{" +
                "\"type\":\"workspace.cohort.get.error\", " +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }

    /**
     * Build a cohort collaborators listing message request
     *
     * @param cohortId the cohort id to list the collaborators for
     * @param limit the max number of collaborators payload to return
     * @return a json string representing the message request
     * @throws JsonProcessingException when failing to build the payload
     */
    public static String createCohortCollaboratorsListingRequest(String cohortId, Integer limit)
            throws JsonProcessingException {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.collaborator.summary")
                .addField("cohortId", cohortId)
                .addField("limit", limit)
                .build();
    }

    /**
     * Extract the actual collaborators account ids found in the response
     *
     * @param response the response to extract the account ids from
     * @param field the response field name that contains the list of {@link com.smartsparrow.iam.payload.AccountPayload}
     * @return a list of string containing all the account ids returned in the response
     * @throws IOException when failing to extract the field
     */
    public static Set<String> getActualAccountIdsFrom(Map<String, Object> response, String field) throws IOException {
        return GenericMessageHelper
                .extractResponseField(new TypeReference<List<AccountCollaboratorPayload>>(){}, response, field)
                .stream()
                .map(one-> one.getAccountPayload().getAccountId().toString())
                .collect(Collectors.toSet());
    }

    /**
     * Extract the actual collaborators team ids found in the response
     */
    public static Set<String> getActualTeamIdsFrom(Map<String, Object> response, String field) throws IOException {
        return GenericMessageHelper
                .extractResponseField(new TypeReference<List<TeamCollaboratorPayload>>(){}, response, field)
                .stream()
                .map(one-> one.getTeamPayload().getId().toString())
                .collect(Collectors.toSet());
    }

    /**
     * Extract the expected collaborators ids from the citrus test context. The collaborators ids are accessed by interpolating
     * each account/team name from the test context.
     *
     * @param dataTable the data table containing the account/team name and type
     * @param testContext the test context to extract the id variables from
     * @return a map with two entries: account and team. Each entry is a set of strings containing the desired accounts/teams ids.
     */
    public static Map<String, Set<String>> getExpectedCollaboratorsIdsFrom(Map<String, String> dataTable,
                                                                           TestContext testContext) {
        Map<String, Set<String>> result = new HashMap<>(2);
        dataTable.forEach((key, value) -> {
            if ("account".equalsIgnoreCase(value)) {
                String accountId = testContext.getVariable(Variables.interpolate(Variables.nameFrom(key, "id")));
                result.computeIfAbsent("accounts", x -> new HashSet<>()).add(accountId);
            } else if ("team".equalsIgnoreCase(value)) {
                String teamId = testContext.getVariable(Variables.interpolate(Variables.nameFrom(key, "id")));
                result.computeIfAbsent("teams", x -> new HashSet<>()).add(teamId);
            } else {
                throw new CitrusRuntimeException("Unknown collaborator type: only account or team");
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> String updateCohortRequest(String cohortId, Map<String, T>  fields) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.change");
        pb.addField("cohortId", cohortId);
        pb.addAll((Map<String, Object>) fields);
        return pb.build();
    }

    public static String updateCohortPermissionFailResponse() {
        return "{" +
                "\"type\":\"workspace.cohort.change.error\"," +
                "\"code\":401," +
                "\"message\": \"Unauthorized: Unauthorized permission level\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";

    }

    public static void assertCohortFields(Map<String, String> expected, CohortPayload actual) {
        assertEquals(expected.get("name"), actual.getSummaryPayload().getName());
        assertEquals(expected.get("enrollmentType"), actual.getSummaryPayload().getEnrollmentType().toString());
        assertEquals(expected.get("startDate"), actual.getSummaryPayload().getStartDate());
        assertEquals(expected.get("endDate"), actual.getSummaryPayload().getEndDate());
        assertEquals(expected.get("enrollmentsCount") == null ? 0 : Long.parseLong(expected.get("enrollmentsCount")),
                     actual.getSummaryPayload().getEnrollmentsCount());
        if (expected.get("color") != null || expected.get("bannerPattern") != null || expected.get("bannerImage") != null) {
            assertEquals(expected.get("color"), actual.getSettingsPayload().getColor());
            assertEquals(expected.get("bannerPattern"), actual.getSettingsPayload().getBannerPattern());
            assertEquals(expected.get("bannerImage"), actual.getSettingsPayload().getBannerImage());
        } else {
            assertNotNull(actual.getSettingsPayload());

        }
        assertNotNull(actual.getSummaryPayload().getCreator());
        assertNotNull(actual.getSummaryPayload().getCreatedAt());
    }

    public static String cohortBroadcastMessage(String cohortId, String cohortAction, String rtmSubscriptionId) {
        // TODO remove next line when FE supported
        String[] actions = cohortAction.split("_");

        return "{" +
                "    \"type\": \"workspace.cohort.broadcast\"," +
                "    \"replyTo\": \"" + rtmSubscriptionId + "\"," +
                "    \"response\": {" +
                "      \"cohortId\" : \"" + cohortId + "\"," +
                // TODO remove next line when FE supported
                "      \"action\" : \"" + actions[1] + "\"," +
                "      \"rtmEvent\" : \"" + cohortAction + "\"" +
                "    }" +
                "}";
    }

    /**
     * Create a cohort archive message request
     *
     * @param cohortId the cohort archive
     * @return a json string representation if the message request
     */
    public static String createCohortArchiveRequest(String cohortId) {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.archive")
                .addField("cohortId", cohortId)
                .build();
    }

    /**
     * Validate a cohort archive message response
     *
     * @return a json string representation of the message response
     */
    public static String getCohortArchiveResponse() {
        return "{" +
                    "\"type\":\"workspace.cohort.archive.ok\"," +
                    "\"response\":{" +
                        "\"finishedDate\":\"@notEmpty()@\"" +
                    "}," +
                    "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    /**
     * Validate a cohort archive/unarchive response message
     *
     * @param userAction it's either <code>archive</code> or <code>unarchive</code>
     * @return a json string representation of the message response
     */
    public static String getCohortArchiveUnauthorizedResponse(String userAction) {
        return "{" +
                    "\"type\":\"workspace.cohort." + userAction + ".error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    /**
     * Validate a cohort unarchive response
     *
     * @return a json string representation of the message response
     */
    public static String getCohortUnarchiveResponse() {
        return "{\"type\":\"workspace.cohort.unarchive.ok\",\"replyTo\":\"@notEmpty()@\"}";
    }

    /**
     * Create a cohort unarchive message request
     *
     * @param cohortId the cohort to unarchive
     * @return a json string representing the message request
     */
    public static String createCohortUnarchiveRequest(String cohortId) {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.unarchive")
                .addField("cohortId", cohortId)
                .build();
    }

    public static String fetchCohortSummary(String cohortId) {
        return "query {\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      id\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchEnrollments(String cohortId) {
        return "query {\n" +
                "  cohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    enrollments {\n" +
                "      edges {\n" +
                "        node {\n" +
                "          accountId\n" +
                "          cohortId\n" +
                "          enrolledAt\n" +
                "          enrolledBy\n" +
                "          enrollmentDate\n" +
                "          enrollmentStatus\n" +
                "          enrollmentType\n" +
                "          expiresAt\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchCohortSummaryWithField(final String cohortId, final String field) {
        return "query {\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      id\n" +
                "      " + field + "\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
