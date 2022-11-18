package com.smartsparrow.wiring.rtm;

import com.smartsparrow.rtm.wiring.AbstractRTMOperationsModule;

public class LearnspaceRTMOperationsModule extends AbstractRTMOperationsModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        new LearnspaceRTMOperationsBinding(binder)
                .bind();
    }
}
