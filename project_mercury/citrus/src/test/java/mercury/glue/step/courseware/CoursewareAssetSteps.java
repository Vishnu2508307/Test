package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.courseware.CoursewareAssetHelper.addAssetErrorResponse;
import static mercury.glue.step.courseware.CoursewareAssetHelper.addAssetRequest;
import static mercury.glue.step.courseware.CoursewareAssetHelper.removeAssetErrorResponse;
import static mercury.glue.step.courseware.CoursewareAssetHelper.removeAssetRequest;
import static mercury.glue.step.courseware.CoursewareAssetHelper.removeAssetsRequest;
import static mercury.glue.step.courseware.FeedbackHelper.GET_MESSAGE;
import static mercury.glue.step.courseware.FeedbackHelper.validateFeedbackGetResponse;
import static mercury.helpers.courseware.ActivityHelper.activityResponseOk;
import static mercury.helpers.courseware.ActivityHelper.getActivityRequest;
import static mercury.helpers.courseware.ComponentHelper.componentGetRequest;
import static mercury.helpers.courseware.ComponentHelper.validateComponentGetResponse;
import static mercury.helpers.courseware.InteractiveHelper.getInteractiveRequest;
import static mercury.helpers.courseware.InteractiveHelper.validateInteractiveGetResponse;
import static mercury.helpers.courseware.PathwayHelper.getPathway;
import static mercury.helpers.courseware.PathwayHelper.validatePathwayPayload;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.asset.service.AssetPayload;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;

