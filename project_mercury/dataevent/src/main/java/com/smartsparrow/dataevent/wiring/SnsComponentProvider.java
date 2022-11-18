package com.smartsparrow.dataevent.wiring;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.component.aws.sns.SnsComponent;
import org.apache.camel.component.aws.sns.SnsConfiguration;

import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.google.inject.Provider;

public class SnsComponentProvider implements Provider<SnsComponent> {

    @Inject
    private CamelContext camelContext;

    @Override
    public SnsComponent get() {
        SnsConfiguration conf = new SnsConfiguration();
        conf.setAmazonSNSClient(AmazonSNSClientBuilder.defaultClient());

        SnsComponent component = new SnsComponent(camelContext);
        component.setConfiguration(conf);

        return component;
    }
}
