package mercury.glue.step;

import static graphql.Assert.assertNotNull;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.helpers.learner.LearnerCoursewareHelper;

public class LearnerCoursewareStructureSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Then("{string} fetches learner element structure for {string} {string}")
    public void fetchesLearnerElementStructureFor(final String accountName,
                                                  final String deploymentName,
                                                  final String elementName) throws Throwable {
        // authorize the account
        authenticationSteps.authenticatesViaIes(accountName);

        messageOperations.sendGraphQL(runner,
                                      LearnerCoursewareHelper.getLearnerCourseware(interpolate("cohort_id"), interpolate(nameFrom(deploymentName,
                                                                                                                                  "id")),
                                                                                   interpolate(nameFrom(elementName,
                                                                                                       "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            @SuppressWarnings("unchecked")
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map)payload.get("response");
                assertNotNull(response);
                Map data = (Map) response.get("data");
                Map learn = (Map) data.get("learn");
                Map cohort = (Map) learn.get("cohort");
                List deployments = (List) cohort.get("deployment");
                Map deployment = (Map) deployments.get(0);
                Map activity = (Map) deployment.get("activity");
                assertNotNull(activity.get("id"));
                List defaultFirsts = (List)activity.get("getDefaultFirst");
                Map defaultFirst = (Map) defaultFirsts.get(0);
                assertNotNull(defaultFirst.get("theme"));
                assertNotNull(defaultFirst.get("pluginId"));
                Map assets = (Map) defaultFirst.get("assets");
                assertNotNull(assets);
                assertNotNull(assets.get("edges"));
            }
        }));

    }

    @Then("{string} fetches learner element structure for {string} and interactive {string}")
    public void fetchesLearnerElementStructureForIntercative(final String accountName,
                                                  final String deploymentName,
                                                  final String elementName) throws Throwable {
        // authorize the account
        authenticationSteps.authenticatesViaIes(accountName);

        messageOperations.sendGraphQL(runner,
                                      LearnerCoursewareHelper.getLearnerCoursewareForInteractive(interpolate("cohort_id"), interpolate(nameFrom(deploymentName,
                                                                                                                                  "id")),
                                                                                   interpolate(nameFrom(elementName,
                                                                                                        "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            @SuppressWarnings("unchecked")
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map)payload.get("response");
                assertNotNull(response);
                Map data = (Map) response.get("data");
                Map learn = (Map) data.get("learn");
                Map cohort = (Map) learn.get("cohort");
                List deployments = (List) cohort.get("deployment");
                Map deployment = (Map) deployments.get(0);
                Map activity = (Map) deployment.get("activity");
                assertNotNull(activity.get("id"));
                List defaultFirsts = (List)activity.get("getDefaultFirst");
                Map defaultFirst = (Map) defaultFirsts.get(0);
                assertNotNull(defaultFirst.get("pluginId"));
            }
        }));

    }

    @Then("{string} fetches learner element structure for {string}")
    public void fetchesLearnerElementStructureForWithoutElement(final String accountName,
                                                  final String deploymentName) throws Throwable {
        // authorize the account
        authenticationSteps.authenticatesViaIes(accountName);

        messageOperations.sendGraphQL(runner,
                                      LearnerCoursewareHelper.getLearnerCoursewareWithoutElementId(interpolate("cohort_id"), interpolate(nameFrom(deploymentName,
                                                                                                                                  "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            @SuppressWarnings("unchecked")
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map)payload.get("response");
                assertNotNull(response);
                Map data = (Map) response.get("data");
                Map learn = (Map) data.get("learn");
                Map cohort = (Map) learn.get("cohort");
                List deployments = (List) cohort.get("deployment");
                Map deployment = (Map) deployments.get(0);
                Map activity = (Map) deployment.get("activity");
                assertNotNull(activity.get("id"));
                List defaultFirsts = (List)activity.get("getDefaultFirst");
                Map defaultFirst = (Map) defaultFirsts.get(0);
                assertNotNull(defaultFirst.get("pluginId"));
            }
        }));

    }
}
