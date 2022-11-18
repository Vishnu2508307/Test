package com.smartsparrow.wiring;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.AWSConfig;
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.util.UUIDs;

public class CommonModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(CommonModule.class);

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public AWSConfig getAWSConfig() {

        if (System.getProperty("env") == null || System.getProperty("region") == null) {
            log.warn("Environment and region are not set");
        }

        AWSConfig awsConfig = new AWSConfig()
                .setEnv(System.getProperty("env"))
                .setRegion(System.getProperty("region"));

        log.info("AWS config value {}", awsConfig);

        return awsConfig;
    }

    @Provides
    @Singleton
    public ServerIdentifier getServerIdentifier() {
        ServerIdentifier serverIdentifier = new ServerIdentifier();
        serverIdentifier.setServerId(UUIDs.timeBased().toString());
        return serverIdentifier;
    }
}
