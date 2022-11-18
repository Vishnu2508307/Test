package com.smartsparrow.config.data;

import javax.inject.Inject;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class ConfigurationGateway {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationGateway.class);

    private final Session session;
    private final ConfigurationMaterializer configurationMaterializer;
    private final ConfigurationMutator configurationMutator;

    @Inject
    public ConfigurationGateway(Session session,
                                ConfigurationMaterializer configurationMaterializer,
                                ConfigurationMutator configurationMutator) {
        this.session = session;
        this.configurationMaterializer = configurationMaterializer;
        this.configurationMutator = configurationMutator;
    }

    public Flux<EnvConfiguration> fetchByRegion(String region) {
        return ResultSets.query(session, configurationMaterializer.fetchByRegion(region))
                .flatMapIterable(map -> map)
                .map(this::mapRowToEnvConfiguration)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching configurations for region %s", region),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    public Flux<EnvConfiguration> fetchByKeyAndRegion(String key, String region) {
        return ResultSets.query(session, configurationMaterializer.fetchByKeyAndRegion(key, region))
                .flatMapIterable(map -> map)
                .map(this::mapRowToEnvConfiguration)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching configurations by key %s and region %s",
                            key, region.toString()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    private EnvConfiguration mapRowToEnvConfiguration(Row row) {
        return new EnvConfiguration()
                .setRegion(row.getString("env_region"))
                .setKey(row.getString("key"))
                .setValue(row.getString("value"));
    }

    public void persist(EnvConfiguration configuration) {
        Mutators.executeBlocking(session, configurationMutator.upsert(configuration));
    }

}
