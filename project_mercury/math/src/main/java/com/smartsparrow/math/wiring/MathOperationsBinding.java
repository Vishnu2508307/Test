package com.smartsparrow.math.wiring;

import org.slf4j.Logger;

import com.smartsparrow.config.wiring.ConfigurationBindingOperations;
import com.smartsparrow.math.config.MathInfraConfiguration;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class MathOperationsBinding {

    private final static Logger log = MercuryLoggerFactory.getLogger(MathOperationsBinding.class);
    private final ConfigurationBindingOperations binder;

    public MathOperationsBinding(ConfigurationBindingOperations configurationBindingOperations) {
        this.binder = configurationBindingOperations;
    }

    public void bind() {
        binder.bind("math.infra")
                .toConfigType(MathInfraConfiguration.class);
    }
}
