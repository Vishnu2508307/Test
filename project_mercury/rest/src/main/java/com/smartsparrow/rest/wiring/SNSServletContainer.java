package com.smartsparrow.rest.wiring;

import javax.inject.Singleton;

import org.glassfish.jersey.servlet.ServletContainer;

/**
 * A singleton servlet container to map specific URLs into by the Guice Module.
 */
@Singleton
public class SNSServletContainer extends ServletContainer {
}
