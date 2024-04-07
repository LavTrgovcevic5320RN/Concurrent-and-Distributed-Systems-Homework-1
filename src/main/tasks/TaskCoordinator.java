package main.tasks;

import main.matrix.MatrixBrain;
import main.models.MyMatrix;
import main.models.Task;
import main.models.TaskType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskCoordinator implements Runnable {
    private TaskQueue taskQueue;
    private ExecutorService executorService;
    private MatrixBrain matrixBrain;
    private long segmentSize;
    private long maxRowsSize;
    private volatile boolean running = true;

    public TaskCoordinator(TaskQueue taskQueue, long segmentSize, long maxRowsSize, MatrixBrain matrixBrain) {
        this.taskQueue = taskQueue;
        this.executorService = Executors.newCachedThreadPool();
        this.matrixBrain = matrixBrain;
        this.segmentSize = segmentSize;
    }

    @Override
    public synchronized void run() {
        while (running) {
            Task task = taskQueue.getNextTask();
            if (task != null) {
                executorService.submit(() -> {
                    try {
                        if(task.getType() == TaskType.POISON) {
                            MatrixMultiplierTask.getExecutor().shutdown();
                            this.running = false;
                            taskQueue.addTask(task);
                        }
                        else if (task.getType() == TaskType.CREATE) {
                            ((MatrixFileTask)task).setSegmentSize(segmentSize);
                            MyMatrix myMatrix = task.initiate().get();
                            System.out.println("Matrix " + myMatrix.getName() + " created");
                            matrixBrain.addMatrix(myMatrix);
                            taskQueue.addTask(new MatrixMultiplierTask(myMatrix, myMatrix));
                        } else if (task.getType() == TaskType.MULTIPLY) {
                            MyMatrix result = task.initiate().get();
                            matrixBrain.addMatrix(result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Task Coordinator interrupted.");
                    }
                });
            }
        }
        executorService.shutdown();
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public MatrixBrain getMatrixBrain() {
        return matrixBrain;
    }

    public void setMatrixBrain(MatrixBrain matrixBrain) {
        this.matrixBrain = matrixBrain;
    }

    public long getSegmentSize() {
        return segmentSize;
    }

    public void setSegmentSize(long segmentSize) {
        this.segmentSize = segmentSize;
    }

    public long getMaxRowsSize() {
        return maxRowsSize;
    }

    public void setMaxRowsSize(long maxRowsSize) {
        this.maxRowsSize = maxRowsSize;
    }
}

