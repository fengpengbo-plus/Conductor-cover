package com.fengpb.conductor.core.execution.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class ExecutionConfig {

    ExecutorService service;

    LinkedBlockingQueue<Runnable> workerQueue;

    public ExecutionConfig(ExecutorService service, LinkedBlockingQueue<Runnable> workerQueue) {
        this.service = service;
        this.workerQueue = workerQueue;
    }
}
