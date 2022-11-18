package com.smartsparrow.util.monitoring;

import java.util.function.Consumer;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Signal;
import reactor.util.context.Context;

/**
 * This class provides a series of utility methods that enables NewRelic instrumentation of reactive code
 * TODO make this class package-private, should only be invoked via ReactiveMonitoring
 */
public class ReactiveTransaction {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReactiveTransaction.class);

    /**
     * Define the supported New Relic instrumentation types
     */
    public enum Type {
        TOKEN
    }

    /**
     * Create a reactive context holding the NewRelic agent token
     *
     * @return the context
     */
    public static Context createToken() {
        final Token token = NewRelic.getAgent().getTransaction().getToken();
        return Context.of(Enums.asString(Type.TOKEN), token);
    }

    /**
     * Link the token to the current NewRelic transaction
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    public static <T> Consumer<? super Signal<T>> linkOnNext() {
        return signal -> {
            if (signal.isOnNext()) {
                if (signal.getContext().hasKey(Type.TOKEN)) {
                    Token token = signal.getContext().get(Enums.asString(Type.TOKEN));
                    token.link();
                }
            }
        };
    }

    /**
     * Expire the token releasing the current NewRelic transaction
     *
     * @param <T> the signal type
     * @return a generic signal consumer
     */
    public static <T> Consumer<? super Signal<T>> expireOnComplete() {
        return signal -> {
            if (signal.isOnComplete()) {
                if (signal.getContext().hasKey(Type.TOKEN)) {
                    Token token = signal.getContext().get(Enums.asString(Type.TOKEN));
                    token.expire();
                }
            }
        };
    }
}
