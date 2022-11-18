package com.smartsparrow.dse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.smartsparrow.util.Enums;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Utility class to assist with common mutator operations.
 */
public class Mutators {

    //
    private static final Logger log = LoggerFactory.getLogger(Mutators.class);

    //
    private static Scheduler mutatorScheduler =  Schedulers.newElastic("mutators");

    /**
     * Helper to enumerate a series of mutators and build upsert queries.
     * <br>
     * Each mutator will be scheduled to perform work using Schedulers.computation()
     *
     * @param mutators
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    public static <T> Flux<? extends Statement> upsert(final Collection<TableMutator<T>> mutators,
                                                       final T... mutations) {
        //
        return apply(TableMutator::upsert, mutators, mutations);
    }

    /**
     * Helper to build a series of upsert query statements from mutations for a single mutator.
     * <br>
     * Each mutator will be scheduled to perform work using Schedulers.computation()
     *
     * @param mutator
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    public static <T> Flux<? extends Statement> upsert(final TableMutator<T> mutator, final T... mutations) {
        return apply(TableMutator::upsert, mutator, mutations);
    }

    /**
     * Helper to build a series of upsert query statements from mutations for a single mutator.
     *
     * @param mutator
     * @param mutations
     * @return
     */
    public static <T> Flux<? extends Statement> upsert(final TableMutator<T> mutator, final Publisher<T> mutations) {
        return apply(TableMutator::upsert, mutator, mutations);
    }

    /**
     * Helper to enumerate a series of mutators and build delete queries.
     *
     * @param mutators
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    public static <T> Flux<? extends Statement> delete(final Collection<TableMutator<T>> mutators,
                                                       final T... mutations) {
        //
        return apply(TableMutator::delete, mutators, mutations);
    }

    /**
     * Helper to build a series of delete query statements from mutations.
     *
     * @param mutator
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    public static <T> Flux<? extends Statement> delete(final TableMutator<T> mutator, final T... mutations) {
        return apply(TableMutator::delete, mutator, mutations);
    }

    /**
     * Helper to apply the func to a series of mutators in order to build Statements.
     * <br>
     *
     * @param func
     * @param mutators
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    private static <T> Flux<? extends Statement> apply(BiFunction<TableMutator<T>, T, Statement> func,
                                                       final Collection<TableMutator<T>> mutators, final T... mutations) {

        // Error conditions, check that there are supplied parameters to work with.
        // consistently throw a IllegalArgumentException on errors.
        Preconditions.checkArgument(func != null, "func must be provided (is null)");
        Preconditions.checkArgument(mutators != null, "mutators must be provided (is null)");
        Preconditions.checkArgument(mutations != null, "mutations must be provided (is null)");
        Preconditions.checkArgument(!mutators.isEmpty(), "mutators must be provided (is empty)");
        Preconditions.checkArgument(mutations.length > 0, "mutations must be provided (is empty)");

        // for each of the mutators.
        return Flux.fromIterable(mutators).flatMap(mutator -> apply(func, mutator, mutations));
    }

    /**
     * Helper to build statements by applying the func of a single mutation to each mutation in a collection.
     *
     * @param func
     * @param mutator
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    private static <T> Flux<? extends Statement> apply(BiFunction<TableMutator<T>, T, Statement> func,
                                                       final TableMutator<T> mutator, final T... mutations) {

        // Error conditions, check that there are supplied parameters to work with.
        // consistently throw a IllegalArgumentException on errors.
        Preconditions.checkArgument(func != null);
        Preconditions.checkArgument(mutator != null);
        Preconditions.checkArgument(mutations != null, "mutations must be provided (is null)");
        Preconditions.checkArgument(mutations.length > 0, "mutations must be provided (is empty)");

        // on the mutators scheduler, build the statement.
        return Flux.just(mutations).flatMap(mutation -> {

            // build and return the Statement as Flux.
            if (log.isDebugEnabled()) {
                log.debug("building mutation using mutator={} with mutation={}", mutator, mutation);
            }
            return Flux.just(func.apply(mutator, mutation));

        }).subscribeOn(mutatorScheduler);
    }

    /**
     * Helper to build statements by applying the func of a single mutation to each mutation in a Flux/Mono
     *
     * @param func
     * @param mutator
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    private static <T> Flux<? extends Statement> apply(BiFunction<TableMutator<T>, T, Statement> func,
                                                       final TableMutator<T> mutator, final Publisher<T> mutations) {

        // Error conditions, check that there are supplied parameters to work with.
        // consistently throw a IllegalArgumentException on errors.
        Preconditions.checkArgument(func != null);
        Preconditions.checkArgument(mutator != null);
        Preconditions.checkArgument(mutations != null, "mutations must be provided (is null)");

        // on the mutators scheduler, build the statement.
        return Flux.from(mutations).map(mutation -> {

            // build and return the Statement as Flux.
            if (log.isDebugEnabled()) {
                log.debug("building mutation using mutator={} with mutation={}", mutator, mutation);
            }
            return func.apply(mutator, mutation);
        }).subscribeOn(mutatorScheduler);
    }


    /**
     * Helper to apply the func to a series of mutations in order to build Statements.
     *
     * @param func
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     */
    @SafeVarargs
    private static <T> Flux<? extends Statement> apply(Function<T, Statement> func, final T... mutations) {

        // Error conditions, check that there are supplied parameters to work with.
        // consistently throw a IllegalArgumentException on errors.
        Preconditions.checkArgument(func != null);
        Preconditions.checkArgument(mutations != null, "mutations must be provided (is null)");
        Preconditions.checkArgument(mutations.length > 0, "mutations must be provided (is empty)");

        // on the computation scheduler, build the statement.
        return Flux.just(mutations).flatMap(mutation -> {

            // build and return the Statement as a Flux.
            if (log.isDebugEnabled()) {
                log.debug("building mutation with mutation={}", mutation);
            }
            return Flux.just(func.apply(mutation));

        }).subscribeOn(mutatorScheduler);
    }

