package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
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
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;

public class BreadcrumbSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("^\"([^\"]*)\" fetches a breadcrumb for ([^\"]*)$")
    public void fetchesABreadcrumbForElement(String accountName, String element) {

        authenticationSteps.authenticateUser(accountName);

        String[] elementArr = element.trim().split(" ");
        String elementName = elementArr[0];
        String elementType = elementArr[1];

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "author.courseware.breadcrumb");
        payload.addField("elementId", Variables.interpolate(Variables.nameFrom(elementName, "id")));
        payload.addField("elementType", elementType.toUpperCase());

        messageOperations.sendJSON(runner, payload.build());
    }

    @Then("^the breadcrumb should be ([^\"]*)$")
    public void theBreadcrumbShouldBeBreadcrumb(String breadcrumb) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "breadcrumb";
                    }

                    @Override
                    public String getType() {
                        return "author.courseware.breadcrumb.ok";
                    }

                    @Override
                    public void validate(ArrayList payload, Map<String, Object> headers, TestContext context) {
                        String[] path = breadcrumb.trim().split("/");

                        assertEquals(path.length, payload.size());
                        int i = 0;
                        for (Object item : payload) {
                            Map element = (Map) item;
                            assertEquals(context.getVariable(Variables.nameFrom(path[i++], "id")), element.get("elementId"));
                        }
                    }
                }));
    }

    @Then("^the breadcrumb fetching fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theBreadcrumbFetchingFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.courseware.breadcrumb.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^\"([^\"]*)\" can not fetch a breadcrumb for ([^\"]*) due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchABreadcrumbForDueToErrorCodeMessage(String accountName, String element, int code, String message) {
        fetchesABreadcrumbForElement(accountName, element);
        theBreadcrumbFetchingFailsWithMessageAndCode(message, code);
    }

    @Then("^\"([^\"]*)\" can fetch a breadcrumb for ([^\"]*) successfully$")
    public void canFetchABreadcrumbForSuccessfully(String accountName, String element) {
        fetchesABreadcrumbForElement(accountName, element);
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.courseware.breadcrumb.ok"));
    }

    @Then("^the breadcrumb should have ([^\"]*) ([^\"]*) ([^\"]*)$")
    public void theBreadcrumbShouldHaveWorkspaceProjectBreadcrumb(String workspaceName, String projectName, String breadcrumb) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
            @Override
            public void validate(BasicResponseMessage message, Map<String, Object> headers, TestContext context) {
                Map<String, Object> response = message.getResponse();

                Assertions.assertEquals(context.getVariable(getWorkspaceIdVar(workspaceName)), response.get("workspaceId"));
                Assertions.assertEquals(context.getVariable(nameFrom(projectName, "id")), response.get("projectId"));
                List<Object> payload = (List<Object>) response.get("breadcrumb");
                String[] path = breadcrumb.trim().split("/");

                assertEquals(path.length, payload.size());
                int i = 0;
                for (Object item : payload) {
                    Map element = (Map) item;
                    assertEquals(context.getVariable(Variables.nameFrom(path[i++], "id")), element.get("elementId"));
                }
            }
        }));
    }
}
