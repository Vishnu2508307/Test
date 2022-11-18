package com.smartsparrow.mercury.simulator.scenario.ies;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.dsl.builder.HttpServerActionBuilder;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("IESIdentityProfileOk")
@RequestMapping(value = "/services/rest/identityprofiles/**", method = RequestMethod.GET)
public class IESIdentityProfileOk extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(HttpServerActionBuilder.HttpServerReceiveActionBuilder::get);

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                "  \"status\" : \"success\",\n" +
                                "  \"data\" : {\n" +
                                "    \"identity\":{\n" +
                                "      \"id\" : \"1234567890\"\n" +
                                "    },\n" +
                                "    \"givenName\":\"Homer\",\n" +
                                "    \"middleName\":\"\",\n" +
                                "    \"familyName\":\"Simpson\",\n" +
                                "    \"emails\": [\n" +
                                "      {\n" +
                                "        \"id\": \"0987654321\",\n" +
                                "        \"emailAddress\": \"citrus@simulator.tld\",\n" +
                                "        \"isPrimary\": \"true\",\n" +
                                "        \"isValidated\": \"Y\"\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  }\n" +
                                "}"))
                );
    }
}
