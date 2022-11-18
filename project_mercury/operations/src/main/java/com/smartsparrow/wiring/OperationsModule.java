package com.smartsparrow.wiring;

import com.google.inject.AbstractModule;
import com.smartsparrow.cache.wiring.RedissonModule;
import com.smartsparrow.config.wiring.ConfigurationManagementModule;
import com.smartsparrow.courseware.wiring.CoursewareModule;
import com.smartsparrow.dataevent.wiring.DataEventModule;
import com.smartsparrow.dse.wiring.CassandraModule;
import com.smartsparrow.export.wiring.ExportConsumerModule;


/**
 * This class describes all the modules that are required for
 * a {@link com.smartsparrow.data.InstanceType#WORKSPACE} type of instance
 */
public class OperationsModule extends AbstractModule {

    @Override
    protected void configure() {

        install(new RedissonModule());
        install(new ConfigurationManagementModule());
        install(new CassandraModule());
        install(new DataEventModule());
        install(new CoursewareModule());
        install(new AssetsModule());

        // install the export consumer
        install(new ExportConsumerModule());
    }
}
