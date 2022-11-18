package com.smartsparrow.mercury.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.EnumSet;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.DispatcherType;
import javax.ws.rs.HttpMethod;

import org.apache.camel.guice.jndi.GuiceInitialContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.smartsparrow.data.InstanceType;
import com.smartsparrow.util.Enums;

/**
 * This class uses the builder pattern to build a {@link JettyServer}.*
 */
public class JettyServerBuilder {

    private static final Logger log = LoggerFactory.getLogger(JettyServerBuilder.class);

    private static final int DEFAULT_PORT = 8080;
    private static final long DEFAULT_SHUTDOWN_TIMEOUT = 30000;
    private static final long DEFAULT_STARTUP_TIMEOUT = 120000;
    private static final int DEFAULT_MIN_GZIP_SIZE = 2048;

    private int port = DEFAULT_PORT;
    private long _shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
    private long _startupTimeout = DEFAULT_STARTUP_TIMEOUT;
    private int _minGzipSize = DEFAULT_MIN_GZIP_SIZE;
    private Module[] guiceModules;
    private final InstanceType instanceType;

    private Boolean allowPermissiveCors = false;
    private final HandlerList handlerList = new HandlerList();

    public JettyServerBuilder() {
        this.instanceType = InstanceType.DEFAULT;
    }

    public JettyServerBuilder(String type) {
        this.instanceType = getInstanceType(type);
    }

    /**
     * Enables creation of a Guice Injector.
     *
     * @param modules a varArgs of Guice modules to use for the injector creation.
     * @return {@link JettyServerBuilder}
     * @throws JettyServerBuilderException when the injector creation fails
     */
    public JettyServerBuilder withModules(Module... modules) {
        this.guiceModules = modules;
        return this;
    }

    /**
     * Returns status of the AllowPermissiveCors flag
     *
     * @return boolean with current status of the flag
     */
    public Boolean getAllowPermissiveCors() {
        return allowPermissiveCors;
    }

    /**
     * Bootstraps the server with a CORS filter configured with Access-Control-Allow-Origin: * and
     * Access-Control-Allow-Headers: Authorization,X-Requested-With,Content-Type,Accept,Cache-Control,Origin.
     *
     * Enables quicker development of the rest endpoints clients
     *
     * @param allow boolean flag to allow or not any Origin cors
     * @return {@link JettyServerBuilder}
     */
    public JettyServerBuilder withPermissiveCors(boolean allow) {
        this.allowPermissiveCors = allow;
        return this;
    }

    /**
     * A setter method that allows setting the jetty server port.
     * The default value given to the port when not set is hold by the DEFAULT_PORT static constant
     * declared at the top of this class.
     *
     * @param port a number representing the port value.
     * @return {@link JettyServerBuilder}
     */
    public JettyServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * A setter method that allows setting the shutdown timeout.
     * Sets the time the server will wait before all the socket connections are gracefully closed.
     *
     * @param _shutdownTimeout the shutdown timeout, the value should always be in milliseconds.
     * @return {@link JettyServerBuilder}
     */
    public JettyServerBuilder setShutdownTimeout(long _shutdownTimeout) {
        this._shutdownTimeout = _shutdownTimeout;
        return this;
    }

    /**
     * A setter method that allows setting the startup timeout.
     * Sets the time the {@link JettyServer} awaitServerStartup method will wait for
     *
     * @param _startupTimeout the startup timeout, the value should be in milliseconds.
     * @return {@link JettyServerBuilder}
     */
    public JettyServerBuilder setStartupTimeout(long _startupTimeout) {
        this._startupTimeout = _startupTimeout;
        return this;
    }

    /**
     * Allows to set the minimum response size to trigger dynamic compression. The default value
     * {@link JettyServerBuilder#DEFAULT_MIN_GZIP_SIZE} will be used when a value is not provided
     *
     * @param _minGzipSize the desired size
     * @return {@link JettyServerBuilder}
     */
    public JettyServerBuilder setMinGzipSize(int _minGzipSize) {
        this._minGzipSize = _minGzipSize;
        return this;
    }

    /**
     * Gets the {@link InstanceType} for this running instance based on the program arguments.
     *
     * @param type the instance type
     * @return {@link InstanceType#DEFAULT} when no program argument specified or the desired instance type when found
     * @throws JettyServerBuilderException when the provided instance type is invalid or unkown
     */
    private InstanceType getInstanceType(String type) {
        try {
            return Enums.of(InstanceType.class, type.toUpperCase());
        } catch (Throwable throwable) {
            throw new JettyServerBuilderException(String.format("invalid instance type %s", type), throwable);
        }
    }

