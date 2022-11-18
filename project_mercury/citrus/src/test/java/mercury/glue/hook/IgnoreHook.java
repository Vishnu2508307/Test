package mercury.glue.hook;

import org.junit.Assume;

import cucumber.api.Scenario;
import cucumber.api.java.Before;

public class IgnoreHook {

    @Before(value = "@ignore", order = HooksOrder.IGNORE_BEFORE)
    public void ignoreScenario(Scenario scenario){
        Assume.assumeTrue("SKIPPING SCENARIO: " + scenario.getName(), false);
    }
}
