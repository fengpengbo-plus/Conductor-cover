package com.fengpb.conductor.core.execution.tasks;

import com.fengpb.conductor.core.dao.QueueDAO;
import com.fengpb.conductor.core.execution.WorkflowExecutor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class SystemTaskWorkerCoordinator {

    @Autowired
    WorkflowExecutor workflowExecutor;

    @Resource
    QueueDAO queueDAO;

    private ExecutorService executorService;

    private LinkedBlockingQueue<Runnable> workerQueue;

    private int workerQueueSize;

    static BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private int pollInterval;

    ExecutionConfig defaultExecutionConfig;

    public static Map<String, WorkflowSystemTask> taskNameWorkFlowTaskMapping = new ConcurrentHashMap<>();

    public SystemTaskWorkerCoordinator() {
        int threadCount = 10;
        pollInterval = 50;
        this.workerQueueSize = 5;
        this.workerQueue = new LinkedBlockingQueue<>(workerQueueSize);

        if (threadCount > 0) {
            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("system-task-worker-%d")
                    .build();
            this.executorService = new ThreadPoolExecutor(threadCount, threadCount,
                    0L, TimeUnit.MILLISECONDS,
                    workerQueue, threadFactory);
            this.defaultExecutionConfig = new ExecutionConfig(this.executorService, this.workerQueue);
            new Thread(this::listen).start();
        }
    }

    private void listen() {
        try {
            for (;;) {
                String workflowSystemTaskQueueName = queue.poll(60, TimeUnit.SECONDS);
                if (workflowSystemTaskQueueName != null) {
                    listen(workflowSystemTaskQueueName);
                }
            }
        } catch (InterruptedException ie) {

        }
    }

    private void listen(String queueName) {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> poolAndExecute(queueName),
                1000, pollInterval, TimeUnit.MILLISECONDS);
    }

    void poolAndExecute(String queueName) {
        try {
            List<String> polledTaskIds = queueDAO.pop(queueName, 2, 200);
            for (String taskId : polledTaskIds) {
                log.info("Task: {} from queue: {} 被发送至工作流执行器", taskId, queueName);
                try {
                    String taskName = queueName;
                    WorkflowSystemTask systemTask = taskNameWorkFlowTaskMapping.get(taskName);
                    ExecutorService executorService = this.defaultExecutionConfig.service;
                    executorService.submit(() -> workflowExecutor.executeSystemTask(systemTask, taskId));
                } catch (RejectedExecutionException ree) {
                    log.warn("工作队列满. Size: {}, queue: {}", workerQueue.size(), queueName);
                }
            }
        } catch (Exception e) {
            log.error("队列:{}系统任务执行异常", queueName, e);
        }
    }

    public static synchronized void add(WorkflowSystemTask systemTask) {
        log.info("系统任务：{}添加至队列", systemTask.getName());
        taskNameWorkFlowTaskMapping.putIfAbsent(systemTask.getName(), systemTask);
        queue.add(systemTask.getName());
    }
}
















