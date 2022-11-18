package com.smartsparrow.wiring.rtm;

import com.smartsparrow.rtm.wiring.AbstractRTMOperationsModule;

public class WorkspaceRTMOperationsModule extends AbstractRTMOperationsModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        new WorkspaceRTMOperationsBinding(binder)
                .bind();
    }
}
