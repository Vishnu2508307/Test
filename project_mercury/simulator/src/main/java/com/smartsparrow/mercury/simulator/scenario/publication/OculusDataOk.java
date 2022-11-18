package com.smartsparrow.mercury.simulator.scenario.publication;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.dsl.builder.HttpServerActionBuilder;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("OculusDataOk")
@RequestMapping(value = "/services/rest/nextext-api/api/nextext/books/**", method = RequestMethod.GET)
public class OculusDataOk extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(HttpServerActionBuilder.HttpServerReceiveActionBuilder::get);

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\"bookId\":\"BRNT-BW05TO15MZU-REV\",\"metadata\":{\"title\":\"Social Studies in Elementary Education\"," +
                                         "\"isbn\":\"NotAvailable\",\"edition\":\"v2\",\"status\":\"PUBLISHED\"}}"))
                );
    }
}
