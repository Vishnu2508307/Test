package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.annotation.AnnotationHelper.coursewareAnnotationCreateMutation;
import static mercury.helpers.annotation.AnnotationHelper.coursewareAnnotationDeleteMutation;
import static mercury.helpers.annotation.AnnotationHelper.coursewareAnnotationsQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.annotation.service.CoursewareAnnotationKey;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.send.ErrorMessage;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class CoursewareAnnotationsSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates a courseware {string} annotation for element {string} and rootElement {string} with")
    public void createsACoursewareAnnotationForElementAndRootElementWith(String user, String motivation,
                                                                         String elementName, String rootElementName,
                                                                         Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, coursewareAnnotationCreateMutation(
                interpolate(nameFrom(rootElementName, "id")),
                motivation,
                interpolate(nameFrom(elementName, "id")),
                args.get("body"),
                args.get("target")
        ));
    }

    @Then("the courseware annotation {string} is created succesfully")
    public void theCoursewareAnnotationIsCreatedSuccesfully(String annotationName) {
        String path = "$.response.data.AnnotationForCoursewareCreate.id";
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload(path, nameFrom(annotationName, "id")));
    }

    @Then("the courseware annotation is not created due to missing permission level")
    public void theCoursewareAnnotationIsNotCreatedDueToMissingPermissionLevel() {
        String path = "$.response.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path + "data.AnnotationForCoursewareCreate", null)
                .jsonPath(path + "errors[0].extensions.code", 403));
    }

    @Given("{string} has created a courseware {string} annotation {string} for element {string} and rootElement {string} with")
    public void hasCreatedACoursewareAnnotationForElementAndRootElementWith(String user, String motivation, String annotationName,
                                                                            String elementName, String rootElementName,
                                                                            Map<String, String> args) {
        createsACoursewareAnnotationForElementAndRootElementWith(user, motivation, elementName, rootElementName, args);
        theCoursewareAnnotationIsCreatedSuccesfully(annotationName);
    }

    @When("{string} fetches courseware annotation by rootElement {string} and motivation {string}")
    public void fetchesCoursewareAnnotationByRootElementAndMotivation(String user, String rootElementName, String motivation) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, coursewareAnnotationsQuery(
                interpolate(nameFrom(rootElementName, "id")),
                motivation,
                null
        ));
    }

    @SuppressWarnings("unchecked")
    @Then("the following courseware annotations are returned")
    public void theFollowingCoursewareAnnotationsAreReturned(List<String> annotationNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                List<Map> annotations = (List<Map>) data.get("AnnotationsByCourseware");
                annotations.forEach(annotationMap -> assertNotNull(annotationMap.get("id")));
            }
        }));
    }

    @SuppressWarnings("unchecked")
    @Then("no courseware annotations are returned")
    public void noCoursewareAnnotationsAreReturned() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                List<Map> annotations = (List<Map>) data.get("AnnotationsByCourseware");

                assertTrue(annotations.isEmpty());
            }
        }));
    }

    @When("{string} fetches courseware annotation by rootElement {string}, motivation {string} and element {string}")
    public void fetchesCoursewareAnnotationByRootElementMotivationAndElement(String user, String rootElementName,
                                                                             String motivation, String elementName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, coursewareAnnotationsQuery(
                interpolate(nameFrom(rootElementName, "id")),
                motivation,
                interpolate(nameFrom(elementName, "id"))
        ));
    }

    @Then("the courseware annotations are not returned due to missing permission level")
    public void theCoursewareAnnotationsAreNotReturnedDueToMissingPermissionLevel() {
        String path = "$.response.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path + "errors[0].extensions.code", 403));
    }

    @Then("the courseware annotation {string} is deleted succesfully")
    public void theCoursewareAnnotationIsDeletedSuccesfully(String annotationName) {
        String path = "$.response.data.AnnotationForCoursewareDelete.id";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path, interpolate(nameFrom(annotationName, "id"))));
    }

    @When("{string} deletes annotation {string} of courseware")
    public void deletesAnnotationOfCoursewareAnnotationWith(String user, String annotationName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendGraphQL(runner, coursewareAnnotationDeleteMutation(
                interpolate(nameFrom(annotationName, "id"))
        ));

    }

    @Then("the courseware annotation {string} is not deleted due to invalid or missing permission level")
    public void theCoursewareAnnotationIsNotDeletedDueToInvalidOrMissingPermissionLevel(String user) {
        String path = "$.response.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path + "data.AnnotationForCoursewareDelete", null)
                .jsonPath(path + "errors[0].extensions.code", 403));
    }

    @When("{string} creates a courseware {string} annotation through rtm for element {string} of type {string} and rootElement {string} with")
    public void createsACoursewareAnnotationForElementAndRootElementWith(String user, String motivation,
                                                                         String elementName, String elementType, String rootElementName,
                                                                         Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.create")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target")).build();

        messageOperations.sendJSON(runner, message);
    }

    @When("{string} creates a courseware {string} annotation with a supplied id through rtm for element {string} of type {string} and rootElement {string} with")
    public void createsACoursewareAnnotationWithASuppliedIdForElementAndRootElementWith(String user, String motivation,
                                                                         String elementName, String elementType, String rootElementName,
                                                                         Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        UUID annotationId = runner.variable("SUPPLIED_ANNOTATION_ID", com.smartsparrow.util.UUIDs.timeBased());
        String message = new PayloadBuilder()
                .addField("type", "author.annotation.create")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target"))
                .addField("annotationId", annotationId)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @When("{string} creates a courseware {string} annotation with the same supplied id as before through rtm for element {string} of type {string} and rootElement {string} with")
    public void createsACoursewareAnnotationWithSameSuppliedIdForElementAndRootElementWith(String user, String motivation,
                                                                                        String elementName, String elementType, String rootElementName,
                                                                                        Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        UUID annotationId = com.smartsparrow.util.UUIDs.fromString(getVariableValue("SUPPLIED_ANNOTATION_ID"));
        String message = new PayloadBuilder()
                .addField("type", "author.annotation.create")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target"))
                .addField("annotationId", annotationId)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    @Then("the courseware annotation is not created due to conflict")
    public void theAnnotationIsNotCreatedDueToConflict() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                Assertions.assertEquals("author.annotation.create.error", payload.getType());
                Assertions.assertEquals(409, payload.getCode().intValue());
                Assertions.assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @Then("the courseware annotation {string} is created successfully")
    public void theCoursewareAnnotationIsCreatedSuccessfully(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.annotation.create.ok\"," +
                    "\"response\":{" +
                        "\"coursewareAnnotation\":{" +
                            "\"id\":\"@notEmpty()@\"," +
                            "\"version\":\"@notEmpty()@\"," +
                            "\"motivation\":\"@notEmpty()@\"," +
                            "\"creatorAccountId\":\"@notEmpty()@\"," +
                            "\"bodyJson\":{\"json\":\"body\"}," +
                            "\"targetJson\":{\"json\":\"target\"}," +
                            "\"rootElementId\":\"@notEmpty()@\"," +
                            "\"elementId\":\"@notEmpty()@\"," +
                            "\"modified\":\"@notEmpty()@\"," +
                            "\"body\":\"@notEmpty()@\"," +
                            "\"created\":\"@notEmpty()@\"," +
                            "\"type\":\"@notEmpty()@\"," +
                            "\"target\":\"@notEmpty()@\"" +
                        "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}")
                .extractFromPayload("$.response.coursewareAnnotation.id", nameFrom(annotationName, "id"))
                .extractFromPayload("$.response.coursewareAnnotation.motivation", nameFrom(annotationName, "motivation")));
    }

    @Then("the courseware annotation {string} is created successfully with the supplied id")
    public void theCoursewareAnnotationIsCreatedSuccessfullyWithId(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.annotation.create.ok\"," +
                                       "\"response\":{" +
                                       "\"coursewareAnnotation\":{" +
                                       "\"id\":\"@notEmpty()@\"," +
                                       "\"version\":\"@notEmpty()@\"," +
                                       "\"motivation\":\"@notEmpty()@\"," +
                                       "\"creatorAccountId\":\"@notEmpty()@\"," +
                                       "\"bodyJson\":{\"json\":\"body\"}," +
                                       "\"targetJson\":{\"json\":\"target\"}," +
                                       "\"rootElementId\":\"@notEmpty()@\"," +
                                       "\"elementId\":\"@notEmpty()@\"," +
                                       "\"modified\":\"@notEmpty()@\"," +
                                       "\"body\":\"@notEmpty()@\"," +
                                       "\"created\":\"@notEmpty()@\"," +
                                       "\"type\":\"@notEmpty()@\"," +
                                       "\"target\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.coursewareAnnotation.id", nameFrom(annotationName, "id"))
                        .extractFromPayload("$.response.coursewareAnnotation.motivation", nameFrom(annotationName, "motivation"))
                        .validate("$.response.coursewareAnnotation.id", "${SUPPLIED_ANNOTATION_ID}"));
    }

    @Then("the courseware annotation create fails due to missing permission level")
    public void theCoursewareAnnotationCreatedFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.annotation.create.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));

    }

    @When("{string} updates a courseware annotation {string} with motivation {string} for element {string} of type {string} and rootElement {string} with")
    public void updatesACoursewareAnnotationWithMotivationForElementOfTypeAndRootElementWith(String user,
                                                                                             String annotationName,
                                                                                             String motivation,
                                                                                             String elementName,
                                                                                             String elementType,
                                                                                             String rootElementName,
                                                                                             Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.update")
                .addField("annotationId", interpolate(nameFrom(annotationName, "id")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target")).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware annotation {string} is updated successfully")
    public void theCoursewareAnnotationIsUpdatedSuccessfully(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.annotation.update.ok\"," +
                        "\"response\":{" +
                        "\"coursewareAnnotation\":{" +
                        "\"id\":\"@notEmpty()@\"," +
                        "\"version\":\"@notEmpty()@\"," +
                        "\"motivation\":\"@notEmpty()@\"," +
                        "\"creatorAccountId\":\"@notEmpty()@\"," +
                        "\"bodyJson\":{\"json\":\"body1\"}," +
                        "\"targetJson\":{\"json\":\"target1\"}," +
                        "\"rootElementId\":\"@notEmpty()@\"," +
                        "\"elementId\":\"@notEmpty()@\"," +
                        "\"modified\":\"@notEmpty()@\"," +
                        "\"body\":\"@notEmpty()@\"," +
                        "\"created\":\"@notEmpty()@\"," +
                        "\"type\":\"@notEmpty()@\"," +
                        "\"target\":\"@notEmpty()@\"" +
                        "}" +
                        "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.coursewareAnnotation.id", nameFrom(annotationName, "id")));
    }

    @Then("the courseware annotation update fails due to missing permission level")
    public void theCoursewareAnnotationUpdateFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.annotation.update.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }


    @When("{string} deletes annotation {string} of courseware element {string} of type {string}")
    public void deletesACoursewareAnnotation(String user, String annotationName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.delete")
                .addField("annotationId", interpolate(nameFrom(annotationName, "id")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }


    @Then("the courseware annotation {string} is deleted successfully")
    public void theCoursewareAnnotationIsDeletedSuccessfully(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"author.annotation.delete.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware annotation delete fails for {string} due to missing permission level")
    public void theCoursewareAnnotationDeletedFailsDueToMissingPermissionLevel(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                       "\"type\":\"author.annotation.delete.error\"," +
                       "\"code\":401," +
                       "\"message\":\"@notEmpty()@\"," +
                       "\"replyTo\":\"@notEmpty()@\"}"));

    }

    @Then("{string} can list following fields from courseware annotation {string} with motivation {string} for element {string} of type {string} and rootElement {string}")
    public void canListFollowingFieldsFromCoursewareAnnotationWithMotivationForElementOfTypeAndRootElement(String user,
                                                                                                           String annotationName,
                                                                                                           String motivation,
                                                                                                           String elementName,
                                                                                                           String elementType,
                                                                                                           String rootElementName,
                                                                                                           Map<String, String> expectedAnnotation) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
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
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.list.ok";
                    }
                }));
    }


    @And("{string} creates a courseware {string} annotation {string} for element {string} and rootElement {string}")
    public void createsACoursewareAnnotationForElementAndRootElementWithTarget(String user, String motivation, String annotationName,
                                                                               String elementName, String rootElementName) {
        authenticationSteps.authenticateUser(user);
        String target = "[{\\\"id\\\": \\\""+ interpolate(nameFrom(elementName, "id")) +"\\\",\\\"type\\\": \\\"INTERACTIVE\\\"}]";
        String body = "{\\\"json\\\":\\\"body\\\"}";
        messageOperations.sendGraphQL(runner, coursewareAnnotationCreateMutation(
                interpolate(nameFrom(rootElementName, "id")),
                motivation,
                interpolate(nameFrom(elementName, "id")),
                body,
                target
        ));
        theCoursewareAnnotationIsCreatedSuccesfully(annotationName);
    }

    @Then("{string} can list courseware annotation with motivation {string} for element {string} of type {string} and rootElement {string}")
    public void canListFollowingFieldsFromCoursewareAnnotationWithMotivationForElementOfTypeAndRootElementWithTarget(String user,
                                                                                                           String motivation,
                                                                                                           String elementName,
                                                                                                           String elementType,
                                                                                                           String rootElementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAnnotation, final Map<String, Object> headers, final TestContext context) {
                        assertNotNull(actualAnnotation.get(0));
                        assertTrue(((LinkedHashMap<?, ?>) actualAnnotation.get(0)).get("target").toString().contains(((LinkedHashMap<?, ?>) actualAnnotation.get(0)).get("target").toString()));
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.list.ok";
                    }
                }));
    }

    @When("{string} tries to fetch fields from courseware annotation {string} with motivation {string} for element {string} of type {string} and rootElement {string}")
    public void triesToFetchFieldsFromCoursewareAnnotationWithMotivationForElementOfTypeAndRootElementUNIT_ONE(String user,
                                                                                                               String annotationName,
                                                                                                               String motivation,
                                                                                                               String elementName,
                                                                                                               String elementType,
                                                                                                               String rootElementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware annotation fetch fails due to missing permission level")
    public void theCoursewareAnnotationFetchFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.annotation.list.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} can list following fields from courseware annotation {string} with motivation {string} and rootElement {string}")
    public void canListFollowingFieldsFromCoursewareAnnotationWithMotivationAndRootElement(String user,
                                                                                                           String annotationName,
                                                                                                           String motivation,
                                                                                                           String rootElementName,
                                                                                                           Map<String, String> expectedAnnotation) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
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
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.list.ok";
                    }
                }));
    }

    @Then("{string} can get following fields from courseware annotation {string} for element {string} and type {string}")
    public void canGetFollowingFieldsFromCoursewareAnnotationForElementAndType(String user, String annotationName, String elementName, String elementType, Map<String, String> expectedAnnotation) {
        authenticationSteps.authenticateUser(user);
        String message = new PayloadBuilder()
                .addField("type", "author.annotation.get")
                .addField("annotationId", interpolate(nameFrom(annotationName, "id")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LinkedHashMap>(LinkedHashMap.class) {
                    @Override
                    public void validate(final LinkedHashMap actualAnnotation, final Map<String, Object> headers, final TestContext context) {
                        assertNotNull(actualAnnotation);
                        Map bodyJson = (Map)actualAnnotation.get("bodyJson");
                        Map targetJson = (LinkedHashMap)actualAnnotation.get("targetJson");
                        assertNotNull(bodyJson);
                        assertNotNull(targetJson);
                        assertEquals(actualAnnotation.get("motivation"), expectedAnnotation.get("motivation"));

                        assertEquals(bodyJson.get("json"), expectedAnnotation.get("body"));
                        assertEquals(targetJson.get("json"), expectedAnnotation.get("target"));

                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.get.ok";
                    }
                }));
    }

    @When("{string} tries to get fields from courseware annotation {string} for element {string} of type {string}")
    public void triesToGetFieldsFromCoursewareAnnotationForElementOfType(String user, String annotationName, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.get")
                .addField("annotationId", interpolate(nameFrom(annotationName, "id")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware annotation get fails for annotaion {string} due to missing permission level")
    public void theCoursewareAnnotationGetFailsForAnnotaionDueToMissingPermissionLevel(String annotationName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.annotation.get.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} has created a courseware {string} annotation {string} through rtm for element {string} of type {string} and rootElement {string} with")
    public void hasCreatedACoursewareAnnotationThroughRtmForElementOfTypeAndRootElementWith(String user,
                                                                                            String motivation,
                                                                                            String annotationName,
                                                                                            String elementName,
                                                                                            String elementType,
                                                                                            String rootElementName,
                                                                                            Map<String, String> args) {
        createsACoursewareAnnotationForElementAndRootElementWith(user, motivation, elementName, elementType, rootElementName, args);
        theCoursewareAnnotationIsCreatedSuccessfully(annotationName);

    }

    @When("{string} has updated a courseware annotation {string} for element {string} of type {string} with")
    public void hasUpdatedACoursewareAnnotationForElementAndRootElementWith(String user,
                                                                            String annotationName,
                                                                            String elementName,
                                                                            String elementType,
                                                                            Map<String, String> args) {
        updatesACoursewareAnnotationWithMotivationForElementOfTypeAndRootElementWith(user, annotationName, null, elementName, elementType, null, args);
        theCoursewareAnnotationIsUpdatedSuccessfully(annotationName);
    }

    @When("{string} has deleted annotation {string} for courseware element {string} of type {string}")
    public void hasDeletedAnnotationForCoursewareElementOfType(String user, String annotationName, String elementName, String elementType) {
        deletesACoursewareAnnotation(user, annotationName, elementName, elementType);
        theCoursewareAnnotationIsDeletedSuccessfully(annotationName);
    }

    @Given("{string} has deleted courseware annotation {string}")
    public void hasDeletedCoursewareAnnotation(String accountName, String annotationName) {
        deletesAnnotationOfCoursewareAnnotationWith(accountName, annotationName);
        theCoursewareAnnotationIsDeletedSuccesfully(annotationName);
    }

    @And("{string} has created a courseware annotation {string} for element {string} of type {string} with")
    public void hasCreatedACoursewareAnnotationForElementOfTypeWith(String accountName, String annotationName,
                                                                    String elementName, String elementType, Map<String, String> args) {
        authenticationSteps.authenticateUser(accountName);

        final String message = new PayloadBuilder()
                .addField("type", "author.annotation.create")
                .addField("rootElementId", interpolate(nameFrom(args.get("rootElementId"), "id")))
                .addField("motivation", args.get("motivation"))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("body", args.get("body"))
                .addField("target", args.get("target"))
                .build();

        messageOperations.sendJSON(runner, message);

        theCoursewareAnnotationIsCreatedSuccessfully(annotationName);
    }

    @Then("{string} can not list annotation with motivation {string} for element {string} and rootElement {string}")
    public void annotationsAreDeletedOnceInteractiveIsDeleted(String user,
                                                              String motivation,
                                                              String elementName,
                                                              String rootElementName
                                                              ) {
       //remove
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", CoursewareElementType.ACTIVITY).build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAnnotation, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(0, actualAnnotation.size());
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.list.ok";
                    }
                }));
    }

    @Then("{string} can not list {string} annotation for rootElement {string}")
    public void annotationsAreDeletedOnceInteractiveIsDeleted(String user,
                                                              String motivation,
                                                              String rootElementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.list")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", motivation).build();

        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAnnotation, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(0, actualAnnotation.size());
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.list.ok";
                    }
                }));
    }

    @When("{string} resolve courseware annotations for rootElement {string} with value {string}")
    public void resolveACoursewareAnnotationWithRootElementWith(String user,
                                                                String rootElementName,
                                                                String resolved,
                                                                List<String> annotationNames) {
        authenticationSteps.authenticateUser(user);
        List<CoursewareAnnotationKey> coursewareAnnotationKeys = annotationNames.stream()
                .map(annotationName -> new CoursewareAnnotationKey()
                        .setId(UUID.fromString(getVariableValue(nameFrom(annotationName, "id"))))
                        .setVersion(UUID.fromString(getVariableValue(nameFrom(annotationName, "id")))))
                .collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.resolve")
                .addField("coursewareAnnotationKeys", coursewareAnnotationKeys)
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("resolved", Boolean.valueOf(resolved)).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware annotations are resolved successfully")
    public void theCoursewareAnnotationsAreResolvedSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.annotation.resolve.ok"));
    }


    @When("{string} reads courseware annotations for rootElement {string} and element {string} of type {string} with value {string}")
    public void readACoursewareAnnotationWithRootElementWith(String user,
                                                                String rootElementName,
                                                                String elementName,
                                                                String elementType,
                                                                String read,
                                                                List<String> annotationNames) {
        authenticationSteps.authenticateUser(user);
        List<UUID> coursewareAnnotationIds = annotationNames.stream()
                .map(annotationName ->
                        UUID.fromString(getVariableValue(nameFrom(annotationName, "id"))))
                .collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.read")
                .addField("annotationIds", coursewareAnnotationIds)
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("read", Boolean.valueOf(read)).build();

        messageOperations.sendJSON(runner, message);
    }

    @When("{string} aggregate courseware annotations for rootElement {string}")
    public void aggregateACoursewareAnnotationWithRootElement(String user,
                                                              String rootElementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.annotation.aggregate")
                .addField("rootElementId", interpolate(nameFrom(rootElementName, "id")))
                .addField("motivation", Motivation.commenting.toString()).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware annotations are read successfully")
    public void theCoursewareAnnotationsAreReadSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.annotation.read.ok")
        );
    }

    @Then("the courseware annotation {string} is fetched successfully with read value {string}")
    public void theCoursewareAnnotationIsDeletedSuccessfully(String annotationName, String read) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LinkedHashMap>(LinkedHashMap.class) {
                    @Override
                    public void validate(final LinkedHashMap actualAnnotation,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualAnnotation);

                        assertEquals(actualAnnotation.get("read").toString(), read);
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareAnnotation";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.get.ok";
                    }
                }));
    }

    @Then("the courseware annotations are aggregated successfully with following values")
    public void theCoursewareAnnotationsAreAggregatedSuccessfully(Map<String, String> expectedAnnotation) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<LinkedHashMap>(LinkedHashMap.class) {
                    @Override
                    public void validate(final LinkedHashMap actualAnnotationAggregation,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualAnnotationAggregation);

                        assertEquals(actualAnnotationAggregation.get("total"),
                                     Integer.parseInt(expectedAnnotation.get("total")));
                        assertEquals(actualAnnotationAggregation.get("read"),
                                     Integer.parseInt(expectedAnnotation.get("read")));
                        assertEquals(actualAnnotationAggregation.get("unRead"),
                                     Integer.parseInt(expectedAnnotation.get("unRead")));
                        assertEquals(actualAnnotationAggregation.get("resolved"),
                                     Integer.parseInt(expectedAnnotation.get("resolved")));
                        assertEquals(actualAnnotationAggregation.get("unResolved"),
                                     Integer.parseInt(expectedAnnotation.get("unResolved")));
                    }

                    @Override
                    public String getRootElementName() {
                        return "coursewareAnnotationAggregate";
                    }

                    @Override
                    public String getType() {
                        return "author.annotation.aggregate.ok";
                    }
                }));
    }
}
