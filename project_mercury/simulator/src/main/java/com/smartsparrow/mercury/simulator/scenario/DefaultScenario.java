
package com.smartsparrow.mercury.simulator.scenario;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;
import org.springframework.http.HttpStatus;

@Scenario("Default")
public class DefaultScenario extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario
                .http()
                .receive((builder -> builder.post()));

        scenario
                .http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("<DefaultResponse>This is a default response!</DefaultResponse>"))
                );
    }
}
