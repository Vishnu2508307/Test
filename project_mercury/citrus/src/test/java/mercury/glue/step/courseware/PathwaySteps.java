package mercury.glue.step.courseware;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.courseware.ActivitySteps.ACTIVITY_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.PathwayHelper.deletePathway;
import static mercury.helpers.courseware.PathwayHelper.deletePathwayError;
import static mercury.helpers.courseware.PathwayHelper.deletePathwayOk;
import static mercury.helpers.courseware.PathwayHelper.fetchWalkablesForPathwayGraphQlQuery;
import static mercury.helpers.courseware.PathwayHelper.fetchWalkablesGraphQlQuery;
import static mercury.helpers.courseware.PathwayHelper.getPathway;
import static mercury.helpers.courseware.PathwayHelper.replaceConfig;
import static mercury.helpers.courseware.PathwayHelper.validatePathwayPayload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.rtm.message.send.ErrorMessage;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.courseware.PathwayHelper;

public class PathwaySteps {

    public static final String PATHWAY_ID_VAR = "pathway_id";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    private static final String DEFAULT_PATHWAY_TYPE = "LINEAR";

    @When("^\"([^\"]*)\" creates a ([^\"]*) pathway for the activity$")
    public void createsAPathwayForTheActivity(String accountName, String pathwayType) {
        createsAPathwayForTheActivity(accountName, pathwayType, null);
    }

