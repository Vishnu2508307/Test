package com.smartsparrow.mercury.wiring;

import com.smartsparrow.rtm.wiring.AbstractRTMOperationsModule;
import com.smartsparrow.wiring.rtm.LearnspaceRTMOperationsBinding;
import com.smartsparrow.wiring.rtm.WorkspaceRTMOperationsBinding;

/**
 * Binds all RTM message apis. Relevant for instances of type {@link com.smartsparrow.data.InstanceType#DEFAULT}
 */
public class DefaultRTMOperationsModule extends AbstractRTMOperationsModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        // bind workspace RTM
        new WorkspaceRTMOperationsBinding(binder)
                .bind();
        // bind learnspace RTM
        new LearnspaceRTMOperationsBinding(binder)
                .bind();
    }

}
