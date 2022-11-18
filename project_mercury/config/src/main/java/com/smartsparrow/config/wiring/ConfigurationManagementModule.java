package com.smartsparrow.config.wiring;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.smartsparrow.config.BootstrapConfiguration;
import com.smartsparrow.config.CassandraConfig;
import com.smartsparrow.config.RedisConfig;
import com.smartsparrow.config.data.ConfigurationConstants;
import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.ConfigurationLoadCache;
import com.smartsparrow.config.data.ConfigurationLoadStrategy;
import com.smartsparrow.config.data.ConfigurationMaterializer;
import com.smartsparrow.config.data.ConfigurationType;
import com.smartsparrow.config.service.BaseConfigurationService;
import com.smartsparrow.config.service.ConfigurationCache;
import com.smartsparrow.config.service.DynamicConfigurationLoadCache;
import com.smartsparrow.config.service.LocalConfigurationLoadStrategy;
import com.smartsparrow.config.service.S3ConfigurationLoadStrategy;
import com.smartsparrow.config.service.StaticConfigurationLoadCache;
import com.smartsparrow.dse.api.CassandraCluster;
import com.smartsparrow.dse.api.DSEException;
import com.smartsparrow.dse.api.PreparedStatementCache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Uses static configuration to connect to configuration source (Cassandra DB),
 * load Bootstrap Configuration and provides Bootstrap Configuration as Singleton.
 * This module needs the following properties:
 * <table border=1>
 * <tr>
 * <td>Property Key</td><td>Default Value</td>
 * </tr>
 * <tr>
 * <td>cassandra.contactPoints</td><td>192.168.192.201</td>
 * </tr>
 * <tr>
 * <td>cassandra.authentication.username</td><td>cassandra</td>
 * </tr>
 * <tr>
 * <td>cassandra.authentication.password</td><td>cassandra</td>
 * </tr>
 * <tr>
 * <td>cassandra.keystore</td><td>System.getProperty("user.dir") + "/etc/tls/dse/local/keystore.jks"</td>
 * </tr>
 * <tr>
 * <td>cassandra.keystore.password</td><td>cassandra</td>
 * </tr>
 * <tr>
 * <td>env.region</td><td>LOCAL</td>
 * </tr>
 * </table>
 *
 * If properties are not passed using "-D", the default values are used.
 * Failure during the fetching of configuration from datasource is fatal and should halt application startup.
 */
public class ConfigurationManagementModule extends AbstractModule {

    private static Logger log = LoggerFactory.getLogger(ConfigurationManagementModule.class);

    private static final String CONTACT_POINTS = "192.168.192.201";
    private static final String USERNAME = "cassandra";
    private static final String PASSWORD = "cassandra";
    private static final String KEY_STORE = System.getProperty("user.dir") + "/etc/tls/dse/local/keystore.jks";
    private static final String KEY_STORE_PASSWORD = "cassandra";
    private static final String ENV_REGION = "LOCAL";
    private static final String REDIS_URL = "redis://redis.local.smartsparrow.com:6379";
    private static final String REDIS_PASSWORD = "aelpredis";

    // Cached config entries TTL
    private static final int CONFIG_CACHE_TTL = 1;
    private static final TimeUnit CONFIG_CACHE_TIME_UNIT = TimeUnit.MINUTES;
    protected MapBinder<String, ConfigurationLoadStrategy> loadStrategyMapBinder;
    protected MapBinder<ConfigurationType, ConfigurationLoadCache> typeConfigCacheMapBinder;

    @Override
    protected void configure() {
        String region = System.getProperty("env.region", ENV_REGION);
        bindConstant().annotatedWith(Names.named("env.region")).to(region);
        log.info("bind 'env.region' property to value '{}'", region);

        loadStrategyMapBinder = MapBinder.newMapBinder(binder(),//
                                            new TypeLiteral<String>(){
                                            }, //
                                            new TypeLiteral<ConfigurationLoadStrategy>(){
                                            }); //

        typeConfigCacheMapBinder = MapBinder.newMapBinder(binder(),//
                                                       new TypeLiteral<ConfigurationType>(){
                                                       }, //
                                                       new TypeLiteral<ConfigurationLoadCache>(){
                                                       }); //

        BootstrapConfiguration bootstrapConfiguration = provideBootstrapConfiguration(region);
        bind(BootstrapConfiguration.class).toInstance(bootstrapConfiguration);

        //bind env with configuration loading strategy
        loadStrategyMapBinder.addBinding("dev").to(S3ConfigurationLoadStrategy.class);
        loadStrategyMapBinder.addBinding("local").to(LocalConfigurationLoadStrategy.class);
        loadStrategyMapBinder.addBinding("ppe").to(S3ConfigurationLoadStrategy.class);
        loadStrategyMapBinder.addBinding("prod").to(S3ConfigurationLoadStrategy.class);
        loadStrategyMapBinder.addBinding("qaint").to(S3ConfigurationLoadStrategy.class);
        loadStrategyMapBinder.addBinding("stg").to(S3ConfigurationLoadStrategy.class);
        loadStrategyMapBinder.addBinding("sandbox").to(LocalConfigurationLoadStrategy.class);

        // bind configuration type with cache loader
        typeConfigCacheMapBinder.addBinding(ConfigurationType.DYNAMIC).to(DynamicConfigurationLoadCache.class);
        typeConfigCacheMapBinder.addBinding(ConfigurationType.STATIC).to(StaticConfigurationLoadCache.class);
    }

