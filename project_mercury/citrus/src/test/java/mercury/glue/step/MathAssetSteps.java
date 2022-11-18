package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class MathAssetSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates a math asset for the {string} {string} with")
    public void createsAMathAssetWith(String user,
                                      String elementName,
                                      String elementType,
                                      final Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.math.asset.create")
                .addField("mathML", args.get("mathML"))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType.toUpperCase())
                .addField("altText", args.get("altText"))
                .build()
        );
    }

    @Then("the math asset {string} is successfully created")
    public void theMathAssetIsCreatedSuccessfully(String assetName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.math.asset.create.ok")
                        .jsonPath("$.response.assetUrn", "@notEmpty()@")
                        .extractFromPayload("$.response.assetUrn", nameFrom(assetName, "assetUrn"))
        );
    }

    @When("{string} fetches the math asset {string}")
    public void fetchesAMathAsset(String user, String assetName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.math.asset.get")
                .addField("urn", interpolate(nameFrom(assetName, "assetUrn")))
                .build()
        );
    }

    @Then("the math asset {string} is successfully fetched")
    public void theMathAssetIsFetchedSuccessfully(String assetName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.math.asset.get.ok")
                        .jsonPath("$.response.asset", "@notEmpty()@")
                        .jsonPath("$.response.asset.id", "@notEmpty()@")
        );
    }

    @When("{string} fetches the learner math asset {string}")
    public void fetchesALearnerMathAsset(String user, String assetName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.math.asset.get")
                .addField("urn", interpolate(nameFrom(assetName, "assetUrn")))
                .build()
        );
    }

    @Then("the learner math asset {string} is successfully fetched")
    public void theLearnerMathAssetIsFetchedSuccessfully(String assetName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "learner.math.asset.get.ok")
                        .jsonPath("$.response.asset", "@notEmpty()@")
                        .jsonPath("$.response.asset.id", "@notEmpty()@")
        );
    }

    @Then("the math asset is not created due to missing permission level")
    public void theMathAssetIsNotCreated() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.math.asset.create.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} removes a math asset {string} from the {string} {string}")
    public void removesAMathAssetWith(String user, String assetName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.math.asset.remove")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType.toUpperCase())
                .addField("assetUrn", interpolate(nameFrom(assetName, "assetUrn")))
                .build()
        );
    }

    @Then("the math asset {string} is successfully removed")
    public void theMathAssetIsRemovedSuccessfully(String assetName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.math.asset.remove.ok")
        );
    }

    @Then("the math asset is not removed due to missing permission level")
    public void theMathAssetIsNotRemoved() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.math.asset.remove.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
