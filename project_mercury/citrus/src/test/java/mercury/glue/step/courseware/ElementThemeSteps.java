package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class ElementThemeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} associate theme {string} with {string} {string}")
    public void associateThemeWithActivity(String user, String themeName, String elementType,  String activityName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.element.theme.create")
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("elementType", elementType)
                .addField("elementId", interpolate(nameFrom(activityName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("theme {string} is successfully associated with activity {string}")
    public void themeIsSuccessfullyAssociatedWithActivity(String themeName, String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.element.theme.create.ok\"," +
                                       "\"response\":{" +
                                       "\"elementTheme\":{" +
                                       "\"themeId\":\"@notEmpty()@\"," +
                                       "\"elementType\":\"@notEmpty()@\"," +
                                       "\"elementId\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.elementTheme.themeId", interpolate(nameFrom(themeName, "id")))
                        .extractFromPayload("$.response.elementTheme.elementId", interpolate(nameFrom(elementName, "id"))));
    }

    @Then("the association is unsuccessful due to missing permission level")
    public void theAssociationIsUnsuccessfulDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.element.theme.create.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} delete theme association for {string} {string}")
    public void deleteThemeFromUnit(String user, String elementType, String elementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.element.theme.delete")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the theme is successfully deleted")
    public void theThemeIsSuccessfullyDeleted() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.element.theme.delete.ok"));
    }

    @When("{string} deletes theme association for {string} {string}")
    public void deletesThemeFrom(String user, String elementType, String elementName) {
        deleteThemeFromUnit(user, elementType, elementName);
        theThemeIsSuccessfullyDeleted();
    }

    @Then("the theme deletion fails due to {string} code with message {string}")
    public void theThemeDeletionFailsDueToCodeWithMessage(String code, String message) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.element.theme.delete.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("{string} has associated theme {string} of variant {string} with {string} {string}")
    public void hasAssociatedThemeOfVariantWith(String user, String themeName, String elementType, String elementName) {
        associateThemeWithActivity(user, themeName, elementType, elementName);
        themeIsSuccessfullyAssociatedWithActivity(themeName, elementName);

    }
}