    public InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * Creates a new {@link JettyServer} with the specified configurations or default configurations
     * if none were provided.
     *
     * @return {@link JettyServer}
     */
    public JettyServer build() {
        checkNotNull(guiceModules, "Modules are required.");
        Server server = new Server();

        addConnector(server);
        configureServletContextHandler(server);

        return new JettyServer(port, server, _startupTimeout, instanceType);
    }

    /**
     * Creates and configures a {@link ServletContextHandler}
     * TODO: this could be more buildable if needed
     *
     * @param server the server that holds the handler
     */
    private void configureServletContextHandler(Server server) throws JettyServerBuilderException {
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/",
                ServletContextHandler.NO_SESSIONS);

        // CORS configuration
        if(allowPermissiveCors) {
            addCorsFilter(servletContextHandler);
        }

        configureGzipHandler(servletContextHandler);

        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.setContextPath("/");
        servletContextHandler.setWelcomeFiles(new String[] { "index.html" });
        servletContextHandler.setBaseResource(Resource.newResource(JettyServerBuilder.class.getClassLoader()
                .getResource("app/")));
        // You MUST add DefaultServlet or your server will always return 404s
        servletContextHandler.addServlet(DefaultServlet.class, "/");

        ErrorPageErrorHandler errorMapper = new ErrorPageErrorHandler();
        errorMapper.addErrorPage(404, "/404.html"); // map all 404's to root (aka /index.html)
        servletContextHandler.setErrorHandler(errorMapper);

        servletContextHandler.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                try {
                    Injector parentInjector = configureJndiContext();
                    return parentInjector.createChildInjector(guiceModules);
                } catch (Exception t) {
                    throw new JettyServerBuilderException("Error creating Guice injector", t);
                }
            }
        });

        configureGracefulShutdown(server, servletContextHandler);

        server.setHandler(handlerList);
    }

    /**
     * Configure the gzip handler
     *
     * @param servletContextHandler the server context handler
     */
    private void configureGzipHandler(ServletContextHandler servletContextHandler) {
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.addIncludedMethods(
                HttpMethod.GET,
                HttpMethod.POST,
                HttpMethod.PATCH,
                HttpMethod.DELETE,
                HttpMethod.PUT,
                HttpMethod.OPTIONS
        );
        gzipHandler.setIncludedMimeTypes("application/json");
        gzipHandler.setHandler(servletContextHandler);
        gzipHandler.setMinGzipSize(_minGzipSize);
        handlerList.addHandler(gzipHandler);
    }

    private void addCorsFilter(ServletContextHandler servletContextHandler) {
        log.info("Adding permissive CORS filter to server");
        FilterHolder cors = servletContextHandler.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,OPTIONS");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                "Authorization,X-Requested-With,Content-Type,Accept,Cache-Control,Origin");
    }

    /**
     * Sets the server shutdown timeout and unset the default shutdown hook
     *
     * @param server the server that holds the handler
     */
    private void configureGracefulShutdown(Server server, ServletContextHandler servletContextHandler) {
        // Add statistics handler for rest requests
        StatisticsHandler statsHandler = new StatisticsHandler();
        statsHandler.setHandler(servletContextHandler);
        handlerList.addHandler(statsHandler);

        server.setStopTimeout(_shutdownTimeout);
        // do not add a shutdown hook, always attempt for a graceful shutdown
        server.setStopAtShutdown(false);
    }

    /**
     * Sets the connector port and adds the connector to the {@link Server}
     * @param server the server that needs the connector
     */
    private void addConnector(Server server) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
    }

    /**
     * Starts up jndi naming registry for Camel use. The {@link GuiceInitialContextFactory} used as context factory
     * creates it's own injector which is returned to be used as parent injector for the Mercury main
     * injector, so the jndi context is inherited
     *
     * @return parent guice injectpor
     * @throws {@link NamingException} when JNDI startup fails
     */
    private Injector configureJndiContext() throws NamingException {

        // Load up JNDI context for camel registry with camel-guice provided factory
        Hashtable<String, String> jndiEnv = new Hashtable<>();
        jndiEnv.put(InitialContext.INITIAL_CONTEXT_FACTORY, GuiceInitialContextFactory.class.getName());
        InitialContext jndiContext = new InitialContext(jndiEnv);

        // lookup injector created by jndi InitialContextFactory and use it as parent injector for the one to be
        // created by JettyServerBuilder.
        return (Injector) jndiContext.lookup(Injector.class.getName());

    }

}

