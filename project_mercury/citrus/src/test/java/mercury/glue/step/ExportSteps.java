package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.export.data.ExportType;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;

public class ExportSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Given("{string} has created an export of activity {string} with export type {string}")
    public void hasCreatedAnExportWithExportType(String accountName, String activityName, String exportType) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.export.request")
                .addField("elementId", interpolate(nameFrom(activityName, "id")))
                .addField("exportType", exportType).build());
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.exportId", nameFrom(activityName, "exportId")));
    }

    @Given("{string} has created an export of activity {string}")
    public void hasCreatedAnExport(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.export.request")
                .addField("elementId", interpolate(nameFrom(activityName, "id")))
                .build());
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.exportId", nameFrom(activityName, "exportId")));
    }

    @When("{string} subscribes to the export for activity {string}")
    public void subscribesToExport(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.export.subscribe")
                .addField("exportId", interpolate(nameFrom(activityName, "exportId")))
                .build());
    }

    @Then("{string} is successfully subscribed to the export")
    public void exportIsSuccessfullySubscribed(String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.export.subscribe.ok")
                .jsonPath("$.response.rtmSubscriptionId", "@notEmpty()@")
        );
    }

    @When("{string} unsubscribes to the export for activity {string}")
    public void unsubscribesToExport(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.export.unsubscribe")
                .addField("exportId", interpolate(nameFrom(activityName, "exportId")))
                .build());
    }

    @Then("{string} is successfully unsubscribed to the export")
    public void exportIsSuccessfullyUnsubscribed(String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.export.unsubscribe.ok")
        );
    }

    @When("{string} lists all the exports for workspace {string}")
    public void listsAllTheExportsInsideWorkspace(String accountName, String workspaceName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.export.list")
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .build());
    }

    @Then("the following exports are listed for workspace {string} with export type {string}")
    public void theFollowingExportsAreListedForWorkspace(final String workspaceName, String exportType, final List<String> expectedExportNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList exports, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedExportNames.size(), exports.size());
                        Set<String> actualExports = new HashSet<>(exports.size());

                        for (Object export : exports) {
                            actualExports.add((String) ((Map) export).get("id"));
                            actualExports.add((String) ((Map) export).get("exportType"));
                        }

                        Set<String> expectedExports = expectedExportNames.stream()
                                .map(activityName -> context.getVariable(interpolate(nameFrom(activityName, "exportId"))))
                                .collect(Collectors.toSet());
                        expectedExports.add(exportType);

                        assertEquals(expectedExports, actualExports);
                        assertTrue(actualExports.contains(exportType));
                    }

                    @Override
                    public String getRootElementName() {
                        return "exportSummaries";
                    }

                    @Override
                    public String getType() {
                        return "workspace.export.list.ok";
                    }
                }));
    }


    @When("{string} lists all the exports for project {string}")
    public void listsAllTheExportsInsideProject(String accountName, String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.export.list")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @Then("the following exports are listed for project {string} with export type {string}")
    public void theFollowingExportsAreListedForProjectWithExportType(final String projectName, String exportType, final List<String> expectedExportNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList exports, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedExportNames.size(), exports.size());
                        Set<String> actualExports = new HashSet<>(exports.size());

                        for (Object export : exports) {
                            actualExports.add((String) ((Map) export).get("id"));
                            actualExports.add((String) ((Map) export).get("exportType"));
                        }

                        Set<String> expectedExports = expectedExportNames.stream()
                                .map(activityName -> context.getVariable(interpolate(nameFrom(activityName, "exportId"))))
                                .collect(Collectors.toSet());
                        expectedExports.add(exportType);

                        assertEquals(expectedExports, actualExports);
                        assertTrue(actualExports.contains(exportType));
                    }

                    @Override
                    public String getRootElementName() {
                        return "exportSummaries";
                    }

                    @Override
                    public String getType() {
                        return "project.export.list.ok";
                    }
                }));
    }

    @Then("{string} gets an export broadcast for {string}")
    public void exportBroadcast(String accountName, String activityName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.export.broadcast")
                .jsonPath("$.response.exportId", interpolate(nameFrom(activityName, "exportId")))
                .jsonPath("$.response.progress", "@notEmpty()@")
                .jsonPath("$.response.rtmEvent", "EXPORT"));
    }

    @Then("{string} gets an export broadcast complete for {string}")
    public void exportBroadcastComplete(String accountName, String activityName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.export.broadcast")
                .jsonPath("$.response.exportId", interpolate(nameFrom(activityName, "exportId")))
                .jsonPath("$.response.progress", "COMPLETE")
                .jsonPath("$.response.rtmEvent", "EXPORT"));
    }

    @Then("{string} gets an export broadcast error for {string}")
    public void exportBroadcastError(String accountName, String activityName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.export.broadcast")
                .jsonPath("$.response.exportId", interpolate(nameFrom(activityName, "exportId")))
                .jsonPath("$.response.progress", "ERROR")
                .jsonPath("$.response.rtmEvent", "EXPORT"));
    }

    @Given("{string} has created an export of activity {string} with export type {string} and metadata {string}")
    public void hasCreatedAnExportWithExportTypeAndMetadata(String accountName, String activityName, String exportType, String metdata) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.export.request")
                .addField("elementId", interpolate(nameFrom(activityName, "id")))
                .addField("exportType", exportType)
                .addField("metadata", metdata).build());
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.exportId", nameFrom(activityName, "exportId")));
    }
}
