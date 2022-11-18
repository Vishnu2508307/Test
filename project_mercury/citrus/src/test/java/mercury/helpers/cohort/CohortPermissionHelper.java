package mercury.helpers.cohort;

import com.google.common.collect.Lists;

import mercury.common.PayloadBuilder;

public class CohortPermissionHelper {

    /**
     * Create a cohort permission grant request
     *
     * @param cohortId the cohort id to grant the permission for
     * @param accountIds the account ids to grant access to
     * @param permissionLevel the permission level of the account
     * @return a json string representing a message request for granting cohort permission
     */
    public static String createCohortPermissionGrantMessage(String cohortId, String permissionLevel, String... accountIds) {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.permission.grant")
                .addField("cohortId", cohortId)
                .addField("accountIds", accountIds)
                .addField("permissionLevel", permissionLevel)
                .build();
    }

    public static String createCohortPermissionTeamGrantMessage(String cohortId, String permissionLevel, String... teamIds) {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.permission.grant")
                .addField("cohortId", cohortId)
                .addField("teamIds", teamIds)
                .addField("permissionLevel", permissionLevel)
                .build();
    }

    /**
     * Validate the success response for a cohort permission grant message
     *
     * @param accountIds the account ids the permission should be granted to
     * @param cohortId the cohort the accounts has now access to
     * @param permissionLevel the permission level
     * @return a json string describing the message response
     */
    public static String getCohortGrantPermissionSuccessResponse(String cohortId, String permissionLevel, String... accountIds) {
        String accountArray = String.join(",", Lists.newArrayList(accountIds));
        return "{" +
                "\"type\":\"workspace.cohort.permission.grant.ok\"," +
                "\"response\":{" +
                "\"accountIds\":\"@assertThat(containsInAnyOrder(" + accountArray + "))@\"," +
                "\"cohortId\":\"" + cohortId + "\"," +
                "\"permissionLevel\":\"" + permissionLevel + "\"" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String getCohortTeamGrantPermissionSuccessResponse(String cohortId, String permissionLevel, String... teamIds) {
        String teamArray = String.join(",", Lists.newArrayList(teamIds));
        return "{" +
                "\"type\":\"workspace.cohort.permission.grant.ok\"," +
                "\"response\":{" +
                "\"teamIds\":\"@assertThat(containsInAnyOrder(" + teamArray + "))@\"," +
                "\"cohortId\":\"" + cohortId + "\"," +
                "\"permissionLevel\":\"" + permissionLevel + "\"" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    /**
     * Validate an error response for cohort permission message request
     *
     * @param action the performed action
     * @return a json string representation of the error message
     */
    public static String getCohortPermissionErrorResponse(String action) {
        return "{" +
                "\"type\":\"workspace.cohort.permission." + action + ".error\"," +
                "\"code\":401," +
                "\"message\":\"@notEmpty()@\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    /**
     * Create a cohort permission revoke request
     *
     * @param cohortId the cohort id to remove the permission for
     * @param accountId the account that will lose the permission
     * @return a json string representing the revoke message request
     */
    public static String createCohortPermissionRevokeMessage(String cohortId, String accountId) {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.permission.revoke")
                .addField("cohortId", cohortId)
                .addField("accountId", accountId)
                .build();
    }

    public static String createCohortTeamPermissionRevokeMessage(String cohortId, String teamId) {
        return new PayloadBuilder()
                .addField("type", "workspace.cohort.permission.revoke")
                .addField("cohortId", cohortId)
                .addField("teamId", teamId)
                .build();
    }
}
