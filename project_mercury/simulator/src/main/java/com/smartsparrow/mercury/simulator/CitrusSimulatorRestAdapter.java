package com.smartsparrow.mercury.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.consol.citrus.simulator.http.HttpOperationScenario;
import com.consol.citrus.simulator.http.HttpRequestPathScenarioMapper;
import com.consol.citrus.simulator.http.SimulatorRestAdapter;
import com.consol.citrus.simulator.scenario.mapper.ScenarioMapper;

import io.swagger.models.Model;
import io.swagger.models.Operation;

@Component
public class CitrusSimulatorRestAdapter extends SimulatorRestAdapter {

    @Bean
    @Override
    public ScenarioMapper scenarioMapper() {
        List<HttpOperationScenario> scenariosMapper = new ArrayList<>();
        scenariosMapper.add(getIESValidateTokenOk());
        scenariosMapper.add(getIESIdentityProfileOk());
        scenariosMapper.add(getMyCloudValidateTokenOk());
        scenariosMapper.add(getMyCloudIdentityProfileOk());
        scenariosMapper.add(getPassportEntitlementCheckOk());
        scenariosMapper.add(getIESBatchProfileOk());
        scenariosMapper.add(getOculusDataOk());
        scenariosMapper.add(getMathAssetOk());
        scenariosMapper.add(getPingOk());

        HttpRequestPathScenarioMapper httpRequestPathScenarioMapper = new HttpRequestPathScenarioMapper();
        httpRequestPathScenarioMapper.setHttpScenarios(scenariosMapper);
        return httpRequestPathScenarioMapper;
    }

    private HttpOperationScenario getIESValidateTokenOk() {
        Operation operation = new Operation();
        operation.setOperationId("IESValidateTokenOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/tokens/validatetoken",
                HttpMethod.GET, operation, definitions);
    }

    private HttpOperationScenario getIESIdentityProfileOk() {
        Operation operation = new Operation();
        operation.setOperationId("IESIdentityProfileOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/identityprofiles/**",
                HttpMethod.GET, operation, definitions);
    }

    private HttpOperationScenario getMyCloudValidateTokenOk() {
        Operation operation = new Operation();
        operation.setOperationId("MyCloudValidateTokenOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/auth/json/pearson/sessions/**",
                HttpMethod.POST, operation, definitions);
    }

    private HttpOperationScenario getMyCloudIdentityProfileOk() {
        Operation operation = new Operation();
        operation.setOperationId("MyCloudIdentityProfileOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/auth/json/pearson/users/**",
                HttpMethod.GET, operation, definitions);
    }

    private HttpOperationScenario getPassportEntitlementCheckOk() {
        Operation operation = new Operation();
        operation.setOperationId("PassportEntitlementCheckOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/product-permissions/**",
                                         HttpMethod.GET, operation, definitions);
    }

    private HttpOperationScenario getIESBatchProfileOk() {
        Operation operation = new Operation();
        operation.setOperationId("IESBatchProfileOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/identityprofiles/batch",
                                         HttpMethod.POST, operation, definitions);
    }

    private HttpOperationScenario getOculusDataOk() {
        Operation operation = new Operation();
        operation.setOperationId("OculusDataOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/nextext-api/api/nextext/books/**",
                                         HttpMethod.GET, operation, definitions);
    }

    private HttpOperationScenario getMathAssetOk() {
        Operation operation = new Operation();
        operation.setOperationId("MathAssetOk");
        Map<String, Model> definitions = new HashMap<>();

        return new HttpOperationScenario("/services/rest/demo/plugins/app/showimage",
                                         HttpMethod.POST, operation, definitions);
    }

    private HttpOperationScenario getPingOk() {
        Operation operation = new Operation();
        operation.setOperationId("PingOk");
        Map<String, Model> definitions = new HashMap<>();
        return new HttpOperationScenario("/services/rest/ping",
                HttpMethod.GET, operation, definitions);
    }
}
