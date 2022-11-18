package mercury.glue.step;

import static mercury.glue.step.CohortSteps.DEFAULT_COHORT_NAME;
import static mercury.glue.step.CohortSteps.DEFAULT_COHORT_TYPE;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Sets;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.ResponseMessageValidationCallback;
import mercury.helpers.cohort.CohortListHelper;

public class CohortListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @Then("^cohort list contains the created cohort$")
    public void cohortListContainsTheCreatedCohort() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohorts";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.list.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ArrayList cohorts, Map<String, Object> headers, TestContext context) {
                        assertEquals(1, cohorts.size());

                        Map<String, String> cohort = (Map<String, String>) cohorts.get(0);
                        assertEquals(context.getVariable("cohort_id"), cohort.get("cohortId"));
                        assertEquals(DEFAULT_COHORT_NAME, cohort.get("name"));
                        assertEquals(DEFAULT_COHORT_TYPE, cohort.get("enrollmentType"));
                    }
                }));
    }

    @When("^\"([^\"]*)\" fetches a list of cohorts$")
    public void fetchesAListOfCohorts(String user) throws Throwable {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, CohortListHelper.listCohortRequest());
    }

    @Then("^cohort list contains (\\d+) cohorts$")
    public void cohortListContainsCohorts(int count) {
        Set<Map<String, String>> expectedListOfCohorts = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            Map<String, String> fields = new HashMap<>(2);
            fields.put("name", DEFAULT_COHORT_NAME + "_" + i);
            fields.put("enrollmentType", DEFAULT_COHORT_TYPE);

            expectedListOfCohorts.add(fields);
        }

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohorts";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.list.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ArrayList cohorts, Map<String, Object> headers, TestContext context) {
                        assertEquals(count, cohorts.size());
                        Set<Map<String, String>> actualListOfCohorts = new HashSet<>(cohorts.size());

                        for (Object cohort : cohorts) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("name", (String) ((Map) cohort).get("name"));
                            actual.put("enrollmentType", (String) ((Map) cohort).get("enrollmentType"));
                            actualListOfCohorts.add(actual);
                        }

                        Assert.assertEquals(expectedListOfCohorts, actualListOfCohorts);
                    }
                }));
    }

    @Then("^cohort list contains$")
    public void cohortListContainsCohorts(List<String> expectedCohorts) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohorts";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.list.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ArrayList cohorts, Map<String, Object> headers, TestContext context) {
                        assertEquals(expectedCohorts.size(), cohorts.size());
                        Set<String> actualCohorts = new HashSet<>(cohorts.size());
                        for (Object cohort : cohorts) {
                            actualCohorts.add((String) ((Map) cohort).get("name"));
                        }

                        Assert.assertEquals(Sets.newHashSet(expectedCohorts), actualCohorts);
                    }
                }));
    }
}
