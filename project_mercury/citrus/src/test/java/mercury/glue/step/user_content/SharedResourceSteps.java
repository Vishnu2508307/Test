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

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class SharedResourceSteps {

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



    @And("{string} had shared lesson {string} to with {string}")
    public void hadShard(String user,
                       String resourceId,
                       String sharedUserId) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "user.content.shared.resource.create")
                .addField("accountId",  interpolate(getAccountIdVar(user)))
                .addField("sharedAccountId", interpolate(nameFrom(sharedUserId, "id")))
                .addField("resourceId", interpolate(nameFrom(resourceId, "id")))
                .addField("resourceType", "LESSON")
                .build());

    }

    @And("{string} had shared course {string} to with {string}")
    public void hadShardCourse(String user,
                         String resourceId,
                         String sharedUserId) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "user.content.shared.resource.create")
                .addField("accountId",  interpolate(getAccountIdVar(user)))
                .addField("sharedAccountId", interpolate(nameFrom(sharedUserId, "id")))
                .addField("resourceId", interpolate(nameFrom(resourceId, "id")))
                .addField("resourceType", "COURSE")
                .build());

    }

    @Then("{string} successfully shared")
    public void hadSharedSuccessfully(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.shared.resource.create.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} failed to share")
    public void failedToShare(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"user.content.shared.resource.create.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
