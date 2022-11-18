package com.smartsparrow.rtm.wiring;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Guice module to provide the RTMScope
 */
public class RTMScopeModule extends AbstractModule {

    public void configure() {
        RTMScope rtmScope = new RTMScope();

        // tell Guice about the scope
        bindScope(RTMScoped.class, rtmScope);

        // bind to injected instances
        bind(RTMScope.class) //
                .toInstance(rtmScope);

        // also make our scope instance injectable by name
        bind(RTMScope.class) //
                .annotatedWith(Names.named("rtmScope")) //
                .toInstance(rtmScope);
    }

}
