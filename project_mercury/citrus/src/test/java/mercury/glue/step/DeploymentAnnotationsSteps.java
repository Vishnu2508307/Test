package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.annotation.AnnotationHelper.deploymentAccountAnnotationsQuery;
import static mercury.helpers.annotation.AnnotationHelper.deploymentAnnotationCreateMutation;
import static mercury.helpers.annotation.AnnotationHelper.deploymentAnnotationsQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mercury.common.PayloadBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.ResponseMessageValidationCallback;

public class DeploymentAnnotationsSteps {
    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates a deployment {string} annotation for element {string} and deployment {string} with")
    public void createsADeploymentAnnotationForElementAndDeploymentWith(String user, String motivation,
                                                                        String elementName, String deploymentName,
                                                                        Map<String, String> args) {

        authenticationSteps.authenticatesViaIes(user);

        messageOperations.sendGraphQL(runner, deploymentAnnotationCreateMutation(
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(elementName, "id")),
                args.get("body"),
                args.get("target"),
                motivation
        ));
    }

    @Then("the deployment annotation {string} is created succesfully")
    public void theDeploymentAnnotationIsCreatedSuccesfully(String annotationName) {
        String path = "$.response.data.AnnotationForDeploymentCreate.id";
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload(path, nameFrom(annotationName, "id")));
    }

    @Then("the deployment annotation is not created due to missing permission level")
    public void theDeploymentAnnotationIsNotCreatedDueToMissingPermissionLevel() {
        String path = "$.response.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path + "data.AnnotationForDeploymentCreate", null)
                .jsonPath(path + "errors[0].extensions.code", 403));    }

    @Given("{string} has created a deployment {string} annotation {string} for element {string} and deployment {string} with")
    public void hasCreatedADeploymentAnnotationForElementAndDeploymentWith(String user, String motivation,
                                                                           String annotationName, String elementName,
                                                                           String deploymentName, Map<String, String> args) {
        createsADeploymentAnnotationForElementAndDeploymentWith(user, motivation, elementName, deploymentName, args);
        theDeploymentAnnotationIsCreatedSuccesfully(annotationName);
    }

    @When("{string} fetches deployment account annotation by deployment {string}, motivation {string} and element {string}")
    public void fetchesDeploymentAccountAnnotationByDeploymentMotivationAndElement(String user, String deploymentName,
                                                                            String motivation, String elementName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, deploymentAccountAnnotationsQuery(
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(getAccountIdVar(user)),
                motivation,
                interpolate(nameFrom(elementName, "id"))
        ));
    }

    @SuppressWarnings("unchecked")
    @Then("the following deployment account annotations are returned")
    public void theFollowingDeploymentAccountAnnotationsAreReturned(List<String> annotationNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                List<Map> annotations = (List<Map>) data.get("AnnotationsByDeploymentAccount");

                List<String> expectedAnnotationIds = annotationNames.stream()
                        .map(annotationName -> context.getVariable(interpolate(nameFrom(annotationName, "id"))))
                        .collect(Collectors.toList());

                annotations.forEach(annotationMap -> assertTrue(expectedAnnotationIds.stream()
                        .anyMatch(one -> one.equals(annotationMap.get("id")))));
            }
        }));    }

    @When("{string} fetches deployment annotation by deployment {string}, motivation {string} and element {string}")
    public void fetchesDeploymentAnnotationByDeploymentMotivationAndElement(String user, String deploymentName,
                                                                            String motivation, String elementName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, deploymentAnnotationsQuery(
                interpolate(nameFrom(deploymentName, "id")),
                motivation,
                interpolate(nameFrom(elementName, "id"))
        ));
    }

    @SuppressWarnings("unchecked")
    @Then("the following deployment annotations are returned")
    public void theFollowingDeploymentAnnotationsAreReturned(List<String> annotationNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                List<Map> annotations = (List<Map>) data.get("AnnotationsByDeployment");

                List<String> expectedAnnotationIds = annotationNames.stream()
                        .map(annotationName -> context.getVariable(interpolate(nameFrom(annotationName, "id"))))
                        .collect(Collectors.toList());

                annotations.forEach(annotationMap -> assertTrue(expectedAnnotationIds.stream()
                                                                        .anyMatch(one -> one.equals(annotationMap.get("id")))));
            }
        }));
    }


    @Then("the deployment annotation is not returned due to unsupported motivation")
    public void theDeploymentAnnotationIsNotReturnedDueToUnsupportedMotivation() {
        String path = "$.response.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path + "data.AnnotationsByDeployment", null)
                .jsonPath(path + "errors[0].extensions.code", 400));    }

    @When("{string} fetches deployment account annotation by deployment {string} and motivation {string}")
    public void fetchesDeploymentAccountAnnotationByDeploymentAndMotivation(String user, String deploymentName, String motivation) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, deploymentAccountAnnotationsQuery(
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(getAccountIdVar(user)),
                motivation,
                null
        ));
    }

    @When("{string} fetches deployment annotation by deployment {string} and motivation {string}")
    public void fetchesDeploymentAnnotationByDeploymentAndMotivation(String user, String deploymentName, String motivation) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, deploymentAnnotationsQuery(
                interpolate(nameFrom(deploymentName, "id")),
                motivation,
                null
        ));
    }

    @When("{string} creates a deployment {string} annotation for element {string} of type {string} and deployment {string} with")
    public void createsADeploymentAnnotationForElementOfTypeAndDeploymentWith(String user, String motivation,
                                                                              String elementName, String elementType,
                                                                              String deploymentName, Map<String, String> args){
        authenticationSteps.authenticatesViaIes(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.create")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target")).build();

        messageOperations.sendJSON(runner, message);
    }

    @When("instructor {string} creates a deployment {string} annotation for element {string} of type {string} and deployment {string} with")
    public void instructorCreatesADeploymentAnnotationForElementOfTypeAndDeploymentWith(String user, String motivation,
                                                                                        String elementName, String elementType,
                                                                                        String deploymentName, Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.create")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target")).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the deployment annotation {string} is created succesfully through RTM")
    public void theDeploymentAnnotationIsCreatedSuccesfullyThroughRTM(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"learner.annotation.create.ok\"," +
                        "\"response\":{" +
                        "\"learnerAnnotation\":{" +
                        "\"id\":\"@notEmpty()@\"," +
                        "\"version\":\"@notEmpty()@\"," +
                        "\"motivation\":\"@notEmpty()@\"," +
                        "\"creatorAccountId\":\"@notEmpty()@\"," +
                        "\"bodyJson\":{\"json\":\"body\"}," +
                        "\"targetJson\":{\"json\":\"target\"}," +
                        "\"deploymentId\":\"@notEmpty()@\"," +
                        "\"elementId\":\"@notEmpty()@\"," +
                        "\"modified\":\"@notEmpty()@\"," +
                        "\"body\":\"@notEmpty()@\"," +
                        "\"created\":\"@notEmpty()@\"," +
                        "\"type\":\"@notEmpty()@\"," +
                        "\"target\":\"@notEmpty()@\"" +
                        "}" +
                        "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.learnerAnnotation.id", nameFrom(annotationName, "id")));
    }

    @Then("the deployment annotation is not created through RTM due to missing permission level")
    public void theDeploymentAnnotationIsNotCreatedThroughRTMDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"learner.annotation.create.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} updates a deployment annotation {string} with motivation {string} for element {string} of type {string} and deployment {string} with")
    public void updatesADeploymentAnnotationWithMotivationForElementOfTypeAndDeploymentWith(String user,
                                                                                            String annotationName,
                                                                                            String motivation,
                                                                                            String elementName,
                                                                                            String elementType,
                                                                                            String deploymentName,
                                                                                            Map<String, String> args) {
        authenticationSteps.authenticatesViaIes(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.update")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("annotationId", interpolate(nameFrom(annotationName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target")).build();

        messageOperations.sendJSON(runner, message);
    }

    @When("instructor {string} updates a deployment annotation {string} with motivation {string} for element {string} of type {string} and deployment {string} with")
    public void instructorUpdatesADeploymentAnnotationWithMotivationForElementOfTypeAndDeploymentWith(String user,
                                                                                                      String annotationName,
                                                                                                      String motivation,
                                                                                                      String elementName,
                                                                                                      String elementType,
                                                                                                      String deploymentName,
                                                                                                      Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.update")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("annotationId", interpolate(nameFrom(annotationName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target")).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the deployment annotation {string} is updated successfully")
    public void theDeploymentAnnotationIsUpdatedSuccessfully(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"learner.annotation.update.ok\"," +
                               "\"response\":{" +
                               "\"learnerAnnotation\":{" +
                               "\"id\":\"@notEmpty()@\"," +
                               "\"version\":\"@notEmpty()@\"," +
                               "\"motivation\":\"@notEmpty()@\"," +
                               "\"creatorAccountId\":\"@notEmpty()@\"," +
                               "\"bodyJson\":{\"json\":\"body1\"}," +
                               "\"targetJson\":{\"json\":\"target1\"}," +
                               "\"deploymentId\":\"@notEmpty()@\"," +
                               "\"elementId\":\"@notEmpty()@\"," +
                               "\"modified\":\"@notEmpty()@\"," +
                               "\"body\":\"@notEmpty()@\"," +
                               "\"created\":\"@notEmpty()@\"," +
                               "\"type\":\"@notEmpty()@\"," +
                               "\"target\":\"@notEmpty()@\"" +
                               "}" +
                               "},\"replyTo\":\"@notEmpty()@\"}")
                .extractFromPayload("$.response.learnerAnnotation.id", nameFrom(annotationName, "id")));
    }

    @Then("the deployment annotation update fails due to missing permission level")
    public void theDeploymentAnnotationUpdateFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"learner.annotation.update.error\"," +
                               "\"code\":401," +
                               "\"message\":\"@notEmpty()@\"," +
                               "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} can list following fields from deployment annotation {string} with motivation {string} for element {string} of type {string} and deployment {string} created by {string}")
    public void canListFollowingFieldsFromDeploymentAnnotationWithMotivationForElementOfTypeAndRootElement(String user,
                                                                                                           String annotationName,
                                                                                                           String motivation,
                                                                                                           String elementName,
                                                                                                           String elementType,
                                                                                                           String deploymentName,
                                                                                                           String creatorName,
                                                                                                           Map<String, String> expectedAnnotation) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.list")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("creatorAccountId", interpolate(nameFrom(creatorName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAnnotation, final Map<String, Object> headers, final TestContext context) {
                        assertNotNull(actualAnnotation.get(0));
                        Map bodyJson = (LinkedHashMap)((LinkedHashMap)actualAnnotation.get(0)).get("bodyJson");
                        Map targetJson = (LinkedHashMap)((LinkedHashMap)actualAnnotation.get(0)).get("targetJson");
                        assertNotNull(bodyJson);
                        assertNotNull(targetJson);
                        assertEquals(((LinkedHashMap)actualAnnotation.get(0)).get("motivation"), expectedAnnotation.get("motivation"));

                        assertEquals(bodyJson.get("json"), expectedAnnotation.get("body"));
                        assertEquals(targetJson.get("json"), expectedAnnotation.get("target"));

                    }

                    @Override
                    public String getRootElementName() {
                        return "learnerAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "learner.annotation.list.ok";
                    }
                }));
    }

    @When("{string} tries to fetch fields from deployment annotation {string} with motivation {string} for element {string} of type {string} and deployment {string} created by {string}")
    public void triesToFetchFieldsFromDeploymentAnnotationWithMotivationForElementOfTypeAndRootElementUNIT_ONE(String user,
                                                                                                               String annotationName,
                                                                                                               String motivation,
                                                                                                               String elementName,
                                                                                                               String elementType,
                                                                                                               String deploymentName,
                                                                                                               String creatorName) {
        authenticationSteps.authenticatesViaIes(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.list")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("creatorAccountId", interpolate(nameFrom(creatorName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the deployment annotation fetch fails due to missing permission level")
    public void theDeploymentAnnotationFetchFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"learner.annotation.list.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} can list following fields from deployment annotation {string} with motivation {string} and deployment {string} created by {string}")
    public void canListFollowingFieldsFromDeploymentAnnotationWithMotivationAndRootElement(String user,
                                                                                           String annotationName,
                                                                                           String motivation,
                                                                                           String deploymentName,
                                                                                           String creatorName,
                                                                                           Map<String, String> expectedAnnotation) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .addField("creatorAccountId", interpolate(nameFrom(creatorName, "id")))
                .addField("motivation", motivation).build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAnnotation, final Map<String, Object> headers, final TestContext context) {
                        assertNotNull(actualAnnotation.get(0));
                        Map bodyJson = (LinkedHashMap)((LinkedHashMap)actualAnnotation.get(0)).get("bodyJson");
                        Map targetJson = (LinkedHashMap)((LinkedHashMap)actualAnnotation.get(0)).get("targetJson");
                        assertNotNull(bodyJson);
                        assertNotNull(targetJson);
                        assertEquals(((LinkedHashMap)actualAnnotation.get(0)).get("motivation"), expectedAnnotation.get("motivation"));

                        assertEquals(bodyJson.get("json"), expectedAnnotation.get("body"));
                        assertEquals(targetJson.get("json"), expectedAnnotation.get("target"));

                    }

                    @Override
                    public String getRootElementName() {
                        return "learnerAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "learner.annotation.list.ok";
                    }
                }));
    }

    @When("{string} deletes deployment annotation {string}")
    public void deletesDeploymentAnnotation(String user, String annotationName) {
        authenticationSteps.authenticatesViaIes(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.delete")
                .addField("annotationId", interpolate(nameFrom(annotationName, "id"))).build();

        messageOperations.sendJSON(runner, message);
    }

    @When("instructor {string} deletes deployment annotation {string}")
    public void instructorDeletesDeploymentAnnotation(String user, String annotationName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "learner.annotation.delete")
                .addField("annotationId", interpolate(nameFrom(annotationName, "id"))).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the learner annotation {string} is deleted successfully")
    public void theLearnerAnnotationIsDeletedSuccessfully(String annotationName) {
                messageOperations.receiveJSON(runner, action -> action
                        .jsonPath("$.type", "learner.annotation.delete.ok")
                );
    }

    @Then("the learner annotation delete fails for {string} due to missing permission level")
    public void theLearnerAnnotationDeleteFailsForDueToMissingPermissionLevel(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"learner.annotation.delete.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