public class CoursewareAssetSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" has added \"([^\"]*)\" asset to \"([^\"]*)\" ([^\"]*)")
    public void hasAddedAssetTo(String accountName, String assetName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, addAssetRequest(interpolate(nameFrom(elementName, "id")),
                elementType.toUpperCase(), interpolate(nameFrom(assetName, "urn"))));
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.courseware.asset.add.ok"));
    }

    @When("^\"([^\"]*)\" has removed \"([^\"]*)\" asset from \"([^\"]*)\" ([^\"]*)$")
    public void hasRemovedAssetFrom(String accountName, String assetName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, removeAssetRequest(interpolate(nameFrom(elementName, "id")),
                elementType.toUpperCase(), interpolate(nameFrom(assetName, "urn"))));
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.courseware.asset.remove.ok"));
    }

    @When("^\"([^\"]*)\" has removed asset from \"([^\"]*)\" ([^\"]*)$")
    public void hasRemovedAssetsFrom(String accountName, String elementName, String elementType,final List<String>  assetNames) {
        authenticationSteps.authenticateUser(accountName);

        List<String> assetNamesConvert = new ArrayList<String>();
        assetNames.forEach(assetName -> assetNamesConvert.add(interpolate(nameFrom(assetName, "urn"))));
        messageOperations.sendJSON(runner, removeAssetsRequest(interpolate(nameFrom(elementName, "id")),
                                                              elementType.toUpperCase(), assetNamesConvert));
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.courseware.assets.remove.ok"));
    }

    @Then("^\"([^\"]*)\" can not add an asset to \"([^\"]*)\" ([^\"]*) due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotAddAnAssetToActivityDueToErrorCodeMessage(String accountName, String elementName, String elementType,
                                                                int code, String errorMessage) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, addAssetRequest(interpolate(nameFrom(elementName, "id")),
                elementType.toUpperCase(), "urn:aero:" + UUID.randomUUID()));
        messageOperations.receiveJSON(runner, action -> action.payload(addAssetErrorResponse(code, errorMessage)));

    }

    @Then("^\"([^\"]*)\" can not remove an asset to \"([^\"]*)\" ([^\"]*) due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotRemoveAnAssetToActivityDueToErrorCodeMessage(String accountName, String elementName, String elementType,
                                                                   int code, String errorMessage) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, removeAssetRequest(interpolate(nameFrom(elementName, "id")),
                elementType.toUpperCase(), "urn:aero:" + UUID.randomUUID()));
        messageOperations.receiveJSON(runner, action -> action.payload(removeAssetErrorResponse(code, errorMessage)));
    }

    @Then("^\"([^\"]*)\" activity payload contains \"([^\"]*)\" asset$")
    public void activityPayloadContainsAsset(String activityName, String assetName) {
        messageOperations.sendJSON(runner, getActivityRequest(interpolate(nameFrom(activityName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, false),
                "activity",
                "author.activity.get.ok"
        )));
    }

    @Then("^\"([^\"]*)\" activity payload does not contain assets$")
    public void activityPayloadDoesNotContainAsset(String activityName) {
        messageOperations.sendJSON(runner, getActivityRequest(interpolate(nameFrom(activityName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> assertPayloadDoesNotContainAsset(payload.getAssets()),
                "activity",
                "author.activity.get.ok"
        )));
    }

    @Then("^\"([^\"]*)\" pathway payload contains \"([^\"]*)\" asset$")
    public void pathwayPayloadContainsAsset(String pathwayName, String assetName) {
        messageOperations.sendJSON(runner, getPathway(interpolate(Variables.nameFrom(pathwayName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, false)
        )));
    }

    @Then("^\"([^\"]*)\" pathway payload does not contain assets$")
    public void pathwayPayloadDoesNotContainAsset(String pathwayName) {
        messageOperations.sendJSON(runner, getPathway(interpolate(nameFrom(pathwayName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload(
                (payload, context) -> assertPayloadDoesNotContainAsset(payload.getAssets())
        )));
    }

    @Then("^\"([^\"]*)\" interactive payload contains \"([^\"]*)\" asset$")
    public void interactivePayloadContainsAsset(String interactiveName, String assetName) {
        messageOperations.sendJSON(runner, getInteractiveRequest(interpolate(Variables.nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateInteractiveGetResponse(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, false)
        )));
    }

    @Then("^\"([^\"]*)\" interactive payload does not contain assets$")
    public void interactivePayloadDoesNotContainAsset(String interactiveName) {
        messageOperations.sendJSON(runner, getInteractiveRequest(interpolate(nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateInteractiveGetResponse(
                (payload, context) -> assertPayloadDoesNotContainAsset(payload.getAssets())
        )));
    }

    @Then("^\"([^\"]*)\" component payload contains \"([^\"]*)\" asset$")
    public void componentPayloadContainsAsset(String componentName, String assetName) {
        messageOperations.sendJSON(runner, componentGetRequest(interpolate(Variables.nameFrom(componentName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateComponentGetResponse(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, false)
        )));
    }

    @Then("^\"([^\"]*)\" component payload does not contain assets$")
    public void componentPayloadDoesNotContainAsset(String componentName) {
        messageOperations.sendJSON(runner, componentGetRequest(interpolate(nameFrom(componentName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateComponentGetResponse(
                (payload, context) -> assertPayloadDoesNotContainAsset(payload.getAssets())
        )));
    }

    @Then("^\"([^\"]*)\" feedback payload contains \"([^\"]*)\" asset$")
    public void feedbackPayloadContainsAsset(String feedbackName, String assetName) {
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, nameFrom(feedbackName, "id")));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateFeedbackGetResponse(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, false)
        )));
    }

    @Then("^\"([^\"]*)\" feedback payload does not contain assets$")
    public void feedbackPayloadDoesNotContainAsset(String feedbackName) {
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, nameFrom(feedbackName, "id")));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateFeedbackGetResponse(
                (payload, context) -> assertPayloadDoesNotContainAsset(payload.getAssets())
        )));
    }

    @And("{string} activity payload contains {string} asset with a new asset id")
    public void activityPayloadContainsAssetWithNewAssetId(String activityName, String assetName) {
        messageOperations.sendJSON(runner, getActivityRequest(interpolate(nameFrom(activityName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, true),
                "activity",
                "author.activity.get.ok"
        )));
    }

    @And("^\"([^\"]*)\" pathway payload contains \"([^\"]*)\" asset with a new asset id$")
    public void pathwayPayloadContainsAssetWithNewAssetId(String pathwayName, String assetName) {
        messageOperations.sendJSON(runner, getPathway(interpolate(Variables.nameFrom(pathwayName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, true)
        )));
    }

    @And("^\"([^\"]*)\" interactive payload contains \"([^\"]*)\" asset with a new asset id$")
    public void interactivePayloadContainsAssetWithNewAssetId(String interactiveName, String assetName) {
        messageOperations.sendJSON(runner, getInteractiveRequest(interpolate(Variables.nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateInteractiveGetResponse(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, true)
        )));
    }

    @And("^\"([^\"]*)\" feedback payload contains \"([^\"]*)\" asset with a new asset id$")
    public void feedbackPayloadContainsAssetWithNewAssetId(String feedbackName, String assetName) {
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, nameFrom(feedbackName, "id")));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateFeedbackGetResponse(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, true)
        )));
    }

    @And("{string} component payload contains {string} asset with a new asset id")
    public void componentPayloadContainsAssetWithNewAssetId(String componentName, String assetName) {
        messageOperations.sendJSON(runner, componentGetRequest(interpolate(Variables.nameFrom(componentName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateComponentGetResponse(
                (payload, context) -> assertPayloadContainsAsset(payload.getAssets(), assetName, context, true)
        )));
    }

    @And("{string} activity payload contains {string} asset with a new asset id and config with new asset urn")
    public void activityPayloadContainsAssetWithNewAssetIdAndConfig(String activityName, String assetName) {
        messageOperations.sendJSON(runner, getActivityRequest(interpolate(nameFrom(activityName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> assertPayloadContainsAssetAndConfig(payload.getAssets(), assetName, payload.getConfig(), context),
                "activity",
                "author.activity.get.ok"
        )));
    }

    @And("{string} interactive payload contains {string} asset with a new asset id and config with new asset urn")
    public void interactivePayloadContainsAssetWithNewAssetIdAndConfig(String interactiveName, String assetName) {
        messageOperations.sendJSON(runner, getInteractiveRequest(interpolate(Variables.nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateInteractiveGetResponse(
                (payload, context) -> assertPayloadContainsAssetAndConfig(payload.getAssets(), assetName, payload.getConfig(), context)
        )));
    }

    @And("{string} component payload contains {string} asset with a new asset id and config with new asset urn")
    public void componentPayloadContainsAssetWithNewAssetIdAndConfig(String componentName, String assetName) {
        messageOperations.sendJSON(runner, componentGetRequest(interpolate(Variables.nameFrom(componentName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateComponentGetResponse(
                (payload, context) -> assertPayloadContainsAssetAndConfig(payload.getAssets(), assetName, payload.getConfig(), context)
        )));
    }

    @And("{string} feedback payload contains {string} asset with a new asset id and config with new asset urn")
    public void feedbackPayloadContainsAssetWithNewAssetIdAndConfig(String feedbackName, String assetName) {
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, nameFrom(feedbackName, "id")));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateFeedbackGetResponse(
                (payload, context) -> assertPayloadContainsAssetAndConfig(payload.getAssets(), assetName, payload.getConfig(), context)
        )));
    }
    
    private void assertPayloadContainsAsset(List<AssetPayload> assets, String assetName, TestContext context, Boolean newAseetId) {
        assertEquals(1, assets.size());

        if(newAseetId){
            assertNotEquals(context.getVariable(nameFrom(assetName, "urn")), assets.get(0).getUrn());
            long alfrescoMetadataCount = assets.get(0).getMetadata()
                            .keySet()
                            .stream()
                            .filter(key -> key.startsWith("alfresco")).count();
            assertEquals(0, alfrescoMetadataCount);
        }else{
            assertEquals(context.getVariable(nameFrom(assetName, "urn")), assets.get(0).getUrn());
        }
    }

    private void assertPayloadDoesNotContainAsset(List<AssetPayload> assets) {
        assertNull(assets);
    }

    private void assertPayloadContainsAssetAndConfig(List<AssetPayload> assets, String assetName, String config, TestContext context){
        assertEquals(1, assets.size());
        assertNotEquals(context.getVariable(nameFrom(assetName, "urn")), assets.get(0).getUrn());
        assertEquals(true, config.contains(assets.get(0).getUrn()));
        assertEquals(false, config.contains(context.getVariable(nameFrom(assetName, "urn"))));
    }
}
