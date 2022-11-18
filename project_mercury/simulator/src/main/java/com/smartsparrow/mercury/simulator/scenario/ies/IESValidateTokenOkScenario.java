package com.smartsparrow.mercury.simulator.scenario.ies;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("IESValidateTokenOk")
@RequestMapping(value = "/services/rest/tokens/validatetoken", method = RequestMethod.GET)
public class IESValidateTokenOkScenario extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(builder -> builder.get());

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                    "\"status\": \"success\",\n" +
                                    "\"data\": true\n" +
                                "}"))
                );
    }
}
