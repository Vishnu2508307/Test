package com.smartsparrow.mercury.wiring;

import com.google.inject.AbstractModule;
import com.smartsparrow.courseware.wiring.CoursewareModule;
import com.smartsparrow.export.wiring.ExportConsumerModule;
import com.smartsparrow.export.wiring.ExportProducerModule;
import com.smartsparrow.ingestion.wiring.IngestionModule;
import com.smartsparrow.math.wiring.MathModule;
import com.smartsparrow.rtm.wiring.RTMModule;
import com.smartsparrow.user_content.wiring.UserContentModule;
import com.smartsparrow.wiring.AssetsModule;

/**
 * This class describes the modules that are required for
 * a {@link com.smartsparrow.data.InstanceType#DEFAULT} type of instance
 */
public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        // install RTM
        install(new RTMModule());

        // bind all the available RTM apis
        install(new DefaultRTMOperationsModule());

        // install export producer
        install(new ExportProducerModule());

        // TODO: the export consumer module is not loaded to support LEGACY Servers when loading default modules
        //  this code change needs to be rolled back when the legacy servers are shutdown, can be enabled by passing the below system property
        // to enabled this pass vm arg: -Ddefault.export.consume.module=enabled
        if ("enabled".equals(System.getProperty("default.export.consume.module"))) {
            // install export consumer
            install(new ExportConsumerModule());
        }

        // install ingestion
        install(new IngestionModule());

        // install assets
        install(new AssetsModule());

        // wire all courseware apis and services
        install(new CoursewareModule());

        // install math
        install(new MathModule());

        //install user content module
        install(new UserContentModule());

        // TODO: install rest module that binds all the available REST apis
        // TODO: install graphql module that binds the complete graphql schema
    }
}
