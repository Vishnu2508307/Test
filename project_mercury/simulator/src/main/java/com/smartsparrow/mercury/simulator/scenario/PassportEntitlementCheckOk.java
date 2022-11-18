package com.smartsparrow.mercury.simulator.scenario;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.dsl.builder.HttpServerActionBuilder;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("PassportEntitlementCheckOk")
@RequestMapping(value = "/services/rest/product-permissions/**", method = RequestMethod.GET)
public class PassportEntitlementCheckOk extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(HttpServerActionBuilder.HttpServerReceiveActionBuilder::get);

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                 "\"access\": \"true\",\n" +
                                 "}"))
                );
    }
}
