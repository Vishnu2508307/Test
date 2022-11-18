package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.courseware.ActivityDuplicateSteps.ACTIVITY_COPY_ID_VAR;
import static mercury.glue.step.courseware.ActivitySteps.ACTIVITY_ID_VAR;
import static mercury.glue.step.courseware.InteractiveSteps.INTERACTIVE_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.ScenarioHelper.createChangeProgressAction;
import static mercury.helpers.courseware.ScenarioHelper.createScenarioErrorResponse;
import static mercury.helpers.courseware.ScenarioHelper.createScenarioRequest;
import static mercury.helpers.courseware.ScenarioHelper.createSendFeedbackAction;
import static mercury.helpers.courseware.ScenarioHelper.createSetCompetencyAction;
import static mercury.helpers.courseware.ScenarioHelper.listScenariosErrorResponse;
import static mercury.helpers.courseware.ScenarioHelper.listScenariosRequest;
import static mercury.helpers.courseware.ScenarioHelper.reorderScenarioErrorResponse;
import static mercury.helpers.courseware.ScenarioHelper.reorderScenariosRequest;
import static mercury.helpers.courseware.ScenarioHelper.reorderScenariosResponse;
import static mercury.helpers.courseware.ScenarioHelper.scenarioDuplicatedListValidationCallback;
import static mercury.helpers.courseware.ScenarioHelper.scenarioListValidationCallback;
import static mercury.helpers.courseware.ScenarioHelper.scenarioValidationCallback;
import static mercury.helpers.courseware.ScenarioHelper.updateScenarioRequest;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Enums;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.courseware.ScenarioHelper;
import mercury.stubs.scenario.ActionDataStub;
import mercury.stubs.scenario.ConditionDataStub;

public class ScenarioSteps {

    public static final String SCENARIO_ID_VAR = "scenario_id";
    public static final String SCENARIO_ID = "scenario_id_";
    public static final String SCENARIO_LIFECYCLE = "scenario_lifecycle_";
    public static final String DEFAULT_LIFECYCLE = "INTERACTIVE_PRE_ENTRY";

    private static final Map<String, String> scenarioArgs = new HashMap<String, String>() {
        {
            putAll(ActionDataStub.ALL);
            putAll(ConditionDataStub.ALL);
        }
    };

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates a scenario with$")
    public void createsAScenarioWith(String user, Map<String, String> args) {

        Map<String, String> arguments = new HashMap<>(args);

        arguments.put("condition", scenarioArgs.get(args.get("condition")));
        if(StringUtils.isNotBlank(args.get("gradePassBackActions"))) {
            arguments.put("actions", scenarioArgs.get(args.get("gradePassBackActions")));
        } else {
            arguments.put("actions", scenarioArgs.get(args.get("actions")));
        }

        createScenario(user, arguments, interpolate(ACTIVITY_ID_VAR));
        runner.variable("expected_scenario", arguments);
    }

    @And("^\"([^\"]*)\" has created interactive scenario \"([^\"]*)\"$")
    public void hasCreatedInteractiveScenario(String user, String scenarioName) {
        Map<String, String> fields = new HashMap<>(2);
        String condition = ConditionDataStub.conditionWith(
                "IS",
                "STRING",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "selection",
                "value");
        fields.put("name", scenarioName);
        fields.put("lifecycle", DEFAULT_LIFECYCLE);
        fields.put("condition", condition);
        fields.put("actions", ActionDataStub.actions());
        createScenario(user, fields, interpolate(INTERACTIVE_ID_VAR));
        scenarioIsCreated(scenarioName);
    }

    @When("^\"([^\"]*)\" has created scenario with$")
    public void hasCreatedScenarioWith(String accountName, Map<String, String> args) {
        Map<String, String> arguments = new HashMap<>(args);

        arguments.put("condition", scenarioArgs.get(args.get("condition")));
        arguments.put("actions", scenarioArgs.get(args.get("actions")));
        createScenario(accountName, arguments, interpolate(ACTIVITY_ID_VAR));
        scenarioIsCreated(arguments.get("name"));
    }

    @And("^(?:|she|he) has created scenarios for the \"([^\"]*)\" activity$")
    public void sheHasCreatedScenariosForTheActivity(String activityName, Map<String, String> scenarios ) {
        hasCreatedScenariosForTheActivity(activityName, scenarios);
    }

