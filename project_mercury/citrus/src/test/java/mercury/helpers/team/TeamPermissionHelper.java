package mercury.helpers.team;

import static mercury.glue.step.ProvisionSteps.getAccountIdVar;

import java.util.Arrays;

import mercury.common.PayloadBuilder;
import mercury.common.Variables;

public class TeamPermissionHelper {

    public static String grantTeamPermissionRequest(String teamId, String accountId, String permissionLevel) {
        return new PayloadBuilder()
                .addField("type", "iam.team.permission.grant")
                .addField("teamId", teamId)
                .addField("accountIds", Arrays.asList(accountId))
                .addField("permissionLevel", permissionLevel)
                .build();
    }

    public static String revokeTeamPermissionRequest(String accountId, String teamId) {
        return new PayloadBuilder()
                .addField("type", "iam.team.permission.revoke")
                .addField("accountIds", Arrays.asList(accountId))
                .addField("teamId", teamId)
                .build();
    }

    public static String grantTeamPermissionResponse(String user, String permission, String teamId) {
            return "{" +
                        "\"type\":\"iam.team.permission.grant.ok\"," +
                        "\"response\":{" +
                            "\"accountIds\":[\"" + Variables.interpolate(getAccountIdVar(user))+ "\"]," +
                            "\"permissionLevel\":\"" + permission + "\"," +
                            "\"teamId\":\"" + teamId + "\"" +
                        "},\"replyTo\":\"@notEmpty()@\"}";
    }
}
