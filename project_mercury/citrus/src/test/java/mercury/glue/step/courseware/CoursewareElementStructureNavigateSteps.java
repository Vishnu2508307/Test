package mercury.glue.step.courseware;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.payload.GetCoursewareElementStructureNavigatePayload;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CoursewareElementStructureNavigateSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches the courseware element structure navigate for {string} {string}")
    public void fetchCoursewareElementStructure(String user, String elementType, String elementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.courseware.structure.navigate")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware element structure navigate is not returned due to missing permission level")
    public void theCoursewareElementStructureFetchFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.structure.navigate.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));

    }

    @Then("the courseware element structure navigate is returned successfully for {string}")
    public void theCoursewareElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
            action.payload("{" +
                "\"type\":\"author.courseware.structure.navigate.ok\"," +
                "\"response\":{" +
                "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"," +
                    "\"parentId\":\"@notEmpty()@\"" +
                "}" +
                "},\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware element structure navigate is returned successfully for {string} with children status {string}")
    public void theCoursewareRootElementStructureisReturnedSuccessfully(String elementName, String hasChildrenStatus) {

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(new ResponseMessageValidationCallback<GetCoursewareElementStructureNavigatePayload>(GetCoursewareElementStructureNavigatePayload.class) {
                    @Override
                    public void validate(GetCoursewareElementStructureNavigatePayload payload, Map<String, Object> headers, TestContext context) {
                        payload.getChildren().iterator().next()//Get PATHWAY
                                .getChildren().stream().iterator()//Get children of PATHWAY
                                .forEachRemaining(getCoursewareElementStructureNavigatePayload -> {
                                    assertAll(()-> {
                                        Assert.assertEquals(hasChildrenStatus.toLowerCase(), getCoursewareElementStructureNavigatePayload.getHasChildren().toString());
                                        Assert.assertNull(getCoursewareElementStructureNavigatePayload.getChildren());
                                    });
                                });
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareStructure";
                    }

                    @Override
                    public String getType() {
                        return "author.courseware.structure.navigate.ok";
                    }
                }));
    }

    @Then("the courseware element structure navigate is returned successfully for {string} with no children")
    public void theCoursewareRootElementStructureisReturnedChildrenNull(String elementName) {

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(new ResponseMessageValidationCallback<GetCoursewareElementStructureNavigatePayload>(GetCoursewareElementStructureNavigatePayload.class) {
                    @Override
                    public void validate(GetCoursewareElementStructureNavigatePayload payload, Map<String, Object> headers, TestContext context) {
                        payload.getChildren().iterator().next()//Get PATHWAY
                                .getChildren().stream().iterator()//Get PATHWAY children
                                .forEachRemaining(getCoursewareElementStructureNavigatePayload -> {
                                    Assert.assertNull(getCoursewareElementStructureNavigatePayload.getHasChildren());
                                    Assert.assertNull(getCoursewareElementStructureNavigatePayload.getChildren());
                                });
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareStructure";
                    }

                    @Override
                    public String getType() {
                        return "author.courseware.structure.navigate.ok";
                    }
                }));
    }

    @When("{string} fetches the courseware support element structure navigate for {string} {string}")
    public void fetchCoursewareSupportElementStructure(String user, String elementType, String elementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.courseware.support.structure.navigate")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware support element structure navigate is not returned due to missing permission level")
    public void theCoursewareSupportElementStructureFetchFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.support.structure.navigate.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));

    }

    @Then("the courseware support element structure navigate is returned successfully for {string}")
    public void theCoursewareSupportElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.support.structure.navigate.ok\"," +
                    "\"response\":{" +
                    "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"," +
                    "\"parentId\":\"@notEmpty()@\"" +
                    "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware support root element structure navigate is returned successfully for {string}")
    public void theCoursewareSupportRootElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.support.structure.navigate.ok\"," +
                    "\"response\":{" +
                    "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"" +
                    "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} fetches the courseware element structure navigate for {string} {string} with field {string}")
    public void fetchCoursewareElementStructureWithConfig(String user, String elementType, String elementName, String fieldName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.courseware.structure.navigate")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("fieldNames", Arrays.asList(fieldName)).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware element structure navigate is returned successfully for {string} with fields")
    public void theCoursewareElementStructureisReturnedSuccessfullyWithFields(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.structure.navigate.ok\"," +
                    "\"response\":{" +
                        "\"coursewareStructure\":{" +
                            "\"children\":\"@notEmpty()@\"," +
                            "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                            "\"type\":\"@notEmpty()@\"," +
                            "\"topParentId\":\"@notEmpty()@\"," +
                            "\"parentId\":\"@notEmpty()@\"," +
                            "\"configFields\":\"@notEmpty()@\"" +
                        "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware root element structure navigate is returned successfully for {string} with fields")
    public void theCoursewareRootElementStructureisReturnedSuccessfullyWithFields(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.structure.navigate.ok\"," +
                    "\"response\":{" +
                        "\"coursewareStructure\":{" +
                            "\"children\":\"@notEmpty()@\"," +
                            "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                            "\"type\":\"@notEmpty()@\"," +
                            "\"topParentId\":\"@notEmpty()@\"," +
                            "\"configFields\":\"@notEmpty()@\"" +
                        "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }
}
