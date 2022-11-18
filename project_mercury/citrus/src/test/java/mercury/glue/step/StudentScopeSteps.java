package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.google.common.base.Strings;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class StudentScopeSteps {

    @CitrusResource
    private TestRunner runner;


    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("^\"([^\"]*)\" sets \"([^\"]*)\" studentScope using element \"([^\"]*)\" in deployment \"([^\"]*)\" with data$")
    public void setsStudentScopeUsingElementInDeploymentWithData(String user, String studentScopeName, String elementName,
                                                                 String deploymentName, String data) {
        authenticationSteps.authenticatesViaIes(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.student.scope.set")
                .addField("sourceId", interpolate(nameFrom(elementName, "id")))
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("studentScopeURN", interpolate(nameFrom(studentScopeName, "studentScope")))
                .addField("data", data)
                .build());
    }

    @Then("^the student scope is not set due to invalid data$")
    public void theStudentScopeIsNotSetDueToInvalidData() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"learner.student.scope.set.error\"," +
                                "\"code\":422," +
                                "\"message\":\"data has invalid fields: [invalidProperty]\"," +
                                "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the student scope is successfully set with data$")
    public void theStudentScopeIsSuccessfullySet(String data) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "learner.student.scope.set.ok")
                .jsonPath("$.response.studentScope.scopeId", "@notEmpty()@")
                .jsonPath("$.response.studentScope.id", "@notEmpty()@")
                .jsonPath("$.response.studentScope.sourceId", "@notEmpty()@")
                .jsonPath("$.response.studentScope.data", data));
    }

    @Then("^the \"([^\"]*)\" student scope is not set due to element \"([^\"]*)\" not registered$")
    public void theStudentScopeIsNotSetDueToElementNotRegistered(String studentScope, String elementName) {
        final String message = String.format("Element `%s` not found in registry for studentScopeUrn `%s`",
                interpolate(nameFrom(elementName, "id")), interpolate(nameFrom(studentScope, "studentScope")));

        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"learner.student.scope.set.error\"," +
                        "\"code\":422," +
                        "\"message\":\""+message+"\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));//6fa2a5d0-2fe4-11e9-ac06-9fe9d4a08ead
    }

    @Given("^\"([^\"]*)\" has set \"([^\"]*)\" studentScope using element \"([^\"]*)\" in deployment \"([^\"]*)\" with data$")
    public void hasSetStudentScopeUsingElementInDeploymentWithData(String user, String studentScopeName, String sourceIdName,
                                                                   String deploymentName, String data) {
        setsStudentScopeUsingElementInDeploymentWithData(user, studentScopeName, sourceIdName, deploymentName, data);
        theStudentScopeIsSuccessfullySet(data);
    }

    @Given("^\"([^\"]*)\" has subscribed to student scope in deployment \"([^\"]*)\"$")
    public void hasSubscribedToStudentScopeInDeployment(String user, String deploymentName) {

        authenticationSteps.authenticatesViaIes(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.student.scope.subscribe")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "learner.student.scope.subscribe.ok"));
    }

    @Then("^\"([^\"]*)\" gets student scope broadcasts for \"([^\"]*)\" and student scope \"([^\"]*)\" set by \"([^\"]*)\" with data$")
    public void getsStudentScopeBroadcastsForAndStudentScopeSetByWithData(String user, String deploymentName, String studentScopeName,
            String elementName, String data) {

        AtomicInteger expectedMessages = new AtomicInteger(2);
        while (expectedMessages.get() > 0) {
            messageOperations.receiveJSON(runner,
                    action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                        @Override
                        public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                            if ("learner.student.scope.broadcast".equals(payload.get("type"))) {
                                expectedMessages.decrementAndGet();

                                Map response = (Map) payload.get("response");
                                assertEquals(context.getVariable(nameFrom(deploymentName, "id")), response.get("deploymentId"));
                                String studentScopeExpected = context.getVariable(nameFrom(studentScopeName, "studentScope"));
                                String studentScopeURN = (String) response.get("studentScopeURN");
                                assertEquals(studentScopeExpected, studentScopeURN);
                                assertEquals("STUDENT_SCOPE",response.get("rtmEvent"));

                                Map studentScope = (Map) response.get("studentScope");
                                assertFalse(Strings.isNullOrEmpty((String) studentScope.get("scopeId")));
                                assertFalse(Strings.isNullOrEmpty((String) studentScope.get("id")));
                                assertEquals(context.getVariable(nameFrom(elementName, "id")), studentScope.get("sourceId"));
                                assertEquals(data, studentScope.get("data"));
                            } else {
                                expectedMessages.decrementAndGet();
                                assertEquals("learner.student.scope.set.ok", payload.get("type"));
                            }
                        }
                    }));
        }
    }

    @When("^\"([^\"]*)\" un-subscribes from student scope in deployment \"([^\"]*)\"$")
    public void unsubscribesFromStudentScopeInDeployment(String user, String deploymentName) {
        authenticationSteps.authenticatesViaIes(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.student.scope.unsubscribe")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "learner.student.scope.unsubscribe.ok"));
    }
}
