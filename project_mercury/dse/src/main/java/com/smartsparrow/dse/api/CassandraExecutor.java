package com.smartsparrow.dse.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CassandraExecutor {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        return executorService;
    }
}
