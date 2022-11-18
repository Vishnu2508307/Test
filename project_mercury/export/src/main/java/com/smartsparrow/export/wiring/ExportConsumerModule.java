package com.smartsparrow.export.wiring;


import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.smartsparrow.export.route.CoursewareExportConsumerRoute;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ExportConsumerModule extends AbstractModule {

    private final static Logger log = MercuryLoggerFactory.getLogger(ExportConsumerModule.class);

    @Override
    protected void configure() {
        bind(CoursewareExportConsumerRoute.class);
    }
}
