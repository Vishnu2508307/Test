package com.smartsparrow.dataevent.wiring;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.component.aws.s3.S3Component;
import org.apache.camel.component.aws.s3.S3Configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.google.inject.Provider;

public class S3ComponentProvider implements Provider<S3Component> {

    @Inject
    private CamelContext camelContext;

    @Inject
    private AmazonS3 s3client;

    @Override
    public S3Component get() {
        S3Configuration s3conf = new S3Configuration();
        s3conf.setAmazonS3Client(s3client);
        s3conf.setUseIAMCredentials(true);

        S3Component s3Component = new S3Component(camelContext);
        s3Component.setConfiguration(s3conf);

        return s3Component;
    }
}
