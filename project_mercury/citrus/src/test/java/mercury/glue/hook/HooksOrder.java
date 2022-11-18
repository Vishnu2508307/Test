package mercury.glue.hook;

import com.consol.citrus.cucumber.CitrusLifecycleHooks;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * Contains all constants for defining an order in which Citrus Hooks should be run.
 * For every hook we should have constant here and use it in {@link Before}/{@link After} annotations : <code>@Before(order = JETTY_SERVER_BEFORE)</code>
 * <br/>
 * The default order is 10000.
 * For {@link Before} lower numbers are run first.
 * For {@link After} higher numbers are run first.
 * <br/>
 * Please be aware that {@link CitrusLifecycleHooks} has default orders and {@link CitrusLifecycleHooks#before(Scenario)} should be run <u>after</u> custom hooks and
 * {@link CitrusLifecycleHooks#after(Scenario)} should be run <u>before</u> our custom hooks.
 */
public class HooksOrder {

    /*
    Before constants in order of execution. Lower numbers are run first.
     */
    //Run Jetty server
    public static final int JETTY_SERVER_BEFORE = 0;
    //
    public static final int IGNORE_BEFORE = 10;
    //open web socket
    public static final int WEB_SOCKET_CLIENT_BEFORE = 100;
    //run tests
    public static final int DEFAULT_BEFORE = 10000;

    /*
    After constants in order of execution. Higher numbers are run first.
     */
    //execute all tests
    public static final int DEFAULT_AFTER = 10000;
    //close web socket
    public static final int WEB_SOCKET_CLIENT_AFTER = 100;
    //stop Jetty server
    public static final int JETTY_SERVER_AFTER = 0;


}
