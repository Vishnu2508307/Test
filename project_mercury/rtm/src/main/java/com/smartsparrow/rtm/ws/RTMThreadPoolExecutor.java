package com.smartsparrow.rtm.ws;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.MutableAuthenticationContextProvider;

/**
 * Create our own Thread Pool Executor, so that we can hook into beforeExecute() in order to set any
 * custom ThreadLocal states.
 */
class RTMThreadPoolExecutor extends ThreadPoolExecutor {

    private MutableAuthenticationContext authenticationContext;

    RTMThreadPoolExecutor(int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            MutableAuthenticationContext authenticationContext) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        // set the authentication context in the running thread.
        MutableAuthenticationContextProvider.set(authenticationContext);

        //
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        // remove the authentication context in the running thread as it can cause leaks.
        MutableAuthenticationContextProvider.cleanup();
    }

    public long getPendingTasksCount() {
        BlockingQueue<Runnable> workQueue = super.getQueue();
        int activeTasksCount = super.getActiveCount();
        int scheduleTasksCount = workQueue.size();

        return activeTasksCount + scheduleTasksCount;
    }
}
