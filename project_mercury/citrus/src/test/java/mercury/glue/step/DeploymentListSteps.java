package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Maps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class DeploymentListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("^\"([^\"]*)\" lists deployments for the cohort$")
    public void listsDeploymentsForTheCohort(String user) {
        authenticationSteps.authenticateUser(user);

        PayloadBuilder paylaod = new PayloadBuilder();
        paylaod.addField("type", "workspace.deployment.list");
        paylaod.addField("cohortId", interpolate("cohort_id"));

        messageOperations.sendJSON(runner, paylaod.build());
    }

    @Then("^deployment list contains$")
    public void deploymentListContains(Map<String, String> deployments) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<List>(List.class) {
                    @Override
                    public void validate(List payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(deployments.size(), payload.size());

                        Map<String, String> expectedDeploymentIds = Maps.newHashMap();
                        deployments.forEach((key, value) -> expectedDeploymentIds.put(context.getVariable(nameFrom(key, "id")),
                                context.getVariable(nameFrom(value, "id"))));

                        for (Object deployment : payload) {
                            assertTrue(expectedDeploymentIds.containsKey(((Map) deployment).get("deploymentId")));
                            String activityId = expectedDeploymentIds.get(((Map) deployment).get("deploymentId"));
                            assertEquals(activityId, ((Map) deployment).get("activityId"));
                            assertEquals(context.getVariable(interpolate("activity_config")), ((Map) deployment).get("config"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "deployments";
                    }

                    @Override
                    public String getType() {
                        return "workspace.deployment.list.ok";
                    }
                }));
    }

    @Then("^lists of deployments fails with code (\\d+) and message \"([^\"]*)\"$")
    public void listsOfDeploymentsFailsWithCodeAndMessage(int code, String message) {
        messageOperations.validateResponseType(runner, "workspace.deployment.list.error", action ->
                action.jsonPath("$.code", code)
                        .jsonPath("$.message", message));
    }


    @Then("^\"([^\"]*)\" verifies that the plugin versions are resolved to$")
    public void verifyPluginVersion(String user, Map<String, String> deployments) {
        authenticationSteps.authenticateUser(user);
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<List>(List.class) {
                    @Override
                    public void validate(List payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(deployments.size(), payload.size());

                        Map<String, String> expectedDeploymentIds = Maps.newHashMap();
                        deployments.forEach((key, value) -> expectedDeploymentIds.put(context.getVariable(nameFrom(key,
                                                                                                                   "id")),
                                                                                      value));
                        for (Object deployment : payload) {
                            assertTrue(expectedDeploymentIds.containsKey(((Map) deployment).get("activityId")));
                            String expectedPluginVersion = expectedDeploymentIds.get(((Map) deployment).get("activityId"));
                            Map<String, String> plugin = (Map) ((Map) deployment).get("plugin");
                            assertNotNull(plugin);
                            assertEquals(expectedPluginVersion, plugin.get("version"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "deployments";
                    }

                    @Override
                    public String getType() {
                        return "workspace.deployment.list.ok";
                    }
                }));
    }

}
