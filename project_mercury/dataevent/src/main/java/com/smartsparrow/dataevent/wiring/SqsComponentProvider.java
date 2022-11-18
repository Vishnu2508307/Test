package com.smartsparrow.dataevent.wiring;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.component.aws.sqs.SqsComponent;
import org.apache.camel.component.aws.sqs.SqsConfiguration;

import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.inject.Provider;

public class SqsComponentProvider implements Provider<SqsComponent> {

    @Inject
    private CamelContext camelContext;

    @Override
    public SqsComponent get() {
        SqsConfiguration conf = new SqsConfiguration();
        conf.setAmazonSQSClient(AmazonSQSClientBuilder.defaultClient());

        SqsComponent component = new SqsComponent(camelContext);
        component.setConfiguration(conf);

        return component;
    }
}
