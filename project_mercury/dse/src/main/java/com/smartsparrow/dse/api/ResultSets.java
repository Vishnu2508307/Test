package com.smartsparrow.dse.api;

import java.util.Set;
import java.util.function.Function;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.smartsparrow.util.Enums;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

/**
 * 
 * Based on:
 *  - http://www.datastax.com/dev/blog/java-driver-async-queries
 *  - https://github.com/olim7t/cassandra-driver-async-examples/blob/master/src/main/java/com/datastax/ResultSets.java
 *
 *  Ported to be backed by Reactor instead of RxJava
 *
 */
public class ResultSets {

    /**
     * Executes the supplied statements, and returns a {@link Flux}  that emits the results as they become available.
     *
     * @param session the {@link Session}
     * @param statements the {@link Statement} queries to execute.
     *
     * @return {@link Flux}&lt{@link ResultSet}&gt
     */
    static Flux<ResultSet> query(Session session, Flux<Statement> statements){
        return statements
        //
        .publishOn(Schedulers.elastic()).flatMap(statement ->
                        //
                        Mono.create(sink -> Futures.addCallback(session.executeAsync(statement),
                                //
                                resultSetCallback(sink),
                                //
                                CassandraExecutor.getExecutorService())));
    }

    /**
     * Executes the supplied statements, and returns a {@link Flux}  that emits the results as they become available.
     *
     * @param session the {@link Session}
     * @param statements the {@link Statement} queries to execute.
     *
     * @return {@link Flux}&lt{@link ResultSet}&gt
     */
    public static Flux<ResultSet> query(Session session, Statement... statements) {
        return query(session, Flux.just(statements));
    }

    // Callback that handles the completion of ResultSetFuture result by supplying the Mono .onNext() emitted value
    private static FutureCallback<ResultSet> resultSetCallback(MonoSink<ResultSet> sink) {
        return new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                sink.success(result);
            }

            @Override
            public void onFailure(Throwable t) {
                sink.error(t);
            }
        };
    }

    /**
     * Get a Boolean value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Boolean value or null if the underlying data is null
     */
    public static Boolean getNullableBoolean(final Row row, final String columnName) {
        return row.isNull(columnName) ? null : row.getBool(columnName);
    }

    /**
     * Get a Integer value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Integer value or null if the underlying data is null
     */
    public static Integer getNullableInteger(final Row row, final String columnName) {
        return row.isNull(columnName) ? null : row.getInt(columnName);
    }

    /**
     * Get a Long value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Long value or null if the underlying data is null
     */
    public static Long getNullableLong(final Row row, final String columnName) {
        return row.isNull(columnName) ? null : row.getLong(columnName);
    }

    /**
     * Get a Float value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Float value or null if the underlying data is null
     */
    public static Float getNullableFloat(final Row row, final String columnName) {
        return row.isNull(columnName) ? null : row.getFloat(columnName);
    }

    /**
     * Get a Double value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Double value or null if the underlying data is null
     */
    public static Double getNullableDouble(final Row row, final String columnName) {
        return row.isNull(columnName) ? null : row.getDouble(columnName);
    }

    /**
     * Get a Enum (represented by a String) value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Boolean value or null if the underlying data is null
     */
    public static <T extends Enum<T>> T getNullableEnum(final Row row, final String columnName, Class<T> enumType) {
        return row.isNull(columnName) ? null : Enums.of(enumType, row.getString(columnName));
    }

    /**
     * Get a Set of Enums (represented by a String) value from the row by column name
     *
     * @param row the data row
     * @param columnName the name of the column
     * @return a Boolean value or null if the underlying data is null
     */
    public static <T extends Enum<T>> Set<T> getNullableEnumSet(final Row row,
                                                                final String columnName,
                                                                Class<T> enumType) {
        return row.isNull(columnName) ? null : Enums.of(enumType, row.getSet(columnName, String.class));
    }
}
