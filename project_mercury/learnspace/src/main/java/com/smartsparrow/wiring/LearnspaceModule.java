package com.smartsparrow.wiring;

import com.google.inject.AbstractModule;
import com.smartsparrow.rtm.wiring.RTMModule;
import com.smartsparrow.wiring.asset.LearnspaceAssetModule;
import com.smartsparrow.wiring.courseware.LearnspaceCoursewareModule;
import com.smartsparrow.wiring.rtm.LearnspaceRTMOperationsModule;
import com.smartsparrow.math.wiring.MathModule;

/**
 * This class describes the required modules for a
 * {@link com.smartsparrow.data.InstanceType#LEARNER} type of instance
 */
public class LearnspaceModule extends AbstractModule {

    @Override
    protected void configure() {
        // install RTM
        install(new RTMModule());

        // bind RTM apis that are relevant to the learnspace
        install(new LearnspaceRTMOperationsModule());

        // install the asset apis and services required by the learnspace
        install(new LearnspaceAssetModule());

        // wire courseware apis and services that are relevant to the learnspace
        install(new LearnspaceCoursewareModule());

        //install math
        install(new MathModule());
    }
}
