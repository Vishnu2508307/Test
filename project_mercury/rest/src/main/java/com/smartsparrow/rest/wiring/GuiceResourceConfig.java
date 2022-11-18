package com.smartsparrow.rest.wiring;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import com.google.inject.Injector;
import com.smartsparrow.exception.RestExceptionMapper;
import com.smartsparrow.exception.WebApplicationExceptionMapper;

/**
 * Configures a bridge between Guice and HK2. HK2 is Jersey's own dependency injection framework. The example of this code
 * is taken from {@see https://github.com/caberger/jerseyguice}
 */
public class GuiceResourceConfig extends ResourceConfig {
    public GuiceResourceConfig() {
        ContainerLifecycleListener containerLifecycleListener = new ContainerLifecycleListener() {
            public void onStartup(Container container) {
                ServletContainer servletContainer = (ServletContainer) container;
                InjectionManager im = container.getApplicationHandler().getInjectionManager();
                ServiceLocator serviceLocator = im.getInstance(ServiceLocator.class);
                GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
                GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
                Injector injector = (Injector) servletContainer.getServletContext()
                        .getAttribute(Injector.class.getName());
                guiceBridge.bridgeGuiceInjector(injector);
            }

            public void onReload(Container container) {
            }

            public void onShutdown(Container container) {
            }
        };

        register(containerLifecycleListener);
        register(MultiPartFeature.class);
        register(WebApplicationExceptionMapper.class);
        register(RestExceptionMapper.class);
    }
}
