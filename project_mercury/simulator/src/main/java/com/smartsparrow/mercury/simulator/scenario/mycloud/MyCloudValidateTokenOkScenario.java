package com.smartsparrow.mercury.simulator.scenario.mycloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("MyCloudValidateTokenOk")
@RequestMapping(value = "/services/rest/auth/json/pearson/sessions/**", method = RequestMethod.POST)
public class MyCloudValidateTokenOkScenario extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(builder -> builder.post());

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                "\"valid\": true,\n" +
                                "\"uid\": \"usmitj1\"\n" +
                                "}"))
                );
    }
}
