package com.smartsparrow.export.wiring;


import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.smartsparrow.export.route.CoursewareExportProducerRoute;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ExportProducerModule extends AbstractModule {

    private final static Logger log = MercuryLoggerFactory.getLogger(ExportProducerModule.class);

    @Override
    protected void configure() {
        bind(CoursewareExportProducerRoute.class);
    }
}
