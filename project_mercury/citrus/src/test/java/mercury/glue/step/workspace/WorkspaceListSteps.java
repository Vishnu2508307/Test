package mercury.glue.step.workspace;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;

public class WorkspaceListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" lists the workspaces$")
    public void listsTheWorkspaces(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.list").build());
    }

    @Then("^the following workspaces are returned$")
    public void theFollowingWorkspacesAreReturned(final List<String> expectedWorkspaces) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(ArrayList payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(expectedWorkspaces.size(), payload.size());
                        final Map<String, String> expected = expectedWorkspaces.stream()
                                .map(one-> Maps.newHashMap(one, one)).reduce((prev, next) ->{
                                    next.putAll(prev);
                                    return next;
                                }).orElse(new HashMap<>());

                        for (Object aPayload : payload) {
                            final Map workspacePayload = (Map) aPayload;
                            String currentName = (String) workspacePayload.get("name");
                            assertEquals(context.getVariable(expected.get(currentName) + "_workspace_id"), workspacePayload.get("id"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "workspaces";
                    }

                    @Override
                    public String getType() {
                        return "workspace.list.ok";
                    }
                }
        ));
    }

    @Then("^an empty list is returned$")
    public void anEmptyListIsReturned() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"workspace.list.ok\"," +
                        "\"response\":{" +
                        "\"workspaces\":[]}," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
