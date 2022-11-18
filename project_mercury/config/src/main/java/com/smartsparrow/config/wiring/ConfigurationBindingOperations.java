package com.smartsparrow.config.wiring;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.config.data.Configuration;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ConfigurationBindingOperations {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ConfigurationBindingOperations.class);

    private MapBinder<String, Configuration> boundConfiguration;

    public ConfigurationBindingOperations(Binder binder) {
        boundConfiguration = MapBinder.newMapBinder(binder,  //
                                                    new TypeLiteral<String>() {
                                                    }, //
                                                    new TypeLiteral<Configuration>() {
                                                    });

    }

    public MapBinder<String, Configuration> getBoundConfiguration() {
        return boundConfiguration;
    }

    public BinderBuilder bind(String type) {
        return new BinderBuilder(type);
    }

    public class BinderBuilder {
        private final String type;
        private Class<? extends Configuration> typeClass;

        /**
         * Create a builder for the named type
         *
         * @param type the type value
         */
        private BinderBuilder(String type) {
            this.type = type;
        }

        /**
         * Bind the configuration type
         *
         * @param typeClass the POJO to represent the configuration response type
         * @return this
         */
        public final BinderBuilder toConfigType(final Class<? extends Configuration> typeClass) {
            // bind the configuration response type
            boundConfiguration.addBinding(type).to(typeClass);
            this.typeClass = typeClass;
            log.info("bind type {} as {}", type, this.typeClass.getName());
            return this;
        }
    }
}