    /**
     * Helper to enumerate a series of mutators and build upsert queries.
     *
     * @param mutators
     * @param mutations
     * @return
     * @deprecated this methods blocks on onNext calls from the Iterable and should be avoided.
     * Prefer {@link Mutators#upsert(Collection, Object[])}
     */
    @Deprecated
    @SafeVarargs
    public static <T> Iterable<? extends Statement> upsertAsIterable(final Collection<TableMutator<T>> mutators,
                                                                     final T... mutations) {
        return upsert(mutators, mutations).publishOn(Schedulers.elastic()).toIterable();
    }

    /**
     * Helper to generate a series of upsert query statements from mutations and a single mutator.
     *
     * @param mutator
     * @param mutations
     * @return
     * @deprecated this methods blocks on onNext calls from the Iterable and should be avoided.
     * Prefer {@link Mutators#upsert(TableMutator, Object[])}
     */
    @Deprecated
    @SafeVarargs
    public static <T> Iterable<? extends Statement> upsertAsIterable(final TableMutator<T> mutator,
                                                                     final T... mutations) {
        return upsert(mutator, mutations).publishOn(Schedulers.elastic()).toIterable();
    }

    /**
     * Helper to enumerate a series of mutators and build delete queries.
     *
     * @param mutators
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     * @deprecated this methods blocks on onNext calls from the Iterable and should be avoided.
     * Prefer {@link Mutators#delete(Collection, Object[])}
     */
    @Deprecated
    @SafeVarargs
    public static <T> Iterable<? extends Statement> deleteAsIterable(final Collection<TableMutator<T>> mutators,
                                                                     final T... mutations) {
        return delete(mutators, mutations).publishOn(Schedulers.elastic()).toIterable();
    }

    /**
     * Helper to generate a series of delete query statements from mutations and a single mutator.
     *
     * @param mutator
     * @param mutations
     * @return
     * @see <a href="http://projectreactor.io/docs/core/release/reference/#schedulers">Reactor Schedulers reference</a>
     * @deprecated this methods blocks on onNext calls from the Iterable and should be avoided.
     * Prefer {@link Mutators#delete(TableMutator, Object[])}
     */
    @Deprecated
    @SafeVarargs
    public static <T> Iterable<? extends Statement> deleteAsIterable(final TableMutator<T> mutator,
                                                                     final T... mutations) {
        return delete(mutator, mutations).publishOn(Schedulers.elastic()).toIterable();
    }

