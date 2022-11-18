package mercury.glue.step.project;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class ProjectFetchSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("{string} fetches project {string}")
    public void fetchesProject(String accountName, String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.get")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @Then("the project is not fetched due to missing proper permission")
    public void theProjectIsNotFetchedDueToMissingProperPermission() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.get.error")
                .jsonPath("$.code", HttpStatus.SC_UNAUTHORIZED));
    }

    @Then("{string} is able to fetch project {string}")
    public void isAbleToFetchProject(String accountName, String projectName) {
        fetchesProject(accountName, projectName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.get.ok")
                .jsonPath("$.response.project", "@notEmpty()@")
                .jsonPath("$.response.project.permissionLevel", "@notEmpty()@")
                .jsonPath("$.response.project.id", interpolate(nameFrom(projectName, "id")))
        );
    }
}
