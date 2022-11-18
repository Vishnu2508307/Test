package com.smartsparrow.mercury.simulator.scenario.ies;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("IESBatchProfileOk")
@RequestMapping(value = "/services/rest/identityprofiles/batch", method = RequestMethod.POST)
public class IESBatchProfileOk extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive((builder -> builder.post()));

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                         "    \"status\": \"success\",\n" +
                                         "    \"data\": {\n" +
                                         "        \"users\": [\n" +
                                         "            {\n" +
                                         "                \"piId\": \"ffffffff606ba1de726ea625b51e3c3b\",\n" +
                                         "                \"profileData\": {\n" +
                                         "                    \"givenName\": \"Alice\",\n" +
                                         "                    \"familyName\": \"De\",\n" +
                                         "                    \"emails\": [\n" +
                                         "                        {\n" +
                                         "                            \"id\": \"c0c98f324681401cbd57a7a709dc29b4\",\n" +
                                         "                            \"emailAddress\": \"Alice.De+8@pearson.com\",\n" +
                                         "                            \"isPrimary\": \"true\",\n" +
                                         "                            \"isValidated\": \"N\"\n" +
                                         "                        }\n" +
                                         "                    ]\n" +
                                         "                }\n" +
                                         "            }\n" +
                                         "        ],\n" +
                                         "        \"notFound\": []\n" +
                                         "    }\n" +
                                         "}"))
                );
    }
}