    private void hasCreatedScenariosForTheActivity(@Nullable String activityName, Map<String, String> scenarios) {
        final String activityId = (activityName != null) ? nameFrom(activityName, "id") : ACTIVITY_ID_VAR;
        //authenticate the current user if needed
        Map<String, List<String>> expectedScenarios = new HashMap<>();
        for (Map.Entry<String, String> scenario : scenarios.entrySet()) {

            if (!expectedScenarios.containsKey(scenario.getValue())) {
                expectedScenarios.put(scenario.getValue(), new ArrayList<>());
            }
            expectedScenarios.get(scenario.getValue()).add(scenario.getKey());

            messageOperations.sendJSON(runner, createScenarioRequest(scenario.getKey(), scenario.getValue(), interpolate(activityId)));
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "author.scenario.create.ok")
                    .extractFromPayload("$.response.scenario.id", SCENARIO_ID + scenario.getKey())
                    .extractFromPayload("$.response.scenario.lifecycle", SCENARIO_LIFECYCLE + scenario.getKey()));
        }

        runner.variable("expected_scenarios", expectedScenarios);
    }

    private void createScenario(String accountName, Map<String, String> args, String parentId) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, createScenarioRequest(args, parentId));
    }

    private void scenarioIsCreated(String scenarioName) {
        messageOperations.receiveJSON(runner, action -> action
                .extractFromPayload("$.response.scenario.id", SCENARIO_ID + scenarioName));
        //hack to have the latest created scenario id in SCENARIO_ID variable. needs to for updating scenario
        runner.variable(SCENARIO_ID_VAR, interpolate(SCENARIO_ID + scenarioName));
    }

    @Then("^the scenario is successfully created$")
    public void theScenarioIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(scenarioValidationCallback("author.scenario.create.ok", "expected_scenario")));
    }

    @Then("^the scenario is not created due to message \"([^\"]*)\" and code (\\d+)$")
    public void theScenarioIsNotCreatedDueToMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.scenario.create.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"
        ));
    }

    @When("^\"([^\"]*)\" updates scenario with$")
    public void updatesScenarioWith(String accountName, Map<String, String> args) {
        Map<String, String> arguments = new HashMap<>(args);
        arguments.put("condition", scenarioArgs.get(args.get("condition")));
        arguments.put("actions", scenarioArgs.get(args.get("actions")));
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, updateScenarioRequest(interpolate(SCENARIO_ID_VAR), arguments));
    }

    @Then("^the updated scenario has fields$")
    public void theUpdatedScenarioHasFields(Map<String, String> expectedArgs) {
        Map<String, String> expectedScenario = new HashMap<>(expectedArgs);
        expectedScenario.put("condition", scenarioArgs.get(expectedArgs.get("condition")));
        expectedScenario.put("actions", scenarioArgs.get(expectedArgs.get("actions")));
        runner.variable("updated_scenario", expectedScenario);
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(scenarioValidationCallback("author.scenario.replace.ok", "updated_scenario")));
    }

    @Then("^scenario \"([^\"]*)\" is successfully updated$")
    public void scenarioIsSuccessfullyUpdated(String scenarioName) {
        messageOperations.receiveJSON(runner, action -> action
                .payload("{" +
                        "\"type\":\"author.scenario.replace.ok\"," +
                        "\"response\":{" +
                        "\"scenario\":{" +
                        "\"id\":\"" + interpolate(SCENARIO_ID + scenarioName) + "\"," +
                        "\"condition\":\"@notEmpty()@\"," +
                        "\"actions\":\"updated actions\"," +
                        "\"name\":\"updated name\"," +
                        "\"description\":\"a flashy new description\"," +
                        "\"correctness\":\"is it correct?\"}},\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("^(?:|she|he) reorders the scenarios for the created \"([^\"]*)\" as$")
    public void reordersTheScenariosForTheCreated(String parentEntityName, List<String> scenariosToReorder) {
        reordersTheScenariosForTheCreated(parentEntityName, DEFAULT_LIFECYCLE, scenariosToReorder);
    }

    @When("^(?:|she|he) reorders the scenarios for the created \"([^\"]*)\" and \"([^\"]*)\" lifecycle as$")
    public void reordersTheScenariosForTheCreated(String parentEntityName, String lifecycle, List<String> scenariosToReorder) {
        messageOperations.sendJSON(runner, reorderScenariosRequest(parentEntityName, lifecycle,
                convertToScenarioIds(scenariosToReorder)));
    }


    @Then("^scenarios are successfully ordered as$")
    public void scenariosAreSuccessfullyOrderedAs(List<String> expectedOrderedScenarios) {
        scenariosAreSuccessfullyOrderedAs(DEFAULT_LIFECYCLE, expectedOrderedScenarios);
    }

    @Then("^scenarios for lifecycle \"([^\"]*)\" are successfully ordered as$")
    public void scenariosAreSuccessfullyOrderedAs(String lifecycle, List<String> expectedOrderedScenarios) {
        messageOperations.receiveJSON(runner, action ->
                action.payload(reorderScenariosResponse(lifecycle, convertToScenarioIds(expectedOrderedScenarios))));
    }

    @Then("^the list of scenarios for the activity and lifecycle ([^\"]*) contains$")
    public void theListOfScenariosForTheActivityAndLifecycleContains(String lifecycle, List<String> expectedScenarios) {
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(ACTIVITY_ID_VAR), lifecycle));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(ArrayList payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(expectedScenarios.size(), payload.size());
                        for (int i = 0; i < expectedScenarios.size(); i++) {
                            String scenarioName = expectedScenarios.get(i);
                            Map scenarioPayload = (Map) payload.get(i);
                            assertEquals(context.getVariable("scenario_id_" + scenarioName), scenarioPayload.get("id"));
                            assertEquals(scenarioName, scenarioPayload.get("name"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "scenarios";
                    }

                    @Override
                    public String getType() {
                        return "author.scenario.list.ok";
                    }
                }));
    }

    @And("^\"([^\"]*)\" has reordered the scenarios for the activity and lifecycle ([^\"]*)$")
    public void hasReorderedTheScenariosForTheActivityAndLifecycle(String accountName, String lifecycle, List<String> listToReorder) {
        List<String> scenarioIds = convertToScenarioIds(listToReorder);
        messageOperations.sendJSON(runner, reorderScenariosRequest("activity", lifecycle, scenarioIds));
        messageOperations.receiveJSON(runner, action -> action.payload(reorderScenariosResponse(lifecycle, scenarioIds)));
    }

    @Then("^the list of scenarios for the activity and lifecycle ([^\"]*) contains updated scenario$")
    public void theListOfScenariosForTheActivityAndLifecycleContainsScenario(String lifecycle, Map<String, String> fields) {
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(ACTIVITY_ID_VAR), lifecycle));

        final Map<String, String> expectedFields = new HashMap<>(fields);
        expectedFields.put("condition", scenarioArgs.get(fields.get("condition")));
        expectedFields.put("actions", scenarioArgs.get(fields.get("actions")));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                scenarioListValidationCallback("author.scenario.list.ok", expectedFields)));
    }

    private List<String> convertToScenarioIds(List<String> scenarioNames) {
        return scenarioNames.stream()
                .map(one -> interpolate(SCENARIO_ID + one))
                .collect(Collectors.toList());
    }

    @And("^the list of scenarios for the new copy and lifecycle ([^\"]*) contains$")
    public void theListOfScenariosForTheNewCopyAndLifecycleContains(String lifecycle, List<String> scenarios) {
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(ACTIVITY_COPY_ID_VAR), lifecycle));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                scenarioDuplicatedListValidationCallback(scenarios)));
    }

    @Then("^\"([^\"]*)\" can not create a scenario due to message \"([^\"]*)\" and code (\\d+)$")
    public void canNotCreateAScenarioDueToMessageAndCode(String accountName, String message, int code) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                createScenarioRequest("default name", ScenarioLifecycle.ACTIVITY_COMPLETE.name(), interpolate(ACTIVITY_ID_VAR)));
        messageOperations.receiveJSON(runner, action -> action.payload(createScenarioErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can create a scenario successfully$")
    public void canCreateAScenarioSuccessfully(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                createScenarioRequest("default name", ScenarioLifecycle.ACTIVITY_COMPLETE.name(), interpolate(ACTIVITY_ID_VAR)));
        messageOperations.validateResponseType(runner, "author.scenario.create.ok");
    }

    @Then("^\"([^\"]*)\" can not reorder scenarios for the activity due to message \"([^\"]*)\" and code (\\d+)$")
    public void canNotReorderScenariosDueToMessageAndCode(String accountName, String message, int code) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                reorderScenariosRequest("activity", ScenarioLifecycle.ACTIVITY_COMPLETE.name(), Lists.newArrayList()));
        messageOperations.receiveJSON(runner, action -> action.payload(reorderScenarioErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can not reorder scenarios for the activity successfully$")
    public void canNotReorderScenariosSuccessfully(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                reorderScenariosRequest("activity", ScenarioLifecycle.ACTIVITY_COMPLETE.name(), Lists.newArrayList()));
        messageOperations.validateResponseType(runner, "author.scenarios.reorder.ok");
    }

    @Then("^\"([^\"]*)\" can not fetch list of scenarios for the activity due to message \"([^\"]*)\" and code (\\d+)$")
    public void canNotFetchListOfScenariosForTheActivityDueToMessageAndCode(String accountName, String message, int code) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(ACTIVITY_ID_VAR), ScenarioLifecycle.ACTIVITY_COMPLETE.name()));
        messageOperations.receiveJSON(runner, action -> action.payload(listScenariosErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can fetch a list of scenarios for the activity successfully$")
    public void canFetchAListOfScenariosForTheActivitySuccessfully(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(ACTIVITY_ID_VAR), ScenarioLifecycle.ACTIVITY_COMPLETE.name()));
        messageOperations.validateResponseType(runner, "author.scenario.list.ok");
    }

    @When("^\"([^\"]*)\" has created scenario \"([^\"]*)\" for activity \"([^\"]*)\"$")
    public void hasCreatedScenarioForActivity(String user, String scenarioName, String activityName) {
        authenticationSteps.authenticateUser(user);

        String condition = ConditionDataStub
                .conditionWith(
                        "IS",
                        "STRING",
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        "selection",
                        "value"
                );
        Map<String, String> args = new HashMap<String, String>() {
            {put("name", "name");}
            {put("lifecycle", "ACTIVITY_EVALUATE");}
            {put("condition", condition);}
            {put("actions", "[]");}
            {put("description", "a description");}
            {put("correctness", "correct");}
        };

        messageOperations.sendJSON(runner, createScenarioRequest(args, interpolate(nameFrom(activityName, "id"))));

        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.scenario.id", nameFrom(scenarioName, "id")));

    }

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" scenario$")
    public void shouldReceiveAnActionMessageForTheScenario(String clientName, String actionName, String scenarioName) {
        messageOperations.receiveJSON(runner, action -> {
            action.jsonPath("$.response.elementId", interpolate(nameFrom(scenarioName, "id")))
                    .jsonPath("$.response.parentElementId", "@notEmpty()@")
                    .jsonPath("$.response.parentElementType", "@notEmpty()@")
                    .jsonPath("$.response.lifecycle", "@notEmpty()@")
                    .jsonPath("$.response.action", actionName)
                    .jsonPath("$.response.rtmEvent",
                              actionName.startsWith("SCENARIO") ? actionName : "SCENARIO_" + actionName);
        }, clientName);
    }

    @When("^\"([^\"]*)\" has updated the \"([^\"]*)\" scenario$")
    public void updatesTheScenario(String user, String scenarioName) {

        authenticationSteps.authenticateUser(user);

        String condition = ConditionDataStub
                .conditionWith(
                        "IS",
                        "STRING",
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        "selection",
                        "value"
                );

        Map<String, String> args = new HashMap<String, String>() {
            {put("name", "name");}
            {put("condition", condition);}
            {put("actions", "[]");}
            {put("description", "an updated description");}
            {put("correctness", "correct");}
        };

        messageOperations.sendJSON(runner, updateScenarioRequest(interpolate(nameFrom(scenarioName, "id")), args));

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.scenario.replace.ok"));
    }

    @When("^\"([^\"]*)\" has reordered the \"([^\"]*)\" scenarios to$")
    public void hasReorderedTheScenariosTo(String user, String activityName, List<String> scenarioIds) {
        authenticationSteps.authenticateUser(user);

        List<String> interpolated = scenarioIds.stream()
                .map(scenarioId -> interpolate(nameFrom(scenarioId, "id")))
                .collect(Collectors.toList());

        messageOperations.sendJSON(runner, reorderScenariosRequest(activityName, "ACTIVITY_EVALUATE",
                interpolated));
        messageOperations.receiveJSON(runner, action ->
                action.payload(reorderScenariosResponse("ACTIVITY_EVALUATE", interpolated)));
    }


    @And("^\"([^\"]*)\" has created a scenario \"([^\"]*)\" for the \"([^\"]*)\" interactive$")
    public void hasCreatedAScenarioForTheInteractive(String accountName, String scenarioName, String interactiveName) {
        Map<String, String> fields = new HashMap<>(2);
        fields.put("name", scenarioName);
        fields.put("lifecycle", DEFAULT_LIFECYCLE);

        String condition = ConditionDataStub
                .conditionWith(
                        "IS",
                        "STRING",
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        "value",
                        ""
                );

        fields.put("condition", condition);
        fields.put("actions", ActionDataStub.actions());

        createScenario(accountName, fields, interpolate(nameFrom(interactiveName, "id")));
        messageOperations.receiveJSON(runner, action -> action
                .extractFromPayload("$.response.scenario.id", nameFrom(scenarioName, "id")));
    }

    @When("^\"([^\"]*)\" has reordered the \"([^\"]*)\" interactive scenarios to$")
    public void hasReorderedTheInteractiveScenariosTo(String user, String interactiveName, List<String> scenarioIds) {
        authenticationSteps.authenticateUser(user);

        List<String> interpolated = scenarioIds.stream()
                .map(scenarioId -> interpolate(nameFrom(scenarioId, "id")))
                .collect(Collectors.toList());

        messageOperations.sendJSON(runner, reorderScenariosRequest(interactiveName, DEFAULT_LIFECYCLE,
                interpolated));
        messageOperations.receiveJSON(runner, action ->
                action.payload(reorderScenariosResponse(DEFAULT_LIFECYCLE, interpolated)));
    }

    @Then("^\"([^\"]*)\" should receive an action scenario \"([^\"]*)\" message for the \"([^\"]*)\" activity$")
    public void shouldReceiveAnActionScenarioMessageForTheActivity(String clientName, String actionName, String activityName) {
        messageOperations.receiveJSON(runner, action -> {
            action.jsonPath("$.response.elementId", interpolate(nameFrom(activityName, "id")))
                    .jsonPath("$.response.elementType", "ACTIVITY")
                    .jsonPath("$.response.scenarioIds", "@notEmpty()@")
                    .jsonPath("$.response.action", actionName)
                    .jsonPath("$.response.rtmEvent",
                              actionName.startsWith("SCENARIO") ? actionName : "SCENARIO_" + actionName);
        }, clientName);
    }

    @Given("^\"([^\"]*)\" has created scenario for \"([^\"]*)\" activity with references$")
    public void hasCreatedScenarioForActivityWithReferences(String user, String activityName, Map<String, String> args) {
        Map<String, String> fields = new HashMap<>(args);
        fields.put("actions", ActionDataStub.actions());
        fields.put("condition", ConditionDataStub.condition());

        createScenario(user, fields, interpolate(nameFrom(activityName, "id")));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.scenario.create.ok"));
    }


    @Then("^the list of scenarios for the \"([^\"]*)\" activity and lifecycle ([^\"]*) contains duplicated scenario$")
    public void theListOfScenariosForTheActivityAndLifecycleContainsDuplicatedScenario(String activityName,
                                                                                       String lifecycle,
                                                                                       Map<String, String> fields) {
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(nameFrom(activityName, "id")), lifecycle));
        Map<String, String> map  = new HashMap<>(fields);
        map.put("actions", ActionDataStub.actions());
        map.put("condition", ConditionDataStub.condition());
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                scenarioListValidationCallback("author.scenario.list.ok", map)));
    }

    @And("^\"([^\"]*)\" has created a scenario \"([^\"]*)\" for the \"([^\"]*)\" interactive with$")
    public void hasCreatedAScenarioForTheInteractiveWith(String user, String scenarioName, String interactiveName,
                                                         Map<String, String> args) {

        String correctness = args.get("correctness");

        authenticationSteps.authenticateUser(user);

        String documentItemName = args.get("awardDocumentItem");
        String documentName = args.get("awardFromDocument");

        String competencyAction = "";

        if (documentItemName != null && documentName != null) {
            competencyAction = ", " + createSetCompetencyAction(
                    interpolate(nameFrom(documentName, "id")),
                    interpolate(nameFrom(documentItemName, "id")),
                    1
            );
        }

        final String competencyMetAction = competencyAction;

        String condition = getCondition(args);

        Map<String, String> fields = new HashMap<String, String>(){
            {
                put("name", scenarioName);
                put("lifecycle", ScenarioLifecycle.INTERACTIVE_EVALUATE.name());
                put("condition", condition);
                put("actions", "[" +
                        getProgressAction(interactiveName, correctness) +
                        ", " + ScenarioHelper.createUnsupportedAction("GRADE") +
                        ", " + ScenarioHelper.createUnsupportedAction("FUBAR") +
                        ", " + getScoreAction(interactiveName, correctness) +
                        ", " + createSendFeedbackAction("Well done mate!") + competencyMetAction +
                        "]");
                put("description", "a description");
                put("correctness", correctness);
            }
        };


        messageOperations.sendJSON(runner, createScenarioRequest(fields, interpolate(nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.scenario.create.ok")
                .extractFromPayload("$.response.scenario.id", nameFrom(scenarioName, "id")));
    }

    @And("^\"([^\"]*)\" has created a scenario \"([^\"]*)\" for the \"([^\"]*)\" interactive with action \"([^\"]*)\"$")
    public void hasCreatedAScenarioForTheInteractiveWithAction(String user, String scenarioName, String interactiveName,
                                                               String actionType,
                                                               Map<String, String> args) {

        authenticationSteps.authenticateUser(user);

        String condition = getCondition(args);

        Map<String, String> fields = new HashMap<String, String>(){
            {
                put("name", scenarioName);
                put("lifecycle", ScenarioLifecycle.INTERACTIVE_EVALUATE.name());
                put("condition", condition);
                put("actions", "[" +
                        ScenarioHelper.createChangeScopeActionWithScopeResolver(
                                interpolate(nameFrom(args.get("studentScopeURN"), "studentScope")),
                                interpolate(nameFrom(args.get("sourceId"), "id")),
                                "foo",
                                DataType.NUMBER,
                                MutationOperator.ADD
                                ) +
                        "]");
                put("description", "a description");
                put("correctness", args.get("correctness"));
            }
        };


        messageOperations.sendJSON(runner, createScenarioRequest(fields, interpolate(nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.scenario.create.ok"));
    }

    @And("^\"([^\"]*)\" has created a scenario \"([^\"]*)\" for the \"([^\"]*)\" interactive with action \"([^\"]*)\" targeting the new property$")
    public void hasCreatedAScenarioForTheInteractiveWithActionTargetingProperty(String user, String scenarioName, String interactiveName,
                                                               String actionType,
                                                               Map<String, String> args) {

        authenticationSteps.authenticateUser(user);

        String condition = getCondition(args);

        Map<String, String> fields = new HashMap<String, String>(){
            {
                put("name", scenarioName);
                put("lifecycle", ScenarioLifecycle.INTERACTIVE_EVALUATE.name());
                put("condition", condition);
                put("actions", "[" +
                        ScenarioHelper.createChangeScopeActionWithScopeResolverNewEditable(
                                interpolate(nameFrom(args.get("studentScopeURN"), "studentScope")),
                                interpolate(nameFrom(args.get("sourceId"), "id")),
                                "json",
                                DataType.BOOLEAN
                        ) +
                        "]");
                put("description", "a description");
                put("correctness", args.get("correctness"));
            }
        };


        messageOperations.sendJSON(runner, createScenarioRequest(fields, interpolate(nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.scenario.create.ok"));
    }

    @And("{string} has created a scenario {string} for the {string} interactive with {string} action for")
    public void hasCreatedAScenarioForTheInteractiveWithInteractiveActionFor(final String user,
                                                                                                final String scenarioName,
                                                                                                final String interactiveName,
                                                                                                final String actionName,
                                                                                                final Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        String condition = getCondition(args);

        Map<String, String> fields = new HashMap<String, String>(){
            {
                put("name", scenarioName);
                put("lifecycle", ScenarioLifecycle.INTERACTIVE_EVALUATE.name());
                put("condition", condition);
                put("actions", "[" +
                        ScenarioHelper.createChangeProgressAction(
                                Enums.of(ProgressionType.class, actionName),
                                interpolate(nameFrom(args.get("elementId"), "id")),
                                args.get("elementType")
                                ) +
                        "]");
                put("description", "a scenario with a go to action");
                put("correctness", "correct");
            }
        };

        messageOperations.sendJSON(runner, createScenarioRequest(fields, interpolate(nameFrom(interactiveName, "id"))));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.scenario.create.ok"));
    }

    @Nonnull
    private String getCondition(Map<String, String> args) {
        return ConditionDataStub
                .conditionWith(
                        "EQUALS",
                        "NUMBER",
                        interpolate(nameFrom(args.get("sourceId"), "id")),
                        interpolate(nameFrom(args.get("studentScopeURN"), "studentScope")),
                        "selection",
                        args.get("expected")
                );
    }

    /**
     * Build a change score action for the supplied interactive
     *
     * @param interactiveName the interactive the change score action targets
     * @param correctness the scenario correctness
     * @return an action with ADD 10 when the scenarioCorrectness is `correct` or an action with REMOVE 2 when the
     * scenarioCorrectness is not `correct`
     */
    private String getScoreAction(String interactiveName, String correctness) {
        if (correctness.equals("correct")) {
            return ScenarioHelper.createChangeScoreAction(interpolate(nameFrom(interactiveName, "id")),
                    "INTERACTIVE", 10d, "ADD");
        }
        return ScenarioHelper.createChangeScoreAction(interpolate(nameFrom(interactiveName, "id")),
                    "INTERACTIVE", 2d, "REMOVE");
    }

    /**
     * Build a grade passback action for the supplied interactive
     *
     * @param interactiveName the interactive the change score action targets
     * @param correctness the scenario correctness
     * @return an action with SET 1.0 when the scenarioCorrectness is `correct` or an action with SET 0.5 when the
     * scenarioCorrectness is not `correct`
     */
    // todo finish this implemntation when ready
    private String getGradePassbackAction(String interactiveName, String correctness) {
        if (correctness.equals("correct")) {
            return ScenarioHelper.createGradePassbackAction(interpolate(nameFrom(interactiveName, "id")),
                    "INTERACTIVE", 1.0d, "SET");
        }
        return ScenarioHelper.createGradePassbackAction(interpolate(nameFrom(interactiveName, "id")),
                "INTERACTIVE", 0.5d, "SET");
    }

    /**
     * Build a progress action for the supplied interactive
     *
     * @param interactiveName the interactive to build the progress action for
     * @param correctness the scenario correctness
     * @return return a {@link ProgressionType#INTERACTIVE_COMPLETE} when the correctness is `correct` or
     * {@link ProgressionType#INTERACTIVE_REPEAT} when the correctness is not `correct`
     */
    private String getProgressAction(String interactiveName, String correctness) {
        if (correctness.equals("correct")) {
            return createChangeProgressAction(ProgressionType.INTERACTIVE_COMPLETE,
                    interpolate(nameFrom(interactiveName, "id")));
        }
        return createChangeProgressAction(ProgressionType.INTERACTIVE_REPEAT,
                interpolate(nameFrom(interactiveName, "id")));
    }

    @And("the list of scenarios for {string} {string} and lifecycle {string} contains")
    public void theListOfScenariosForAndLifecycleContains(String entityName, String entityType, String lifecycle, List<String> expectedScenarios) {
        messageOperations.sendJSON(runner, listScenariosRequest(interpolate(nameFrom(entityName, "id")), lifecycle));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                scenarioDuplicatedListValidationCallback(expectedScenarios)));
    }

    @And("{string} has created scenarios for {string} activity")
    public void hasCreatedScenariosForActivity(String accountName, String activityName, Map<String, String> scenarios) {
        authenticationSteps.authenticateUser(accountName);
        hasCreatedScenariosForTheActivity(activityName, scenarios);
    }

    @And("{string} has created a scenario {string} for the {string} activity with {string} action for")
    public void hasCreatedAScenarioForTheActivityWithActivityActionFor(final String user,
                                                                                          final String scenarioName,
                                                                                          final String activityName,
                                                                                          final String actionName,
                                                                                          final Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        String condition = getCondition(args);

        Map<String, String> fields = new HashMap<String, String>(){
            {
                put("name", scenarioName);
                put("lifecycle", ScenarioLifecycle.ACTIVITY_COMPLETE.name());
                put("condition", condition);
                put("actions", "[" +
                        ScenarioHelper.createChangeProgressAction(
                                Enums.of(ProgressionType.class, actionName),
                                interpolate(nameFrom(args.get("elementId"), "id")),
                                args.get("elementType")
                        ) +
                        "]");
                put("description", "a scenario with a go to action");
                put("correctness", "correct");
            }
        };

        messageOperations.sendJSON(runner, createScenarioRequest(fields, interpolate(nameFrom(activityName, "id"))));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.scenario.create.ok"));
    }
}
