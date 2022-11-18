package mercury.glue.step;

import static junit.framework.TestCase.assertTrue;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;

public class EditRoleSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" \"([^\"]*)\"s \"([^\"]*)\" role to \"([^\"]*)\"'s account$")
    public void roleToSAccount(String user, String action, String role, String account) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.role." + action)
                .addField("accountId", Variables.interpolate(getAccountIdVar(account)))
                .addField("role", role).build());
    }

    @SuppressWarnings("unchecked")
    @Then("^the \"([^\"]*)\" role is successfully added to \"([^\"]*)\"'s account$")
    public void theRoleIsSuccessfullyAddedToSAccount(final String role, final String account) throws Throwable {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"iam.role.add.ok\"," +
                                "\"response\":{" +
                                    "\"accountId\":\""+Variables.interpolate(getAccountIdVar(account))+"\"," +
                                    "\"roles\":\"@notEmpty()@\"}," +
                                    "\"replyTo\":\"@notEmpty()@\"}")
                        .validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
                            @Override
                            public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                                List<String> roles = (List<String>) payload.getResponse().get("roles");
                                assertTrue(roles.stream().anyMatch(one-> one.equals(role)));
                            }
                        }));
    }

    @SuppressWarnings("unchecked")
    @Then("^the \"([^\"]*)\" role is successfully removed from \"([^\"]*)\"'s account$")
    public void theRoleIsSuccessfullyRemovedFromSAccount(final String role, final String account) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                    "\"type\":\"iam.role.remove.ok\"," +
                                    "\"response\":{" +
                                        "\"accountId\":\""+Variables.interpolate(getAccountIdVar(account))+"\"," +
                                        "\"roles\":\"@notEmpty()@\"" +
                                    "},\"replyTo\":\"@notEmpty()@\"}")
        .validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
            @Override
            public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                List<String> roles = (List<String>) payload.getResponse().get("roles");
                assertTrue(roles.stream().noneMatch(one-> one.equals(role)));
            }
        }));
    }

    @Then("^the role is not added due to missing permission level$")
    public void theRoleIsNotAddedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"iam.role.add.error\"," +
                                "\"code\":401," +
                                "\"message\":\"@notEmpty()@\"," +
                                "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the role is not removed due to missing permission level$")
    public void theRoleIsNotRemovedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"iam.role.remove.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the role is not added because already assigned$")
    public void theRoleIsNotAddedBecauseAlreadyAssigned() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"iam.role.add.error\"," +
                                "\"code\":400," +
                                "\"response\":{" +
                                    "\"reason\":\"@notEmpty()@\"" +
                                "},\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the role is not added due to a higher role required")
    public void theRoleIsNotAddedDueToAHigherRoleRequired() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "iam.role.add.error")
                .jsonPath("$.message", "Unauthorized: Higher role required"));
    }

    @Then("the role is not removed due to a higher role required")
    public void theRoleIsNotRemovedDueToAHigherRoleRequired() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "iam.role.remove.error")
                .jsonPath("$.message", "Unauthorized: Higher role required"));
    }
}
