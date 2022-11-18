package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class CredentialSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches credential type for an email")
    public void fetchesCredentialTypeForAnEmail(String user) {
        authenticationSteps.authenticateUser(user);

        runner.variable("randomEmail", "mercury:randomEmail()");
        String message = new PayloadBuilder()
                .addField("type", "iam.credential.list")
                .addField("email", "${randomEmail}")
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("credential type fetched successfully")
    public void credentialTypeFetchedSuccessfully() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<List>(List.class) {
                    @Override
                    public void validate(List credentialType, Map<String, Object> headers, TestContext context) {
                       assertTrue(credentialType.isEmpty());
                    }

                    @Override
                    public String getRootElementName() {
                        return "credentialTypes";
                    }

                    @Override
                    public String getType() {
                        return "iam.credential.list.ok";
                    }
                }));
    }
}
