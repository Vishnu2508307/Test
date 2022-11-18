package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.ProvisionSteps.getAccountEmailVar;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.step.courseware.ActivitySteps.ACTIVITY_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.ActivityHelper.activityDuplicateOk;
import static mercury.helpers.courseware.ActivityHelper.activityOk;
import static mercury.helpers.courseware.ActivityHelper.activityResponseOk;
import static mercury.helpers.courseware.ActivityHelper.duplicateActivityIntoPathway;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.courseware.payload.ActivityPayload;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;

public class ActivityDuplicateSteps {

    public static final String ACTIVITY_COPY_ID_VAR = "activity_copy_id";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" duplicates \"([^\"]*)\" activity into \"([^\"]*)\" pathway$")
    public void duplicatesActivityIntoPathway(String accountName, String activityName, String pathwayName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, duplicateActivityIntoPathway(interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(pathwayName, "id")), null, null));
    }

    @When("{string} duplicates {string} activity into {string} pathway while new duplicate flow is {string}")
    public void duplicatesActivityIntoPathwayWithNewDuplicationFlow(String accountName, String activityName, String pathwayName, String onOff) {
        authenticationSteps.authenticateUser(accountName);
        duplicatesActivityIntoPathwayAtPositionWithNewDuplicateFlow(accountName, activityName, pathwayName, -1, onOff);
    }

    @When("^\"([^\"]*)\" duplicates \"([^\"]*)\" activity into \"([^\"]*)\" pathway at position (\\d+)$")
    public void duplicatesActivityIntoPathwayAtPosition(String accountName, String activityName, String pathwayName, int index) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, duplicateActivityIntoPathway(interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(pathwayName, "id")), index, null));
    }

    @When("{string} duplicates {string} activity into {string} pathway at position {int} while new duplicate flow is {string}")
    public void duplicatesActivityIntoPathwayAtPositionWithNewDuplicateFlow(String accountName, String activityName, String pathwayName, int index, String onOff) {
        authenticationSteps.authenticateUser(accountName);
        Boolean newDuplicateFlow = false;

        if(onOff.equalsIgnoreCase("on")){
            newDuplicateFlow = true;
        }
        messageOperations.sendJSON(runner, duplicateActivityIntoPathway(interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(pathwayName, "id")), index, newDuplicateFlow));
    }

    @Then("^the new copy is successfully created$")
    public void theNewCopyIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {

            @Override
            public void validate(ActivityPayload payload, Map<String, Object> headers, TestContext context) {
                assertNotNull(payload.getActivityId());
                assertNotEquals(context.getVariable(ACTIVITY_ID_VAR), payload.getActivityId().toString());
                assertEquals(context.getVariable(PLUGIN_ID_VAR), payload.getPlugin().getPluginId().toString());
                assertEquals(context.getVariable("activity_config"), payload.getConfig());
                assertEquals(context.getVariable("activity_theme_config"), payload.getActivityTheme());
                context.setVariable(ACTIVITY_COPY_ID_VAR, payload.getActivityId());
            }

            @Override
            public String getRootElementName() {
                return "activity";
            }

            @Override
            public String getType() {
                return "author.activity.duplicate.ok";
            }
        }));
    }

    @Then("^the \"([^\"]*)\" activity has been successfully duplicated$")
    public void theActivityHasBeenSuccessfullyDuplicated(String copiedActivityName) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(activityDuplicateOk((payload, context) -> {
                    assertNotNull(payload);
                    context.setVariable(nameFrom(copiedActivityName, "id"), payload.getActivityId().toString());
                })));
    }

    @Then("^the activity duplication fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theActivityDuplicationFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.activity.duplicate.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    public static String replaceByIds(TestRunner runner, String str) {
        String foo = Arrays.stream(str.split(","))
                .map(entity -> {
                    String id = runner.variable(nameFrom(entity, "id"), interpolate(nameFrom(entity, "id")));
                    return "\"" + entity.replace("_COPY", "") + "\":\"" + id + "\"";
                })
                .collect(Collectors.joining(","));
        return "{" + foo + "}";
    }

    @Then("^\"([^\"]*)\" can not duplicate \"([^\"]*)\" activity into \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDuplicateActivityIntoPathwayDueToErrorCodeMessage(String accountName, String activityName,
                                                                        String pathwayName, int code, String message) {
        duplicatesActivityIntoPathway(accountName, activityName, pathwayName);
        theActivityDuplicationFailsWithMessageAndCode(message, code);
    }

    @Then("^\"([^\"]*)\" can duplicate \"([^\"]*)\" activity into \"([^\"]*)\" pathway$")
    public void canDuplicateActivityIntoPathwaySuccessfully(String accountName, String activityName, String pathwayName) {
        duplicatesActivityIntoPathway(accountName, activityName, pathwayName);
        theActivityHasBeenSuccessfullyDuplicated(nameFrom(activityName, "COPY"));
    }

    @When("{string} duplicates {string} to project {string}")
    public void duplicatesToProject(String accountName, String activityName, String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.duplicate")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @When("{string} duplicates {string} to project {string} while new duplicate flow is {string}")
    public void duplicatesToProjectWithNewDuplicateFlowIsOn(String accountName, String activityName, String projectName, String onOff) {
        authenticationSteps.authenticateUser(accountName);

        Boolean newDuplicateFlow = false;

        if(onOff.equalsIgnoreCase("on")){
            newDuplicateFlow = true;
        }

        PayloadBuilder payloadBuilder = new PayloadBuilder()
                .addField("type", "project.activity.duplicate")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("newDuplicateFlow", newDuplicateFlow);

        messageOperations.sendJSON(runner, payloadBuilder.build());
    }

    @Then("the new copy {string} is successfully created inside project {string}")
    public void theNewCopyIsSuccessfullyCreatedInsideProject(String activityName, String projectName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> {
                    assertNotNull(payload.getActivityId().toString());
                    assertEquals(context.getVariable(PLUGIN_ID_VAR), payload.getPlugin().getPluginId().toString());
                    context.setVariable(nameFrom(activityName, "id"), payload.getActivityId().toString());
                },
                "activity",
                "project.activity.duplicate.ok")));
    }

    @Then("the new copy {string} is successfully created inside project {string} with {string} as creator")
    public void theNewCopyIsSuccessfullyCreatedInsideProjectWithAsCreator(String activityName, String projectName, String accountName) {
        messageOperations.receiveJSON(runner, action -> action.payload(activityOk(
                "project.activity.duplicate.ok",
                "@variable('" + nameFrom(activityName, "id") + "')@",
                interpolate(PLUGIN_ID_VAR),
                interpolate(getAccountEmailVar(accountName)),
                interpolate(getAccountIdVar(accountName))
        )));
    }
}
