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

public class FavoriteSteps {

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



    @Given("{string} had added activity {string} to favorite from {string} inside project {string} in workspace {string}")
    public void hadAddedActivityToFavoriteFromInsideProjectInWorkspace(String user,
                                                                       String activityName,
                                                                       String rootElementName,
                                                                       String projectName,
                                                                       String workspaceName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "user.content.favorite.create")
                .addField("accountId",  interpolate(getAccountIdVar(user)))
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .addField("resourceType", "LESSON")
                .build());

    }

    @Then("{string} successfully added activity {string} favorite")
    public void successfullyAddedActivityToFavorite(String user, String courseOrLesson) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.favorite.create.ok\"," +
                                       "\"response\":{\"favoriteId\":\"@notEmpty()@\"}," +
                                       "\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.favoriteId", nameFrom(courseOrLesson, "favoriteId")));
    }

    @Given("{string} can list activity added to favorite")
    public void hadRetrievedActivityToFavoriteFromInsideProjectInWorkspace(String user) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "user.content.favorite.get")
                .addField("accountId",  interpolate(getAccountIdVar(user)))
                .build());

    }

    @Then("{string} successfully listed activity to favorite")
    public void successfullyListedActivityToFavorite(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.favorite.get.ok\"," +
                                       "\"response\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("{string} can remove activity {string} to favorite from {string} inside project {string} in workspace {string} added to favorite")
    public void canRemoveActivityToFavoriteFromInsideProjectInWorkspaceAddedToFavorite(String user,
                                                                                       String activityName,
                                                                                       String rootElementName,
                                                                                       String projectName,
                                                                                       String workspaceName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "user.content.favorite.remove")
                .addField("favoriteId", interpolate(nameFrom(activityName, "favoriteId")))//TODO NEED favorite ID from interpolation
                .addField("accountId", interpolate(getAccountIdVar(user)))
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .build());
    }

    @Then("{string} successfully removed activity from favorite")
    public void successfullyRemovedActivityFromFavorite(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.favorite.remove.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
