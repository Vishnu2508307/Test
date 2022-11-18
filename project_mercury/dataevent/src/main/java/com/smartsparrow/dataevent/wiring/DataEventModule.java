package com.smartsparrow.dataevent.wiring;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.aws.s3.S3Component;
import org.apache.camel.component.aws.sns.SnsComponent;
import org.apache.camel.component.aws.sqs.SqsComponent;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DataEventModule extends CamelModuleWithMatchingRoutes {

    private final static Logger log = LoggerFactory.getLogger(DataEventModule.class);

    @Override
    protected void configure() {
        super.configure();

        // Install configured components
        bind(S3Component.class).toProvider(S3ComponentProvider.class);
        bind(SnsComponent.class).toProvider(SnsComponentProvider.class);
        bind(SqsComponent.class).toProvider(SqsComponentProvider.class);
    }

    /**
     * Provider necessary so Guice knows how to wire camel @{@link org.apache.camel.Produce} annotation; eg:
     *
     * @Produce
     * private ProducerTemplate producer;
     *
     * or
     *
     * @Produce (uri = 'direct:some.route?params)
     * private ProducerTemplate producer;
     *
     * Fields with that @Produce annotation don't require to be part of the constructor injector.
     *
     * @see ProducerTemplate
     *
     */
    @Provides
    @Singleton
    public ProducerTemplate getProducerTemplate(CamelContext context) {
        return context.createProducerTemplate();
    }

    /**
     * Service that implements the "reactive-streams" component.
     *
     * @param camelContext
     * @return
     */
    @Provides
    @Singleton
    CamelReactiveStreamsService getReactiveStreamsService(CamelContext camelContext) {
        return CamelReactiveStreams.get(camelContext);
    }

}
