package com.smartsparrow.util.monitoring;

import com.smartsparrow.util.log.ReactiveMdc;

import reactor.util.context.Context;

public class ReactiveMonitoring {

    /**
     * Creates a reactive context that includes data about all the supported reactive monitoring
     * types available.
     * Currently available types are:
     * <ul>
     *     <li>{@link ReactiveMdc}</li>
     *     <li>{@link ReactiveTransaction}</li>
     * </ul>
     * @return the created reactive context
     */
    public static Context createContext() {
        final Context reactiveMdcContext = ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT);
        final Context traceContext = ReactiveMdc.with(ReactiveMdc.Property.TRACE_ID);
        final Context newRelicContext = ReactiveTransaction.createToken();
        return reactiveMdcContext.putAll(newRelicContext).putAll(traceContext);
    }
}
