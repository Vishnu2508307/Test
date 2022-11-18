package com.smartsparrow.config.wiring;


import com.smartsparrow.data.AbstractModuleDecorator;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractConfigurationModule extends AbstractModuleDecorator {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractConfigurationModule.class);

    protected ConfigurationBindingOperations binder;

    @Override
    public abstract void decorate();

    @SuppressFBWarnings(value = "DM_EXIT", justification = "Shut down the application if the default binding fails")
    @Override
    protected void configure() {
        binder = new ConfigurationBindingOperations(binder());

            // use the decorator to add any additional binding based on the instance type
            this.decorate();
    }
}
