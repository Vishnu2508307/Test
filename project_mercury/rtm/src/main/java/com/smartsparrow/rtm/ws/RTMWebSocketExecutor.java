package com.smartsparrow.rtm.ws;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.MutableAuthenticationContext;

/**
 * A wrapper around the ExecutorService
 */
class RTMWebSocketExecutor {

    private static final Logger log = LoggerFactory.getLogger(RTMWebSocketExecutor.class);

    // the executor service that handles the threads, there is one of these per socket (=> for that specific user)
    private RTMThreadPoolExecutor executor;

    @Inject
    RTMWebSocketExecutor(Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider) {
        // limit the number of concurrent processing of messages per connection.
        executor = new RTMThreadPoolExecutor(1, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
                                             mutableAuthenticationContextProvider.get());
    }

    /**
     * @return the executor service
     */
    public RTMThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * Tries to gracefully stop the executor by stopping new tasks from being accepted but finnish processing already
     * running/accepted tasks. If takes longer than supplied timeout parameter, tries to forcefully stop all tasks
     * and prints a log warn of pending tasks.
     *
     * @param timeout time to wait before forcefully trying to kill all tasks
     * @param timeunit unit of timeout
     * @return the list of runnable that were not executed
     */
    public void shutdownWebsocketExecutor(long timeout, TimeUnit timeunit) {
        executor.shutdown();
        try {
            executor.awaitTermination(timeout, timeunit);
        } catch (InterruptedException e) {
            List<Runnable> unprocessed = executor.shutdownNow();
            unprocessed.forEach(r -> log.warn("Task not processed due to socket close: {}", r));
        }
    }

    /**
     * Wrapper around the submit method. It returns a future that can be used for evaluation.
     *
     * @return {type Future} a future
     */
    public <T> Future<T> submit(Callable<T> callable){
        return executor.submit(callable);
    }

    /**
     * Wrapper around the execute method. Both `execute` and `submit` implementations
     * in a forkJoinPool perform an externalPush. The differences are the `execute`
     * throws a null pointer exception when the runnable is null and does not return
     * a future.
     *
     */
    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * Gracefully shuts down the executor service
     *
     * A shutdown signal is sent to the {@link ExecutorService} then the
     * {@link ExecutorService#awaitTermination(long, TimeUnit)} method is invoked to enabled all the threads inside
     * the pool to finish processing
     *
     * @param timeout the amount of time the executor will wait for all the threads to terminate
     * @param timeUnit the timeUnit value of the timeout
     * @return {@code true} if this executor terminated and
     *         {@code false} if the timeout elapsed before termination
     */
    public boolean gracefulShutdown(long timeout, TimeUnit timeUnit) throws InterruptedException {
        executor.shutdown();
        return executor.awaitTermination(timeout, timeUnit);
    }

    @VisibleForTesting
    public void setExecutor(RTMThreadPoolExecutor executor) {
        this.executor = executor;
    }

}
