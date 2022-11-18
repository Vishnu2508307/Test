package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.CoursewarePublishHelper.getProjectActivityPublishErrorResponse;
import static mercury.helpers.courseware.CoursewarePublishHelper.getProjectActivityPublishRequest;
import static mercury.helpers.courseware.CoursewarePublishHelper.getProjectActivityPublishUpdateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class CoursewarePublishSteps {

    private static final Map<String, Integer> errorCodesMap = new HashMap<String, Integer>() {
        {
            put("UNAUTHORIZED", HttpStatus.SC_UNAUTHORIZED);
            put("BAD REQUEST", HttpStatus.SC_BAD_REQUEST);
        }
    };

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" publishes \"([^\"]*)\" activity$")
    public void publishesActivity(String user, String activityId) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityId, "id")),
                interpolate("cohort_id")
        ));
    }

    @Then("^the activity is not published due to \"([^\"]*)\" error$")
    public void theActivityIsNotPublishedDueToError(final String errorType) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                getProjectActivityPublishErrorResponse(errorCodesMap.get(errorType))
        ));
    }

    @Then("^\"([^\"]*)\" activity is successfully published at \"([^\"]*)\"$")
    public void activityIsSuccessfullyPublishedAt(final String activityName, final String deploymentName) {
        theActivityIsSuccessfullyPublishedAt(activityName, deploymentName, "cohort_id");
    }

    private void theActivityIsSuccessfullyPublishedAt(final String activityName, final String deploymentName, final String cohortId) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.deployment.id", "@variable('" + nameFrom(deploymentName, "id") + "')@")
                .jsonPath("$.response.deployment.changeId", "@variable('" + nameFrom(deploymentName, "change_id") + "')@")
                .extractFromPayload("$.response.deployment.activityId", nameFrom(deploymentName, "activity_id"))
                .jsonPath("$.response.deployment.activityId", interpolate(nameFrom(activityName, "id")))
                .jsonPath("$.response.deployment.cohortId", interpolate(cohortId)));
    }

    @When("^\"([^\"]*)\" publishes \"([^\"]*)\" activity to update \"([^\"]*)\"$")
    public void publishesActivityToUpdate(String user, String activityId, String deploymentId) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishUpdateRequest(
                interpolate(nameFrom(activityId, "id")),
                interpolate(nameFrom(deploymentId, "id")),
                interpolate("cohort_id")
        ));
    }

    @And("^\"([^\"]*)\" and \"([^\"]*)\" have different$")
    public void andHaveDifferent(final String deploymentIdOne, final String deploymentIdTwo, List<String> fields) {
        fields.forEach(field -> {
            Object valOne = runner.variable(nameFrom(deploymentIdOne, field), interpolate(nameFrom(deploymentIdOne, field)));
            Object valTwo = runner.variable(nameFrom(deploymentIdTwo, field), interpolate(nameFrom(deploymentIdTwo, field)));
            assertNotEquals(valOne, valTwo);
        });
    }

    @Given("^\"([^\"]*)\" has published \"([^\"]*)\" activity to \"([^\"]*)\"$")
    public void hasPublishedActivityTo(String user, String activityId, String deploymentIdName) {
        publishesActivity(user, activityId);
        activityIsSuccessfullyPublishedAt(activityId, deploymentIdName);
    }

    @And("{string} has published {string} activity to cohort {string} as {string}")
    public void hasPublishedActivityToCohortAs(String accountName, String activityName, String cohortName, String deploymentName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(cohortName, "id"))
        ));
        theActivityIsSuccessfullyPublishedAt(activityName, deploymentName, nameFrom(cohortName, "id"));
    }

    @And("^\"([^\"]*)\" and \"([^\"]*)\" have same$")
    public void andHaveSame(final String deploymentIdOne, final String deploymentIdTwo, List<String> fields) {
        fields.forEach(field -> {
            Object valOne = runner.variable(nameFrom(deploymentIdOne, field), interpolate(nameFrom(deploymentIdOne, field)));
            Object valTwo = runner.variable(nameFrom(deploymentIdTwo, field), interpolate(nameFrom(deploymentIdTwo, field)));
            assertEquals(valOne, valTwo);
        });
    }

    @When("^\"([^\"]*)\" publishes \"([^\"]*)\" activity to cohort \"([^\"]*)\"$")
    public void publishesActivityToCohort(String user, String activityName, String cohortName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityName, "id")),
                interpolate("cohort_id")
        ));
    }

    @And("{string} publishes {string} activity to {string} cohort as {string}")
    public void publishesActivityToCohort_(final String accountName, final String activityName, final String cohortName,
                                           final String deploymentName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(cohortName, "id"))
        ));

        theActivityIsSuccessfullyPublishedAt(activityName, deploymentName, nameFrom(cohortName, "id"));
    }

    @When("{string} publishes {string} activity from a project")
    public void publishesActivityFromAProject(String user, String activityId) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityId, "id")),
                interpolate("cohort_id")
        ));
    }

    @When("{string} publishes {string} activity from a project and {string} resolve plugin version")
    public void publishesActivityFromAProjectAndResolvePluginVersion(String user,
                                                                     String activityId,
                                                                     String lockPluginVersionEnabled) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityId, "id")),
                interpolate("cohort_id"),
                Boolean.valueOf(lockPluginVersionEnabled)
        ));
    }

    @Then("the activity is not published from a project due to {string} error")
    public void theActivityIsNotPublishedFromAProjectDueToError(final String errorType) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                getProjectActivityPublishErrorResponse(errorCodesMap.get(errorType))
        ));
    }

    @And("{string} has published {string} activity from a project to {string}")
    public void hasPublishedActivityFromAProjectTo(String user, String activityId, String deploymentIdName) {
        publishesActivityFromAProject(user, activityId);
        activityIsSuccessfullyPublishedAt(activityId, deploymentIdName);
    }

    @When("{string} publishes {string} activity from a project to update {string}")
    public void publishesActivityFromAProjectToUpdate(String user, String activityId, String deploymentId) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishUpdateRequest(
                interpolate(nameFrom(activityId, "id")),
                interpolate(nameFrom(deploymentId, "id")),
                interpolate("cohort_id")
        ));
    }

    @When("{string} publishes {string} activity from a project to cohort {string}")
    public void publishesActivityfromAProjectToCohort(String user, String activityName, String cohortName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityName, "id")),
                interpolate("cohort_id")
        ));
    }

    @And("{string} has published {string} activity to cohort {string} from a project with name {string}")
    public void hasPublishedActivityToCohortFromAProject(String accountName, String activityName, String cohortName,
                                                         String deploymentName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, getProjectActivityPublishRequest(
                interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(cohortName, "id"))
        ));

        theActivityIsSuccessfullyPublishedAt(activityName, deploymentName, nameFrom(cohortName, "id"));
    }
}