    private BootstrapConfiguration provideBootstrapConfiguration(String region) {
        //initialise cassandra session to load Bootstrap configuration
        CassandraCluster tempCluster = createCluster();
        Session tempSession = createSession(tempCluster);
        //load Bootstrap configuration
        BaseConfigurationService service = createBootstrapConfigService(tempSession, region);
        CassandraConfig cassandraConfig = service.getRenderedConfig(CassandraConfig.class, "cassandra");

        //use default values to connect DB if there is no settings defined in config.env table
        if (cassandraConfig == null) {
            cassandraConfig = new CassandraConfig()
                    .setContactPoints(Lists.newArrayList(CONTACT_POINTS))
                    .setUsername(USERNAME)
                    .setPassword(PASSWORD)
                    .setKeystore(KEY_STORE)
                    .setKeystorePassword(KEY_STORE_PASSWORD);
            log.info("No Cassandra configuration was found in DB. Using default settings {}", cassandraConfig);
        }

        RedisConfig redisConfig = service.getRenderedConfig(RedisConfig.class, "redis");
        if (redisConfig == null) {
            redisConfig = new RedisConfig().setAddress(REDIS_URL).setPassword(REDIS_PASSWORD);
            log.info("No Redis configuration was found in DB. Using default settings {}", redisConfig);
        }

        //close Cassandra session
        tempSession.close();
        //shutdown cluster
        tempCluster.close();

        return new BootstrapConfiguration(cassandraConfig, redisConfig);
    }

    /**
     * Creates instance of BaseConfigurationService needed to load Cassandra configuration. This instance uses a temporary
     * cassandra session. Also there is no need to cache prepared statements.
     *
     * @param session the temporary session
     * @param region  the region
     * @return returns temporary instance of BaseConfigurationService
     */
    private BaseConfigurationService createBootstrapConfigService(Session session, String region) {
        PreparedStatementCache preparedStatementCache = new PreparedStatementCache(session) {
            @Override
            public PreparedStatement resolve(String query) {
                return session.prepare(query);
            }
        };
        ConfigurationGateway gateway =
                new ConfigurationGateway(session, new ConfigurationMaterializer(preparedStatementCache), null);

        return new BaseConfigurationService(gateway, region);
    }

    /**
     * Creates temporary cassandra cluster and opens session using connection properties from static configuration.
     * This session is used only to load Cassandra properties from DB.
     *
     * @return - the opened cassandra session
     */
    @SuppressFBWarnings(value = "DM_EXIT")
    private Session createSession(CassandraCluster cluster) {
        //
        Session session = null;
        try {
            session = cluster.openSession();
        } catch (DSEException e) {
            log.error("Bootstrap configuration can not be loaded: error connecting to cluster, exiting.", e);
            System.exit(-1);
        }
        return session;
    }

    private CassandraCluster createCluster() {
        //get Static configuration to load Dynamic Configuration
        String contactPoints = System.getProperty("cassandra.contactPoints", CONTACT_POINTS);
        String username = System.getProperty("cassandra.authentication.username", USERNAME);
        String password = System.getProperty("cassandra.authentication.password", PASSWORD);
        String keyStore = System.getProperty("cassandra.keystore", KEY_STORE);
        String keyStorePassword = System.getProperty("cassandra.keystore.password", KEY_STORE_PASSWORD);

        // split the contact points.
        List<String> _addresses = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(contactPoints);

        return new CassandraCluster(_addresses, username, password, keyStore, keyStorePassword);
    }

    /**
     * Provides singleton instance of in memory local Guava cache to store long lived configuration settings.
     */
    @Provides
    @Singleton
    ConfigurationCache provideConfigurationCache() {

        Cache<String, Object> guavaCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(CONFIG_CACHE_TTL, CONFIG_CACHE_TIME_UNIT)
                .removalListener(notification -> {
                    log.info("Config cache evicted key {} due to reason: {}", notification.getKey(), notification.getCause().name());
                })
                .build();

        return new ConfigurationCache(guavaCache);
    }

    /**
     * Provides singleton instance of in memory local Guava cache to store dynamic configuration.
     * Dynamic config cache expire based on ttl value
     */
    @Provides
    @Singleton
    DynamicConfigurationLoadCache provideDynamicConfigurationCache() {

        Cache<String, Object> guavaCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(ConfigurationConstants.DYNAMIC_CONFIG_CACHE_TTL, ConfigurationConstants.DYNAMIC_CONFIG_CACHE_UNIT)
                .removalListener(notification -> {
                    log.info("Dynamic config cache evicted key {} due to reason: {}", notification.getKey(), notification.getCause().name());
                })
                .build();

        return new DynamicConfigurationLoadCache(guavaCache);
    }

    /**
     * Provides singleton instance of in memory local Guava cache to store static configuration.
     */
    @Provides
    @Singleton
    StaticConfigurationLoadCache provideStaticConfigurationCache() {

        Cache<String, Object> guavaCache = CacheBuilder
                .newBuilder()
                .removalListener(notification -> {
                    log.info("Static config cache evicted key {} due to reason: {}", notification.getKey(), notification.getCause().name());
                })
                .build();

        return new StaticConfigurationLoadCache(guavaCache);
    }

}
