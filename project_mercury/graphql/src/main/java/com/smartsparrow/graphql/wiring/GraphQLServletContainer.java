package com.smartsparrow.graphql.wiring;

import javax.inject.Singleton;

import org.glassfish.jersey.servlet.ServletContainer;

/**
 * A singleton servlet container to map specific URLs for GRAPHQL into by the Guice Module.
 */
@Singleton
public class GraphQLServletContainer extends ServletContainer {
}
