package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class StudentScopeRegisterSteps {
    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("^\"([^\"]*)\" register \"([^\"]*)\" \"([^\"]*)\" element to \"([^\"]*)\" student scope$")
    public void registerElementToStudentScope(String user, String elementName, String elementType, String scopeName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.student.scope.register")
                .addField("studentScopeURN", interpolate(nameFrom(scopeName, "studentScope")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .build());
    }

    @Then("^the \"([^\"]*)\" request is not permitted$")
    public void theRequestIsNotPermitted(String actionType) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", String.format("author.student.scope.%s.error", actionType)));
    }

    @Then("^the register request fails due to \"([^\"]*)\"$")
    public void theRegisterRequestFailsDueTo(String message) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.student.scope.register.error")
                .jsonPath("$.message", message));
    }

    @Then("^the element is successfully registered$")
    public void theElementIsSuccessfullyRegistered() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.student.scope.register.ok"));
    }

    @Given("^\"([^\"]*)\" has registered \"([^\"]*)\" \"([^\"]*)\" element to \"([^\"]*)\" student scope$")
    public void hasRegisteredElementToStudentScope(String user, String elementName, String elementType, String scopeName) {
        registerElementToStudentScope(user, elementName, elementType, scopeName);
        theElementIsSuccessfullyRegistered();
    }

    @When("^\"([^\"]*)\" de-register \"([^\"]*)\" \"([^\"]*)\" element from \"([^\"]*)\" student scope$")
    public void deRegisterElementFromStudentScope(String user, String elementName, String elementType, String scopeName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.student.scope.deregister")
                .addField("studentScopeURN", interpolate(nameFrom(scopeName, "studentScope")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .build());
    }

    @Then("^the element is successfully de-registered$")
    public void theElementIsSuccessfullyDeRegistered() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.student.scope.deregister.ok"));
    }

    @Then("{string} can list the following sources registered for a scope {string} with element {string} of type {string}")
    public void canListTheFollowingSourcesRegisteredForAScope(String user, String scopeName, String elementName, String elementType, final Map<String, String> expectedSourcesByScope) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.source.scope.list")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("scopeURN", interpolate(nameFrom(scopeName, "studentScope")))
                .addField("elementType", elementType)
                .build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualSourcesByScope, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedSourcesByScope.size(), actualSourcesByScope.size());

                        List<Map.Entry<String, String>> expectedEntries = new ArrayList<>(expectedSourcesByScope.entrySet());

                        for (int i = 0; i < actualSourcesByScope.size(); i++) {
                            final Map actualChangelog = (Map) actualSourcesByScope.get(i);
                            final Map.Entry<String, String> expectedEntry = expectedEntries.get(i);
                            final String expectedOnElementId = context.getVariable(interpolate(nameFrom(expectedEntry.getKey(), "id")));
                            assertEquals(expectedOnElementId, actualChangelog.get("elementId"));
                            assertEquals(expectedEntry.getValue(), actualChangelog.get("elementType"));
                            assertEquals(context.getVariable(interpolate(nameFrom(scopeName, "studentScope"))), actualChangelog.get("studentScopeUrn"));
                            assertNotNull(actualChangelog.get("configurationFields"));
                            assertNotNull(actualChangelog.get("configSchema"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "registeredScopeReference";
                    }

                    @Override
                    public String getType() {
                        return "author.source.scope.list.ok";
                    }
                }));

    }

    @Then("{string} list empty sources registered for a scope {string} with element {string} of type {string}")
    public void listEmptySourcesRegisteredForAScope(String user, String scopeName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.source.scope.list")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("scopeURN", interpolate(nameFrom(scopeName, "studentScope")))
                .addField("elementType", elementType)
                .build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualSourcesByScope, final Map<String, Object> headers, final TestContext context) {
                        assertTrue(actualSourcesByScope.isEmpty());
                    }

                    @Override
                    public String getRootElementName() {
                        return "registeredScopeReference";
                    }

                    @Override
                    public String getType() {
                        return "author.source.scope.list.ok";
                    }
                }));
    }

    @When("{string} tries to list all sources registered for a scope {string} with element {string} of type {string}")
    public void triesToListAllSourcesRegisteredForAScopeWithElementOfType(String user, String scopeName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.source.scope.list")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("scopeURN", interpolate(nameFrom(scopeName, "studentScope")))
                .addField("elementType", elementType)
                .build());
    }

    @Then("it fails because of missing permission level")
    public void itFailsBecauseOfMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.source.scope.list.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
