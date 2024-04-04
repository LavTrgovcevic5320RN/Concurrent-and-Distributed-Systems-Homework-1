package main.tasks;

import lombok.Getter;
import lombok.Setter;
import main.matrix.MatrixBrain;
import main.matrix.MatrixExtractor;
import main.matrix.MatrixMultiplier;
import main.models.MyMatrix;
import main.models.Task;
import main.models.TaskType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class TaskCoordinator implements Runnable {
    private TaskQueue taskQueue;
    private ExecutorService executorService;
    private MatrixExtractor extractorThreadPool;
    private MatrixMultiplier multiplierThreadPool;
    private MatrixBrain matrixBrain;
    private long segmentSize;
    private long maxRowsSize;

    public TaskCoordinator(TaskQueue taskQueue, long segmentSize, long maxRowsSize) {
        this.taskQueue = taskQueue;
        this.executorService = Executors.newCachedThreadPool();
        this.extractorThreadPool = new MatrixExtractor(taskQueue);
        this.multiplierThreadPool = new MatrixMultiplier();
        this.matrixBrain = new MatrixBrain(taskQueue);
        this.segmentSize = segmentSize;
    }

    @Override
    public void run() {
        while (true) {
            Task task = taskQueue.getNextTask();
            if (task != null) {
                executorService.submit(() -> {
                    try {
                        if (task.getType() == TaskType.CREATE) {
                            ((MatrixFileTask)task).setSegmentSize(segmentSize);
                            MyMatrix myMatrix = task.initiate().get();
                            matrixBrain.addMatrix(myMatrix);
                            System.out.println("Matrica kreirana");
//                                extractorThreadPool.extractMatrixFromFile(((MatrixFileTask) task).getFile(), task);

                        } else if (task.getType() == TaskType.MULTIPLY) {
                            MyMatrix result = task.initiate().get();
                            matrixBrain.displayMatrix(result);
//                            matrixBrain.addMatrix(result);
//                            System.out.println("Matrice pomnozene");
//                            multiplierThreadPool.submitMultiplicationTask(((MatrixMultiplierTask) task).getMatrixA(), ((MatrixMultiplierTask) task).getMatrixB());
                        }
                    } catch (Exception e) {
//                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                        System.err.println("Task Coordinator interrupted.");
                    }
                });
            }
        }
    }

}

