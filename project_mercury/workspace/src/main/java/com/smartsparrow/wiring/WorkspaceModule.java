package com.smartsparrow.wiring;

import com.google.inject.AbstractModule;
import com.smartsparrow.export.wiring.ExportProducerModule;
import com.smartsparrow.ingestion.wiring.IngestionModule;
import com.smartsparrow.math.wiring.MathModule;
import com.smartsparrow.rtm.wiring.RTMModule;
import com.smartsparrow.wiring.asset.WorkspaceAssetModule;
import com.smartsparrow.wiring.courseware.WorkspaceCoursewareModule;
import com.smartsparrow.wiring.rtm.WorkspaceRTMOperationsModule;

/**
 * This class describes all the modules that are required for
 * a {@link com.smartsparrow.data.InstanceType#WORKSPACE} type of instance
 */
public class WorkspaceModule extends AbstractModule {

    @Override
    protected void configure() {
        // install RTM
        install(new RTMModule());

        // bind RTM apis that are relevant to the workspace
        install(new WorkspaceRTMOperationsModule());

        // install the export
        install(new ExportProducerModule());

        // install ingestion
        install(new IngestionModule());

        // wire asset apis and services that are relevant to the workspace
        install(new WorkspaceAssetModule());

        // wire courseware apis and services that are relevant to the workspace
        install(new WorkspaceCoursewareModule());

        //install math
        install(new MathModule());
    }
}
