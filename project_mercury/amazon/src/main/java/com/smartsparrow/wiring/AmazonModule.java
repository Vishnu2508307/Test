package com.smartsparrow.wiring;

import static com.amazonaws.auth.profile.internal.AwsProfileNameLoader.AWS_PROFILE_ENVIRONMENT_VARIABLE;
import static com.amazonaws.auth.profile.internal.AwsProfileNameLoader.AWS_PROFILE_SYSTEM_PROPERTY;

import javax.inject.Singleton;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class AmazonModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public AmazonS3 getS3Client(@Named("env.region") String region) {
        //define AWS profile name
        if (System.getenv(AWS_PROFILE_ENVIRONMENT_VARIABLE) == null && System.getProperty(AWS_PROFILE_SYSTEM_PROPERTY) == null) {
            System.setProperty(AWS_PROFILE_SYSTEM_PROPERTY, region);
        }
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }
}