    @When("^\"([^\"]*)\" creates a ([^\"]*) pathway for random activity$")
    public void createsAPathwayForRandomActivity(String accountName, String pathwayType) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, PathwayHelper.createPathway(UUIDs.timeBased().toString(), pathwayType));
    }

    @Then("^the ([^\"]*) pathway is successfully created$")
    public void thePathwayIsSuccessfullyCreated(String pathwayType) {
        String expectedPathway = PathwayHelper.createPathwayOk("@notEmpty()@", "${" + ACTIVITY_ID_VAR + "}",
                pathwayType);
        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway));
    }

    @Then("^the ([^\"]*) pathway is successfully created with the supplied id$")
    public void thePathwayIsSuccessfullyCreatedWithId(String pathwayType) {
        String expectedPathway = PathwayHelper.createPathwayOk("@notEmpty()@", "${" + ACTIVITY_ID_VAR + "}",
                                                               pathwayType);
        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway)
                .validate("$.response.pathway.pathwayId", "${SUPPLIED_PATHWAY_ID}"));
    }

    private void createsAPathwayForTheActivity(String accountName, String pathwayType, String activityName) {
        authenticationSteps.authenticateUser(accountName);

        String activityVar = (activityName == null) ? ACTIVITY_ID_VAR : nameFrom(activityName, "id");
        messageOperations.sendJSON(runner, PathwayHelper.createPathway(interpolate(activityVar), pathwayType));
    }

    @Then("^the pathway creation fails due to code (\\d+) and message \"([^\"]*)\"$")
    public void thePathwayCreationFailsDueToCodeAndMessage(int code, String message) {
        messageOperations.receiveJSON(runner, action -> action.payload(PathwayHelper.createPathwayError(code, message)));
    }

    @And("^\"([^\"]*)\" has created a pathway$")
    public void hasCreatedAPathway(String accountName) throws Throwable {
        authenticationSteps.authenticateUser(accountName);
        createPathway(ACTIVITY_ID_VAR, PATHWAY_ID_VAR);
    }

    @And("^\"([^\"]*)\" has created a pathway for the \"([^\"]*)\" activity$")
    public void hasCreatedPathwayForTheActivity(String user, String activityName) {
        authenticationSteps.authenticateUser(user);
        createPathway(Variables.nameFrom(activityName, "id"), PATHWAY_ID_VAR);
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" pathway for the \"([^\"]*)\" activity$")
    public void hasCreatedAPathwayForTheActivity(String user, String pathwayName, String activityName) {
        authenticationSteps.authenticateUser(user);
        createPathway(Variables.nameFrom(activityName, "id"), Variables.nameFrom(pathwayName, "id"));
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" pathway named \"([^\"]*)\" for the \"([^\"]*)\" activity$")
    public void hasCreatedATypedPathwayForTheActivity(String user, String pathwayType, String pathwayName, String activityName) {
        authenticationSteps.authenticateUser(user);
        createPathway(Variables.nameFrom(activityName, "id"), //
                      pathwayType, //
                      Variables.nameFrom(pathwayName, "id"));
    }

    @And("^the \"([^\"]*)\" pathway does not have the \"([^\"]*)\" activity as children$")
    public void thePathwayDoesNotHaveTheActivityAsChildren(String pathwayName, String activityName) {
        messageOperations.sendJSON(runner, getPathway(interpolate(Variables.nameFrom(pathwayName, "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload((payload, context) -> {

            String parentActivityId = context.getVariable(Variables.nameFrom(activityName, "id"));
            if (payload.getChildren() != null) {
                assertEquals(0, payload.getChildren()
                        .stream()
                        .filter(one -> String.valueOf(one.getElementId()).equals(parentActivityId))
                        .collect(Collectors.toList()).size());
            } else {
                assertNull(payload.getChildren());
            }
        })));
    }

    @Then("^the \"([^\"]*)\" pathway should have \"([^\"]*)\" activity as child$")
    public void thePathwayShouldHaveActivityAsChild(String pathwayName, String activityName) {
        messageOperations.sendJSON(runner, getPathway(interpolate(Variables.nameFrom(pathwayName, "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload((payload, context) -> {

            String childActivityId = context.getVariable(Variables.nameFrom(activityName, "id"));
            assertNotNull(payload.getChildren());
            assertEquals(1, payload.getChildren()
                    .stream()
                    .filter(one -> String.valueOf(one.getElementId()).equals(childActivityId))
                    .collect(Collectors.toList()).size());
            assertEquals(childActivityId, payload.getChildren().get(0).getElementId().toString(), childActivityId);
            assertEquals(childActivityId, payload.getChildren().get(0).getElementType(), CoursewareElementType.ACTIVITY);
        })));
    }

    @When("^\"([^\"]*)\" deletes the \"([^\"]*)\" pathway for the \"([^\"]*)\" activity$")
    public void deletesThePathwayForTheActivity(String user, String pathwayName, String activityName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, deletePathway(interpolate(nameFrom(pathwayName, "id")),
                interpolate(nameFrom(activityName, "id"))));
    }

    @Then("^the \"([^\"]*)\" pathway is deleted$")
    public void thePathwayIsDeleted(String pathwayName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload(deletePathwayOk(interpolate(Variables.nameFrom(pathwayName, "id")))));
    }

    private void createPathway(String activityIdVar, String pathwayIdVar) {
        messageOperations.sendJSON(runner, PathwayHelper.createPathway(
                interpolate(activityIdVar), DEFAULT_PATHWAY_TYPE)
        );
        String expectedPathway = PathwayHelper.createPathwayOk("@variable('" + pathwayIdVar + "')@",
                interpolate(activityIdVar), DEFAULT_PATHWAY_TYPE);
        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway));
    }

    private void createPathway(String activityIdVar, String pathwayType, String pathwayIdVar) {
        messageOperations.sendJSON(runner, PathwayHelper.createPathway(
                interpolate(activityIdVar), pathwayType)
        );
        String expectedPathway = PathwayHelper.createPathwayOk("@variable('" + pathwayIdVar + "')@",
                interpolate(activityIdVar), pathwayType);
        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway));
    }

    @And("^\"([^\"]*)\" has deleted the \"([^\"]*)\" pathway for the \"([^\"]*)\" activity$")
    public void hasDeletedThePathwayForTheActivity(String user, String pathwayName, String activityName) {
        deletesThePathwayForTheActivity(user, pathwayName, activityName);
        thePathwayIsDeleted(pathwayName);
    }

    @And("^\"([^\"]*)\" pathway does not include interactive \"([^\"]*)\" in its children anymore$")
    public void pathwayDoesNotIncludeInteractiveInItsChildrenAnymore(String pathwayName, String interactiveName) {
        messageOperations.sendJSON(runner, PathwayHelper.getPathway(interpolate(nameFrom(pathwayName, "id"))));

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(PathwayHelper.validatePathwayPayload((payload, context) -> {
                    UUID deletedInteractiveId = UUID.fromString(context.getVariable(nameFrom(interactiveName, "id")));

                    List<WalkableChild> children = payload.getChildren();

                    assertNotNull(children);

                    assertTrue(children.stream().noneMatch(child-> child.getElementId().equals(deletedInteractiveId)));
                })));
    }

    @When("^\"([^\"]*)\" fetches the \"([^\"]*)\" pathway$")
    public void fetchesThePathway(String user, String pathwayName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, PathwayHelper.getPathway(interpolate(nameFrom(pathwayName, "id"))));
    }

    @Then("^\"([^\"]*)\" pathway has walkable children$")
    public void pathwayHasWalkableChildren(String pathwayName, List<String> children) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validatePathwayPayload((payload, context) -> {
                    String pathwayId = context.getVariable(interpolate(nameFrom(pathwayName, "id")));
                    assertNotNull(payload);
                    assertNotNull(payload.getChildren());
                    assertEquals(pathwayId, payload.getPathwayId().toString());
                    assertEquals(children.size(), payload.getChildren().size());

                    List<WalkableChild> actualChildren = payload.getChildren();

                    for (int i = 0; i < children.size(); i++) {
                        String childId = children.get(i);
                        context.setVariable(nameFrom(childId, "id"), actualChildren.get(i).getElementId().toString());
                    }
                })));
    }

    @Then("^\"([^\"]*)\" can not create a pathway for \"([^\"]*)\" activity due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotCreateAPathwayForActivityDueToErrorCodeMessage(String accountName, String activityName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        createsAPathwayForTheActivity(accountName, DEFAULT_PATHWAY_TYPE, activityName);
        thePathwayCreationFailsDueToCodeAndMessage(code, message);
    }

    @Then("^\"([^\"]*)\" can create a pathway for \"([^\"]*)\" activity successfully$")
    public void canCreateAPathwayForActivitySuccessfully(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        createPathway(nameFrom(activityName, "id"), PATHWAY_ID_VAR);
    }

    @Then("^\"([^\"]*)\" can not delete \"([^\"]*)\" pathway for \"([^\"]*)\" activity due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDeletePathwayForActivityDueToErrorCodeMessage(String accountName, String pathwayName, String activityName,
                                                                    int code, String message) {
        deletesThePathwayForTheActivity(accountName, pathwayName, activityName);
        messageOperations.receiveJSON(runner, action -> action.payload(deletePathwayError(code, message)));
    }

    @Then("^\"([^\"]*)\" can delete \"([^\"]*)\" pathway for \"([^\"]*)\" activity successfully$")
    public void canDeletePathwayForActivitySuccessfully(String accountName, String pathwayName, String activityName) {
        hasDeletedThePathwayForTheActivity(accountName, pathwayName, activityName);
    }

    @Then("^\"([^\"]*)\" can not fetch \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchPathwayDueToErrorCodeMessage(String accountName, String pathwayName, int code, String message) {
        fetchesThePathway(accountName, pathwayName);
        messageOperations.receiveJSON(runner, action -> action.payload(PathwayHelper.getPathwayErrorResponse(code,message)));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" pathway successfully$")
    public void canFetchPathwaySuccessfully(String accountName, String pathwayName) {
        fetchesThePathway(accountName, pathwayName);
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.pathway.get.ok"));
    }

    @When("{string} creates a {string} pathway for activity {string} with config")
    public void createsAPathwayForActivityWithConfig(String user, String pathwayType, String activityName, String config) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, PathwayHelper.createPathway(
                interpolate(nameFrom(activityName, "id")),
                pathwayType,
                config)
        );
    }

    @When("{string} creates a {string} pathway for activity {string} with a supplied id")
    public void createsAPathwayForActivityWithASuppliedId(String user, String pathwayType, String activityName) {
        authenticationSteps.authenticateUser(user);
        UUID pathwayId = runner.variable("SUPPLIED_PATHWAY_ID", com.smartsparrow.util.UUIDs.timeBased());

        messageOperations.sendJSON(runner, PathwayHelper.createPathway(
                interpolate(nameFrom(activityName, "id")),
                pathwayType,
                null,
                pathwayId)
        );
    }

    @When("{string} creates a {string} pathway for activity {string} with the same supplied id as before")
    public void createsAPathwayWithSameId(String user, String pathwayType, String activityName) {
        authenticationSteps.authenticateUser(user);
        UUID pathwayId = com.smartsparrow.util.UUIDs.fromString(getVariableValue("SUPPLIED_PATHWAY_ID"));

        messageOperations.sendJSON(runner, PathwayHelper.createPathway(
                interpolate(nameFrom(activityName, "id")),
                pathwayType,
                null,
                pathwayId)
        );
    }

    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    @Then("the pathway is not created due to conflict")
    public void thePathwayIsNotCreatedDueToConflict() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                Assertions.assertEquals("author.pathway.create.error", payload.getType());
                Assertions.assertEquals(401, payload.getCode().intValue());
                Assertions.assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @Then("the {string} pathway is succesfully created and has config")
    public void thePathwayIsSuccesfullyCreatedAndHasConfig(final String pathwayName, final String config) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {

                assertEquals("author.pathway.create.ok", payload.get("type"));

                Map response = (Map) payload.get("response");
                Map pathway = (Map) response.get("pathway");

                Object pathwayId = pathway.get("pathwayId");
                assertNotNull(pathwayId);

                context.setVariable(nameFrom(pathwayName, "id"), pathwayId.toString());

                assertEquals(config, pathway.get("config"));
            }
        }));
    }

    @And("{string} has created a {string} pathway for activity {string} with config")
    public void hasCreatedAPathwayForActivityWithConfig(String user, String pathwayName, String activityName, String config) {
        createsAPathwayForActivityWithConfig(user, DEFAULT_PATHWAY_TYPE, activityName, config);
        thePathwayIsSuccesfullyCreatedAndHasConfig(pathwayName, config);
    }

    @When("{string} replace config for {string} pathway with")
    public void replaceConfigForPathwayWith(String user, String pathwayName, String config) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, replaceConfig(interpolate(nameFrom(pathwayName, "id")), config));
    }

    @Then("the {string} pathway config are successfully replaced with")
    public void thePathwayConfigAreSuccessfullyReplacedWith(String pathwayName, String config) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {

                assertEquals("author.pathway.config.replace.ok", payload.get("type"));

                Map response = (Map) payload.get("response");
                Map pathwayConfig = (Map) response.get("config");

                String pathwayId = context.getVariable(interpolate(nameFrom(pathwayName, "id")));
                assertEquals(pathwayId, pathwayConfig.get("pathwayId"));
                assertEquals(config, pathwayConfig.get("config"));
            }
        }));
    }

    @Then("{string} pathway is returned with config")
    public void pathwayIsReturnedWithConfig(String pathwayName, String config) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {

                assertEquals("author.pathway.get.ok", payload.get("type"));

                Map response = (Map) payload.get("response");
                Map pathway = (Map) response.get("pathway");

                String pathwayId = context.getVariable(interpolate(nameFrom(pathwayName, "id")));

                assertEquals(pathwayId, pathway.get("pathwayId"));
                assertEquals(config, pathway.get("config"));
            }
        }));
    }

    @When("{string} has replaced config for {string} pathway with")
    public void hasReplacedConfigForPathwayWith(String user, String pathwayName, String config) {
        replaceConfigForPathwayWith(user, pathwayName, config);
        thePathwayConfigAreSuccessfullyReplacedWith(pathwayName, config);
    }

    @When("{string} creates a {string} pathway for the {string} activity")
    public void createsAPathwayForTheDefinedActivity(String user, String pathwayType, String activityName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, PathwayHelper.createPathway(
                interpolate(nameFrom(activityName, "id")), pathwayType)
        );

    }

    @Then("a {string} pathway named {string} is successfully created")
    public void aPathwayNamedIsSuccessfullyCreated(String pathwayType, String pathwayName) {
        String expectedPathway = PathwayHelper.createPathwayOk("@variable('" + nameFrom(pathwayName, "id") + "')@",
                "@notEmpty()@", pathwayType);
        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway));
    }

    @And("{string} has configured {string} interactive as starting walkable for {string} graph pathway")
    public void hasConfiguredInteractiveAsStartingWalkableForGraphPathway(String user, String interactiveName,
                                                                          String graphPathwayName) {
        String config = new PayloadBuilder()
                .addField("startingWalkableId", interpolate(nameFrom(interactiveName, "id")))
                .addField("startingWalkableType", "INTERACTIVE")
                .build();

        replaceConfigForPathwayWith(user, graphPathwayName, config);
        messageOperations.validateResponseType(runner, "author.pathway.config.replace.ok");
    }

    @When("{string} fetches the walkables for the first pathway in cohort {string} and deployment {string}")
    public void fetchesTheWalkablesForTheFirstPathwayInCohortAndDeployment(final String user,
                                                                           final String cohortName,
                                                                           final String deploymentName) {

        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchWalkablesGraphQlQuery(
                interpolate(nameFrom(cohortName, "id")),
                interpolate(nameFrom(deploymentName, "id"))
        ));
    }

    @When("{string} fetches the walkables for the {string} activity and {string} pathway in cohort {string} and deployment {string}")
    public void fetchesTheWalkablesForThePathwayInCohortAndDeployment(final String user,
                                                                           final String activityName,
                                                                           final String pathwayName,
                                                                           final String cohortName,
                                                                           final String deploymentName) {

        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchWalkablesForPathwayGraphQlQuery(
                interpolate(nameFrom(cohortName, "id")),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(activityName, "id")),
                interpolate(nameFrom(pathwayName, "id"))
        ));
    }

    @Then("{string} walkable is supplied")
    public void walkableIsSupplied(String walkableName) {
        String walkablePath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node";
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(walkablePath + ".id", interpolate(nameFrom(walkableName, "id"))));
    }

    @And("{string} has set {int} as exit after condition for the {string} pathway")
    public void hasSetAsExitAfterConditionForThePathway(String user, int exitAfter, String pathwayName) {
        String config = new PayloadBuilder()
                .addField("exitAfter", exitAfter)
                .build();

        replaceConfigForPathwayWith(user, pathwayName, config);
        messageOperations.validateResponseType(runner, "author.pathway.config.replace.ok");
    }

    @Then("a {string} is supplied between")
    public void aIsSuppliedBetween(String randomWalkableName, List<String> expectedWalkables) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(final Map payload, final Map<String, Object> headers, final TestContext context) {
                List edges = getWalkableEdges(payload);
                Map edge = (Map) edges.get(0);
                Map node = (Map) edge.get("node");

                String receivedWalkableId = (String) node.get("id");

                List<String> anyExpectedWalkableIds = expectedWalkables.stream()
                        .map(expectedWalkable -> context.getVariable(interpolate(nameFrom(expectedWalkable, "id"))))
                        .collect(Collectors.toList());

                assertTrue(anyExpectedWalkableIds.contains(receivedWalkableId));

                context.setVariable(nameFrom(randomWalkableName, "id"), receivedWalkableId);
                context.setVariable(nameFrom(randomWalkableName, "studentScope"), node.get("studentScopeURN"));
            }
        }));
    }

    @Then("a {string} is supplied excluding")
    public void aIsSuppliedExcluding(String randomWalkableName, List<String> excludedWalkables) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(final Map payload, final Map<String, Object> headers, final TestContext context) {
                List edges = getWalkableEdges(payload);
                Map edge = (Map) edges.get(0);
                Map node = (Map) edge.get("node");

                String receivedWalkableId = (String) node.get("id");

                List<String> allExcludedWalkableIds = excludedWalkables.stream()
                        .map(excludedWalkable -> context.getVariable(interpolate(nameFrom(excludedWalkable, "id"))))
                        .collect(Collectors.toList());

                assertFalse(allExcludedWalkableIds.contains(receivedWalkableId));

                context.setVariable(nameFrom(randomWalkableName, "id"), receivedWalkableId);
                context.setVariable(nameFrom(randomWalkableName, "studentScope"), node.get("studentScopeURN"));
            }
        }));
    }

    @Then("no walkables are returned")
    public void noWalkablesAreReturned() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(final Map payload, final Map<String, Object> headers, final TestContext context) {
                List edges = getWalkableEdges(payload);

                assertTrue(edges.isEmpty());
            }
        }));
    }

    private List getWalkableEdges(final Map payload) {
        Map response = (Map) payload.get("response");
        Map data = (Map) response.get("data");
        Map learn = (Map) data.get("learn");
        Map cohort = (Map) learn.get("cohort");
        List deployments = (List) cohort.get("deployment");
        Map deployment = (Map) deployments.get(0);
        Map activity = (Map) deployment.get("activity");
        List pathways = (List) activity.get("pathways");
        Map pathway = (Map) pathways.get(0);
        Map walkables = (Map) pathway.get("walkables");
        return (List) walkables.get("edges");
    }

    @And("{string} has set config for {string} pathway with")
    public void hasSetConfigForPathwayWith(String user, String pathwayName, String config) {
        replaceConfigForPathwayWith(user, pathwayName, config);
        messageOperations.validateResponseType(runner, "author.pathway.config.replace.ok");
    }

    @Then("the {string} pathway includes the {string} description")
    public void thePathwayIncludesTheDescription(String pathwayName, String description) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload(
                (payload, context) -> {
                    String activityId = context.getVariable(interpolate(nameFrom(pathwayName, "id")));
                    Assertions.assertEquals(activityId, payload.getPathwayId().toString());
                    Assertions.assertEquals(description, payload.getDescription());
                })));
    }

    @Then("the {string} pathway has empty walkable children")
    public void thePathwayHasEmptyWalkableChildren(String pathwayName) {
        messageOperations.sendJSON(runner, PathwayHelper.getPathway(interpolate(nameFrom(pathwayName, "id"))));
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(PathwayHelper.validatePathwayPayload((payload, context) -> {
                    List<WalkableChild> children = payload.getChildren();
                    assertNull(children);
                })));
    }

    @When("{string} creates a {string} pathway for the activity with preload pathway {string}")
    public void createsALINEARPathwayForTheActivityWithPreload(String accountName, String pathwayType, String preloadPathway) {
        authenticationSteps.authenticateUser(accountName);
        String activityVar = ACTIVITY_ID_VAR ;
        messageOperations.sendJSON(runner, PathwayHelper.createPathwayWithPreload(interpolate(activityVar), pathwayType, null, preloadPathway));
    }

    @When("{string} creates a {string} pathway for activity {string} with a supplied id and preload pathway {string}")
    public void createsAPathwayForActivityWithASuppliedIdAndPreload(String user,
                                                                    String pathwayType,
                                                                    String activityName,
                                                                    String preloadPathway) {
        authenticationSteps.authenticateUser(user);
        UUID pathwayId = runner.variable("SUPPLIED_PATHWAY_ID", com.smartsparrow.util.UUIDs.timeBased());

        messageOperations.sendJSON(runner, PathwayHelper.createPathwayWithPreload(
                interpolate(nameFrom(activityName, "id")),
                pathwayType,
                null,
                pathwayId,
                preloadPathway)
        );
    }

    @When("{string} created a {string} pathway for the activity {string} with preload pathway {string}")
    public void createsAPathwayForTheActivityWithPreloadPathway(String accountName, String pathwayType, String activityName, String preloadPathway) {
        authenticationSteps.authenticateUser(accountName);
        String activityVar = Variables.nameFrom(activityName, "id") ;
        String pathwayIdVar = Variables.nameFrom(pathwayType, "id");
        messageOperations.sendJSON(runner, PathwayHelper.createPathwayWithPreload(interpolate(activityVar), DEFAULT_PATHWAY_TYPE, null, preloadPathway));
        String expectedPathway = PathwayHelper.createPathwayOk("@variable('" + pathwayIdVar + "')@", interpolate(activityVar),
                                                               DEFAULT_PATHWAY_TYPE);

        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway));
    }

    @And("{string} has created a {string} pathway named {string} for the {string} activity with preload pathway {string}")
    public void hasCreatedAPathwayNamedForTheActivityWithPreloadPathway(String accountName, String pathwayType, String pathwayName, String activityName, String preloadPathway) {
        authenticationSteps.authenticateUser(accountName);
        String activityVar = Variables.nameFrom(activityName, "id") ;
        String pathwayIdVar = Variables.nameFrom(pathwayName, "id");
        messageOperations.sendJSON(runner, PathwayHelper.createPathwayWithPreload(interpolate(activityVar), pathwayType, null, preloadPathway));
        String expectedPathway = PathwayHelper.createPathwayOk("@variable('" + pathwayIdVar + "')@", interpolate(activityVar),
                                                               pathwayType);

        messageOperations.receiveJSON(runner, action -> action.payload(expectedPathway));
    }
}
