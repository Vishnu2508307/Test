package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.helpers.courseware.AncestryHelper;

public class AncestrySteps {
    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches the courseware ancestry for {string} in workspace {string}")
    public void fetchesTheCoursewareAncestryForInWorkspace(String user, String elementName, String workspaceName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, AncestryHelper.getCoursewareAncestry(
                interpolate(getWorkspaceIdVar(workspaceName)),
                interpolate(nameFrom(elementName, "id"))
        ));
    }

    @Then("the courseware element type is {string} and the ancestry has")
    public void theCoursewareElementTypeIsAndTheAncestryHas(String elementType, List<String> expectedAncestry) {
        verifyAncestry(elementType, expectedAncestry, "$.response.data.workspace.getCoursewareAncestry.");
    }

    @When("{string} fetches the learner element ancestry for {string} in deployment {string}")
    public void fetchesTheLearnerElementAncestryForInDeployment(String user, String elementName, String deploymentName) {
        authenticationSteps.authenticatesViaIes(user);

        messageOperations.sendGraphQL(runner, AncestryHelper.getLearnerAncestry(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(elementName, "id"))
        ));
    }

    @Then("the learner element type is {string} and the ancestry has")
    public void theLearnerElementTypeIsAndTheAncestryHas(String elementType, List<String> expectedAncestry) {
        verifyAncestry(elementType, expectedAncestry, "$.response.data.learn.cohort.deployment[0].getLearnerAncestry.");
    }

    private void verifyAncestry(String elementType, List<String> expectedAncestry, String path) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(path + "type", elementType)
                        .jsonPath(path + "ancestry[0].elementId", interpolate(nameFrom(expectedAncestry.get(0), "id")))
                        .jsonPath(path + "ancestry[1].elementId", interpolate(nameFrom(expectedAncestry.get(1), "id")))
                        .jsonPath(path + "ancestry[2].elementId", interpolate(nameFrom(expectedAncestry.get(2), "id"))));
    }
}
