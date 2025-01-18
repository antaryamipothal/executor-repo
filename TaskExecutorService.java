package com.interview.opentext;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.interview.opentext.Main.Task;

public class TaskExecutorService implements Main.TaskExecutor{ 

	private final LinkedBlockingQueue<TaskCombinedWrapper<?>> taskQueue = new LinkedBlockingQueue<>();
	private final ConcurrentHashMap<UUID, Boolean> activeTaskGroups = new ConcurrentHashMap<>();
	private final ExecutorService executor;
	private final int maxNoConcurrency;
	private volatile boolean isShutdown = false;

	public TaskExecutorService(int maxNoConcurrency) {
		this.maxNoConcurrency = maxNoConcurrency;
		this.executor = Executors.newFixedThreadPool(maxNoConcurrency);
		startTaskExecution();
	}

	@Override
	public <T> Future<T> submitTask(Task<T> task) {
		if (task == null) {
			throw new IllegalArgumentException("Task must not be null");
		}

		FutureTask<T> futureTask = new FutureTask<>(task.taskAction());
		TaskCombinedWrapper<T> taskWrapper = new TaskCombinedWrapper<>(task, futureTask);
		taskQueue.offer(taskWrapper);
		return futureTask;
	}

	private void startTaskExecution() {
		new Thread(() -> {
			while (true) {
				try {
					TaskCombinedWrapper<?> taskWrapper = taskQueue.take();
					UUID groupId = taskWrapper.task.taskGroup().groupUUID();

					synchronized (activeTaskGroups) {
						while (activeTaskGroups.containsKey(groupId)) {
							activeTaskGroups.wait();
						}
						activeTaskGroups.put(groupId, true);
					}

					executor.submit(() -> {
						try {
							taskWrapper.futureTask.run();
						} finally {
							synchronized (activeTaskGroups) {
								activeTaskGroups.remove(groupId);
								activeTaskGroups.notifyAll();
							}
						}
					});
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}).start();
	}

	private static class TaskCombinedWrapper<T> {
		final Main.Task<T> task;
		final FutureTask<T> futureTask;

		TaskCombinedWrapper(Main.Task<T> task, FutureTask<T> futureTask) {
			this.task = task;
			this.futureTask = futureTask;
		}
	}
	

	public void shutDownExecutor() {
		
        isShutdown = true;

        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("Timeout reached. Forcing shutdown...");

                executor.shutdownNow();
            } else {
                System.out.println("ExecutorService shut down gracefully.");
            }
        } catch (InterruptedException e) {
            System.out.println("Shutdown interrupted. Forcing shutdown...");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        taskQueue.clear();
        synchronized (activeTaskGroups) {
            activeTaskGroups.clear();
        }
        System.out.println("TaskExecutorService shutdown complete.");
		
	}

}
