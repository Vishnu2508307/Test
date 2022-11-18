package mercury.glue.step.publication;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.datastax.driver.core.utils.UUIDs;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PublicationActivitySteps {

    private UUID ACTIVITY_ID_PREGENERATED;

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("user requests a list of activity")
    public void userRequestsAListOfActivity() {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "publication.activity.fetch");
        payload.addField("workspaceId", UUID.randomUUID());
        messageOperations.sendJSON(runner, payload.build());
    }

    @And("{string} has created activity {string} inside project {string} with id")
    public void hasCreatedActivityInsideProjectWithId(String accountName, String activityName, String projectName) {
        ACTIVITY_ID_PREGENERATED = UUIDs.timeBased();
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.create")
                .addField("pluginId", interpolate(PLUGIN_ID_VAR))
                .addField("pluginVersion", "1.*")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("activityId", ACTIVITY_ID_PREGENERATED)
                .build());

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.activity.create.ok")
                .extractFromPayload("$.response.activity.activityId", ACTIVITY_ID_PREGENERATED.toString())
                .jsonPath("$.response.activity.plugin", "@notEmpty()@")
                .jsonPath("$.response.activity.creator", "@notEmpty()@")
                .jsonPath("$.response.activity.createdAt", "@notEmpty()@")
                .jsonPath("$.response.activity.studentScopeURN",  "@notEmpty()@")

        );
    }

    @Given("{string} has created publication {string} for the activity")
    public void hasCreatedPublicationForTheActivity(String accountName, String title) {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.create.request")
                .addField("accountId", UUIDs.timeBased())
                .addField("activityId", ACTIVITY_ID_PREGENERATED)
                .addField("exportId", UUIDs.timeBased())
                .addField("publicationTitle", title)
                .addField("author", "Hibbeler")
                .addField("description", "test title")
                .addField("version", "1.0")
                .build());
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "publication.create.request.ok"));
    }

    @Then("{string} should fetch a list of activity in workspace {string} including")
    public void shouldFetchAListOfActivityInWorkspaceIncluding(String accountName, String workspaceName, List<String> activities) {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "publication.activity.fetch");
        payload.addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)));
        messageOperations.sendJSON(runner, payload.build());

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "activities";
                    }

                    @Override
                    public String getType() {
                        return "publication.activity.fetch.ok";
                    }

                    @Override
                    public void validate(ArrayList results, Map<String, Object> headers, TestContext context) {

                        assertEquals(1, results.size());

                        Set<String> actualActivities = new HashSet<>();
                        for (Object activity : results) {
                            actualActivities.add((String) ((Map) activity).get("activityId"));
                        }

                        assertTrue(actualActivities.contains(ACTIVITY_ID_PREGENERATED.toString()));
                    }
                }));
    }

    @Then("{string} should fetch a list of activity including")
    public void shouldFetchAListOfActivityIncluding(String accountName, List<String> activities) {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "publication.activity.fetch");
        messageOperations.sendJSON(runner, payload.build());

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "activities";
                    }

                    @Override
                    public String getType() {
                        return "publication.activity.fetch.ok";
                    }

                    @Override
                    public void validate(ArrayList results, Map<String, Object> headers, TestContext context) {

                        Set<Map> expectedActivities = new HashSet<>();
                        for (String activity : activities) {
                            Map<String, String> expected = new HashMap<>();
                            expected.put("title", activity);
                            expectedActivities.add(expected);
                        }

                        Set<Map> actualActivities = new HashSet<>();
                        for (Object activity : results) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("title", (String) ((Map) activity).get("title"));
                            actualActivities.add(actual);
                        }

                        assertTrue(actualActivities.containsAll(expectedActivities));

                    }
                }));
    }
}
