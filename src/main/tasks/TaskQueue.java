package main.tasks;

import main.models.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {

    private BlockingQueue<Task> queue;

    public TaskQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void addTask(Task task) {
        try {
            queue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Task getNextTask() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
