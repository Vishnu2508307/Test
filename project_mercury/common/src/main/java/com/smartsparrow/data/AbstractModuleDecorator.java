package com.smartsparrow.data;

import com.google.inject.AbstractModule;

/**
 * Abstract class that allows to decorate a guice abstract module.
 * It allows to keep common binding behaviour and having the subclass
 * specifying additional required bindings.
 */
public abstract class AbstractModuleDecorator extends AbstractModule {

    /**
     * Use this method to specify what the are the specifying bindings this module should be
     * decorated with
     */
    public abstract void decorate();

}
