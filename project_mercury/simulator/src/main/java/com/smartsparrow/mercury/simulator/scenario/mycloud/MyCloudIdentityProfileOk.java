package com.smartsparrow.mercury.simulator.scenario.mycloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.dsl.builder.HttpServerActionBuilder;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("MyCloudIdentityProfileOk")
@RequestMapping(value = "/services/rest/auth/json/pearson/users/**", method = RequestMethod.GET)
public class MyCloudIdentityProfileOk extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(HttpServerActionBuilder.HttpServerReceiveActionBuilder::get);

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                         "  \"_id\": \"USMITJ1\",\n" +
                                "  \"mail\": [\n" +

                                         "    \"support@citrus.dev\"\n" +

                                "  ],\n" +

                                "  \"givenName\": [\n" +
                                "    \"John\"\n" +
                                "  ],\n" +
                                "  \"sn\": [\n" +
                                "    \"Smith\"\n" +
                                "  ]\n" +
                                "}"))
                );
    }
}
