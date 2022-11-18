package mercury.glue.step.user_content;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.HTTP_CLIENT;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class RecentlyViewedSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(HTTP_CLIENT)
    private HttpClient httpClient;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;



    @Given("{string} had added activity {string} to recently viewed from {string} inside project {string} in workspace {string}")
    public void hadAddedActivityToRecentlyViewedFromInsideProjectInWorkspace(String user,
                                                                       String activityName,
                                                                       String rootElementName,
                                                                       String projectName,
                                                                       String workspaceName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "user.content.recently.viewed.create")
                .addField("accountId",  interpolate(getAccountIdVar(user)))
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .addField("resourceType", "LESSON")
                .build());

    }

    @Then("{string} successfully added activity to recently viewed")
    public void successfullyAddedActivityToFavorite(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.recently.viewed.create.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} failed to update recently viewed")
    public void failedToUpdateRecentlyViewed(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.recently.viewed.create.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
