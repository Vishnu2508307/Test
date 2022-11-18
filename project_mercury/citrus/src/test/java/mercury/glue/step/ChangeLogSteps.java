package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class ChangeLogSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @Then("{string} can list the following courseware changelog for element {string}")
    public void canListTheFollowingCoursewareChangelogForElement(final String accountName, final String elementName, final Map<String, String> expectedChangeLogMap) {

        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.courseware.changelog.list")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList elementChangelogs, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedChangeLogMap.size(), elementChangelogs.size());

                        List<Map.Entry<String, String>> expectedEntries = new ArrayList<>(expectedChangeLogMap.entrySet());

                        for (int i = 0; i < elementChangelogs.size(); i++) {
                            final Map actualChangelog = (Map) elementChangelogs.get(i);
                            final Map.Entry<String, String> expectedEntry = expectedEntries.get(i);
                            final String onElementName = expectedEntry.getKey().split("__")[1];
                            final String expectedOnElementId = context.getVariable(interpolate(nameFrom(onElementName, "id")));
                            assertEquals(expectedOnElementId, actualChangelog.get("onElementId"));
                            assertEquals(expectedEntry.getValue(), actualChangelog.get("coursewareAction"));
                            assertEquals(context.getVariable(nameFrom(elementName, "id")), actualChangelog.get("elementId"));
                        }

                    }

                    @Override
                    public String getRootElementName() {
                        return "changelogs";
                    }

                    @Override
                    public String getType() {
                        return "project.courseware.changelog.list.ok";
                    }
                }));
    }

    @Then("{string} can list the following courseware changelog  for project {string}")
    public void canListTheFollowingCoursewareChangelogForProject(final String accountName, final String projectName, final Map<String, String> expectedChangeLogMap) {

        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.changelog.list")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList projectChangelogs, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedChangeLogMap.size(), projectChangelogs.size());

                        List<Map.Entry<String, String>> expectedEntries = new ArrayList<>(expectedChangeLogMap.entrySet());

                        for (int i = 0; i < projectChangelogs.size(); i++) {
                            final Map actualChangelog = (Map) projectChangelogs.get(i);
                            final Map.Entry<String, String> expectedEntry = expectedEntries.get(i);
                            final String onElementName = expectedEntry.getKey().split("__")[1];
                            final String expectedOnElementId = context.getVariable(interpolate(nameFrom(onElementName, "id")));
                            assertEquals(expectedOnElementId, actualChangelog.get("onElementId"));
                            assertEquals(expectedEntry.getValue(), actualChangelog.get("coursewareAction"));
                            assertEquals(context.getVariable(nameFrom(projectName, "id")), actualChangelog.get("projectId"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "projectchangelogs";
                    }

                    @Override
                    public String getType() {
                        return "project.changelog.list.ok";
                    }
                }));
    }

    @And("{string} subscribes to changelog events over project {string}")
    public void subscribesToEventsOverProject(String accountName, String projectName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.changelog.subscribe")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build(), accountName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.changelog.subscribe.ok"), accountName);
    }

    @Then("{string} sees {string} was {string} by {string} changelog")
    public void seesWasByChangelog(String clientName, String elementName, String coursewareAction, String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                        .jsonPath("$.response.changeLog.onElementId", interpolate(nameFrom(elementName, "id")))
                        .jsonPath("$.response.changeLog.coursewareAction", coursewareAction)
                        .jsonPath("$.response.changeLog.accountId", interpolate(getAccountIdVar(accountName))),
                clientName);
    }

    @And("{string} subscribes to changelogs events over activity {string}")
    public void subscribesToChangelogsEventsOverActivity(String accountName, String activityName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.changelog.subscribe")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .build(), accountName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.activity.changelog.subscribe.ok"), accountName);
    }

    @Then("{string} unsubscribes to changelogs events over the activity {string}")
    public void unsubscribesToChangelogsEventsOverTheActivity(String accountName, String activityName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.changelog.unsubscribe")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .build(), accountName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.activity.changelog.unsubscribe.ok"), accountName);
    }

    @Then("{string} tries to subscribe to the changelogs events over activity {string} but fails due to missing permission level")
    public void triesToSubscribeToTheChangelogsEventsOverActivityButFailsDueToMissingPermissionLevel(String accountName, String activityName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.changelog.subscribe")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .build(), accountName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.activity.changelog.subscribe.error"), accountName);
    }
}
