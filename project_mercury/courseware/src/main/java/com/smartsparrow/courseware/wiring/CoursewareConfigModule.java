package com.smartsparrow.courseware.wiring;

import com.google.inject.Provides;
import com.smartsparrow.cache.config.CacheInfraResponse;
import com.smartsparrow.cache.config.RouteConsumersInfraResponse;
import com.smartsparrow.cohort.wiring.PassportInfraResponse;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.config.wiring.AbstractConfigurationModule;
import com.smartsparrow.eval.wiring.EvaluationFeatureResponse;
import com.smartsparrow.eval.wiring.EvaluationModeFeatureResponse;

public class CoursewareConfigModule extends AbstractConfigurationModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        // config binding
        new CoursewareOperationsBinding(binder)
                .bind();
    }

    @Provides
    CacheInfraResponse provideCacheConfig(ConfigurationService configurationService) {
        return configurationService.load("cache.infra", CacheInfraResponse.class);
    }

    @Provides
    PassportInfraResponse providePassportConfig(ConfigurationService configurationService) {
        return configurationService.load("passport.infra", PassportInfraResponse.class);
    }

    @Provides
    EvaluationFeatureResponse provideEvaluationFeatureConfig(ConfigurationService configurationService) {
        return configurationService.load("evaluation.feature", EvaluationFeatureResponse.class);
    }

    @Provides
    RouteConsumersInfraResponse provideRouteConsumersConfig(ConfigurationService configurationService) {
        return configurationService.load("routeConsumers.infra", RouteConsumersInfraResponse.class);
    }

    @Provides
    CsgInfraResponse provideCsgConfig(ConfigurationService configurationService) {
        return configurationService.load("csg.infra", CsgInfraResponse.class);
    }

    @Provides
    EvaluationModeFeatureResponse provideEvaluationModeFeatureConfig(ConfigurationService configurationService) {
        return configurationService.load("evaluation_mode.feature", EvaluationModeFeatureResponse.class);
    }

}
