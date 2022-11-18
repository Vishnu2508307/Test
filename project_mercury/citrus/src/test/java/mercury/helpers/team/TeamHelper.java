package mercury.helpers.team;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Assertions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.smartsparrow.iam.data.team.TeamSummary;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;

public class TeamHelper {

    @SuppressWarnings("unchecked")
    public static String createTeam(Map<String, String> fields) {
        return new PayloadBuilder()
                .addField("type", "iam.team.create")
                .addAll((Map) fields)
                .build();
    }

    public static String createTeam(String name) {
        return new PayloadBuilder()
                .addField("type", "iam.team.create")
                .addField("name", name)
                .build();
    }

    @SuppressWarnings("unchecked")
    public static String updateTeam(String teamId, Map<String, String> fields) {
        return new PayloadBuilder()
                .addField("type", "iam.team.update")
                .addField("teamId", teamId)
                .addAll((Map) fields)
                .build();
    }

    public static String deleteTeam(String teamId, String subscriptionId) {
        return new PayloadBuilder()
                .addField("type", "iam.team.delete")
                .addField("teamId", teamId)
                .addField("subscriptionId", subscriptionId)
                .build();
    }

    @SuppressWarnings("unchecked")
    public static ValidationCallback createTeamValidationCallback(String expectedTeamVarName) {
        return new ResponseMessageValidationCallback<TeamSummary>(TeamSummary.class) {
            @Override
            public void validate(TeamSummary payload, Map<String, Object> headers, TestContext context) {
                Map<String, String> expectedTeam = context.getVariable(expectedTeamVarName, Map.class);

                assertNotNull(payload.getId());
                assertEquals(expectedTeam.get("name"), payload.getName());
                assertEquals(expectedTeam.get("description"), payload.getDescription());
                assertEquals(expectedTeam.get("thumbnail"), payload.getThumbnail());
                assertEquals(expectedTeam.get("subscriptionId"), payload.getSubscriptionId().toString());
            }

            @Override
            public String getRootElementName() {
                return "team";
            }

            @Override
            public String getType() {
                return "iam.team.create.ok";
            }
        };
    }

    public static ValidationCallback updateTeamValidationCallback(Map<String, String> expectedTeam) {
        return new ResponseMessageValidationCallback<TeamSummary>(TeamSummary.class) {
            @Override
            public void validate(TeamSummary payload, Map<String, Object> headers, TestContext context) {
                assertNotNull(payload.getId());
                assertEquals(expectedTeam.get("name"), payload.getName());
                assertEquals(expectedTeam.get("description"), payload.getDescription());
                assertEquals(expectedTeam.get("thumbnail"), payload.getThumbnail());
            }

            @Override
            public String getRootElementName() {
                return "team";
            }

            @Override
            public String getType() {
                return "iam.team.update.ok";
            }
        };
    }

    public static String listSubscriptionTeamsRequest(Integer limit) {
        return new PayloadBuilder()
                .addField("type", "iam.subscription.team.list")
                .addField("collaboratorLimit", limit)
                .build();
    }

    public static String teamResponseEmptyList(String type) {
        return "{" +
                "\"type\":\""+type+"\"," +
                "\"response\":{" +
                "\"teams\":[]" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }

    public static ResponseMessageValidationCallback<List> validateReceivedTeamsCallback(List<String> teamNameList, final String type) {
        return new ResponseMessageValidationCallback<List>(List.class) {
            @SuppressWarnings("unchecked")
            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
                TeamHelper.verifyReceivedTeams(payload, context, teamNameList);
            }

            @Override
            public String getRootElementName() {
                return "teams";
            }

            @Override
            public String getType() {
                return type;
            }
        };
    }

    private static void verifyReceivedTeams(List payload, TestContext context, List<String> teamNameList) {
        Assertions.assertEquals(teamNameList.size(), payload.size());

        final Map<String, String> expected = teamNameList.stream()
                .map(one-> Maps.newHashMap(one, one)).reduce((prev, next) ->{
                    next.putAll(prev);
                    return next;
                }).orElse(new HashMap<>());

        for (Object aPayload : payload) {
            final Map teamPayload = (Map) aPayload;
            String currentName = (String) teamPayload.get("name");
            Assertions.assertEquals(context.getVariable(Variables.nameFrom(expected.get(currentName), "id")),
                    teamPayload.get("teamId"));
        }
    }
}
