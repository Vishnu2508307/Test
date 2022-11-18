package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class ProjectFindSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} tries to fetch project info for element {string} and element type {string}")
    public void triesToFetchProjectInfoForElementAndElementType(String user, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.courseware.project.find")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .build());
    }

    @Then("{string} fetch following project information successfully")
    public void fetchFollowingProjectInformationSuccessfully(String user, Map<String, String> values) {
        authenticationSteps.authenticateUser(user);
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.courseware.project.find.ok\"," +
                                       "\"response\":{" +
                                       "\"projectSummary\":{" +
                                       "\"id\":\"" + interpolate(nameFrom(values.get("projectName"), "id")) + "\"," +
                                       "\"workspaceId\":\"" + interpolate(getWorkspaceIdVar(values.get("workspaceName"))) + "\"," +
                                       "\"createdAt\": \"@notEmpty()@\"," +
                                       "\"name\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
        );
    }

    @Then("the project successfully is not fetched successfully")
    public void theProjectSuccessfullyIsNotFetchedSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.courseware.project.find.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
