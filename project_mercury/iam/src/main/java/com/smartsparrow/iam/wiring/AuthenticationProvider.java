package com.smartsparrow.iam.wiring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.MutableAuthenticationContext;

/**
 * This class acts as the underlying logic for the Authentication Context providers.
 */
class AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(IamModule.class);

    // create a thread local to store the authentication context.
    private static ThreadLocal<MutableAuthenticationContext> threadLocal;

    // initialize the context with an object; providers should not return null.
    static {
        threadLocal = ThreadLocal.withInitial(() -> new MutableAuthenticationContext());
    }

    /**
     * Get the thread local's authentication context value.
     *
     * @return the thread local's authentication context value.
     */
    static MutableAuthenticationContext get() {
        MutableAuthenticationContext value = threadLocal.get();
        if (log.isDebugEnabled()) {
            log.debug("get authentication context on thread named '{}' and returning value {}", Thread.currentThread().getName(), value);
        }
        return value;
    }

    /**
     * Remove any set thread local values.
     */
    static void cleanup() {
        threadLocal.remove();
    }

    /**
     * Set the thread local authentication context value.
     *
     * @param value the value to set in the thread local.
     */
    static void set(MutableAuthenticationContext value) {
        if (log.isDebugEnabled()) {
            log.debug("set authentication context on thread named '{}' and value {}", Thread.currentThread().getName(), value);
        }
        threadLocal.set(value);
    }
}
