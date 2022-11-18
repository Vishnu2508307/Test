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

public class ProjectSubscribeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} subscribes to the project {string}")
    public void subscribesToProject(String accountName, String project) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.subscribe")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .build());
    }

    @Then("{string} is successfully subscribed to the project")
    public void projectIsSuccessfullySubscribed(String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.subscribe.ok")
                .jsonPath("$.response.rtmSubscriptionId", "@notEmpty()@")
        );
    }

    @When("{string} unsubscribes to the project {string}")
    public void unsubscribesToProject(String accountName, String project) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.unsubscribe")
                .addField("projectId", interpolate(nameFrom(project, "id")))
                .build());
    }

    @Then("{string} is successfully unsubscribed to the project")
    public void projectIsSuccessfullyUnsubscribed(String accountName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.unsubscribe.ok")
        );
    }
}
