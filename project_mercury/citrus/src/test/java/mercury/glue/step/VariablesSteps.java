package mercury.glue.step;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;

public class VariablesSteps {

    @CitrusResource
    private TestRunner runner;

    @And("^variable \"([^\\s]+)\" is \"([^\"]*)\"$")
    public void variable(String name, String value) {
        runner.variable(name, value);
    }
}
