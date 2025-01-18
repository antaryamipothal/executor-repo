# executor-repo
This repository being have implementation and test cases where can validate multi threaded scenarios with multi task groups with unique group ids
The task is to create a task executor service that meets the following requirements:

**Concurrent Task Submission:** Tasks can be submitted without blocking the submitting thread.
**Asynchronous and Concurrent Execution:** Tasks are executed concurrently, with a limit on maximum concurrency.
**Task Results:** Results can be retrieved via a Future object.
**Order Preservation:** Tasks must execute in the order they are submitted.
**Task Grouping:** Tasks in the same TaskGroup must not run concurrently.

**Assumptions:**
TaskGroup: Tasks are grouped by a TaskGroup object, ensuring no two tasks from the same group are executed simultaneously.
Thread-Safety: Thread-safe mechanisms are needed to manage task submission and execution.
Concurrency Control: A ConcurrentHashMap can track active tasks for each TaskGroup to prevent concurrent execution within groups.
Order Preservation: A LinkedBlockingQueue ensures tasks are dequeued in the order they are submitted.
