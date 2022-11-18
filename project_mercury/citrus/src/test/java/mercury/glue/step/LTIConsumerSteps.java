package mercury.glue.step;

import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.util.Tokens;

import cucumber.api.java.en.And;
import mercury.common.MessageOperations;

public class LTIConsumerSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @And("{string} has LTI consumer credentials {string} with key {string} and secret {string} available")
    public void hasLTIConsumerKeyAndSecretAvailable(String accountName, String credName, String ltiKeyName, String ltiSecretName) {
        final String key = Tokens.generate(24);
        final String secret = Tokens.generate(36);

        final LtiConsumerCredential creds = new LtiConsumerCredential()
                .setKey(key)
                .setSecret(secret);

        runner.variable(nameFrom(ltiKeyName, "ltiKey"), key);
        runner.variable(nameFrom(ltiSecretName, "ltiSecret"), secret);
        runner.variable(nameFrom(credName, "ltiCredentials"), creds);
    }
}