    /**
     * Helper to apply a mutator method on a series of mutations.
     *
     * @param func
     * @param mutations
     * @return
     * @deprecated this methods blocks on onNext calls from the Iterable and should be avoided.
     * Prefer {@link Mutators#apply(Function, Object[])}
     */
    @Deprecated
    @SafeVarargs
    public static <T> Iterable<? extends Statement> applyAsIterable(final Function<T, Statement> func,
                                                                    final T... mutations) {
        return apply(func, mutations).publishOn(Schedulers.elastic()).toIterable();
    }

    /**
     * Helper to run executeAsync on statement operator and blocks till all futures return.
     * Should be used in insert/update/delete operations that have no return value.
     *
     * @param session
     * @param statements
     * @deprecated This version blocks until all ResultSetFutures are done and should not be used,
     * give preference to {@link Mutators#execute}
     */
    @Deprecated
    public static void executeBlocking(Session session, Statement... statements) {
        executeBlocking(session, Arrays.asList(statements));
    }

    /**
     * Helper to run executeAsync on statement operator and blocks till all futures return.
     * Should be used in insert/update/delete operations that have no return value.
     *
     * @param session
     * @param statements
     * @deprecated This version blocks until all ResultSetFutures are done and should not be used,
     * give preference to {@link Mutators#execute}
     */
    @Deprecated
    public static void executeBlocking(Session session, Iterable<? extends Statement> statements) {
        Preconditions.checkArgument(statements != null, "statements iterable must be provided (is null)");
        Preconditions.checkArgument(session != null && !session.isClosed(), "invalid session was provided (is null or closed)");
        List<ResultSetFuture> futures = StreamSupport.stream(statements.spliterator(), true).map(session::executeAsync)
                .collect(Collectors.toList());
        futures.forEach(ResultSetFuture::getUninterruptibly); // block till all futures return
    }

    /**
     * Takes in a {@link Flux} of {@link Statement} and executes each asynchronously, returning a Flux of {@link Void}.
     * <p>
     * The Flux&lt;{@link Void}&gt; result is because mutations usually don't care about returned {@link ResultSet}.
     * If ResultSet is needed (ex: to verify a LWT was applied), use {@link ResultSets}
     *
     * @param session    Cassandra cluster {@link Session}
     * @param statements Flux of {@link Statement} to be run
     * @return Flux of {@link Void}
     */
    public static Flux<Void> execute(Session session, Flux<? extends Statement> statements) {
        return statements
                // Switch to elastic scheduler for DB call
                .publishOn(Schedulers.elastic())
                // ResultSet is a Guava ListenableFuture, Reactor supports natively Java8 CompletableFuture, so we need
                // to manually create our publisher
                .flatMap(statement -> Mono.create(sink -> Futures
                        .addCallback(session.executeAsync(statement), new FutureCallback<ResultSet>() {
                            @Override
                            public void onSuccess(ResultSet result) {
                                sink.success();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                sink.error(t);
                            }
                        }, CassandraExecutor.getExecutorService())));
    }

    /**
     * Utility to set the nth parameter in a bound statement to the value supplied only if value is not null, to avoid writing
     * tombstones
     *
     * Note: positional parameters are zero indexed
     *
     * @param stmt the prepared statement
     * @param index the index of the parameter
     * @param value the value to be set, if not null
     * @param typeClass class object representing the type of 'value' so driver can map to right codec
     */
    public static <T> void bindNonNull(BoundStatement stmt, int index, T value, Class<T> typeClass) {
        if(value != null) {
            stmt.set(index, value, typeClass);
        }
    }

    public static void bindNonNull(BoundStatement stmt, int index, UUID value) {
        bindNonNull(stmt, index, value, UUID.class);
    }

    public static void bindNonNull(BoundStatement stmt, int index, String value) {
        bindNonNull(stmt, index, value, String.class);
    }

    public static void bindNonNull(BoundStatement stmt, int index, Boolean value) {
        bindNonNull(stmt, index, value, Boolean.class);
    }

    public static void bindNonNull(BoundStatement stmt, int index, Integer value) {
        bindNonNull(stmt, index, value, Integer.class);
    }

    public static void bindNonNull(BoundStatement stmt, int index, Long value) {
        bindNonNull(stmt, index, value, Long.class);
    }

    public static void bindNonNull(BoundStatement stmt, int index, Float value) {
        bindNonNull(stmt, index, value, Float.class);
    }

    public static <T extends Enum> void bindNonNull(BoundStatement stmt, int index, T enumValue) {
        if (enumValue != null) {
            bindNonNull(stmt, index, Enums.asString(enumValue));
        }
    }
}
