package com.interview.opentext;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestMultiTaskExecutor {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		
        TaskExecutorService executorService = new TaskExecutorService(3);

        Main.TaskGroup group1 = new Main.TaskGroup(UUID.randomUUID());
        Main.TaskGroup group2 = new Main.TaskGroup(UUID.randomUUID());

        // Submit tasks
        Future<String> future1 = executorService.submitTask(new Main.Task<>(
            UUID.randomUUID(),
            group1,
            Main.TaskType.READ,
            () -> {
                Thread.sleep(1000);
                return "Task 1 from Group 1 completed";
            }
        ));

        Future<String> future2 = executorService.submitTask(new Main.Task<>(
            UUID.randomUUID(),
            group1,
            Main.TaskType.WRITE,
            () -> {
                Thread.sleep(500);
                return "Task 2 from Group 1 completed";
            }
        ));

        Future<String> future3 = executorService.submitTask(new Main.Task<>(
            UUID.randomUUID(),
            group2,
            Main.TaskType.READ,
            () -> {
                Thread.sleep(300);
                return "Task 1 from Group 2 completed";
            }
        ));

        // Wait for and print task results
        System.out.println(future1.get());
        System.out.println(future2.get());
        System.out.println(future3.get());

        // Shutdown the executor
        executorService.shutdown();
    }

}
