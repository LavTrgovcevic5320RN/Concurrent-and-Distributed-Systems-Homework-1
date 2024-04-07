package main.tasks;

import main.models.MyMatrix;
import main.models.Task;
import main.models.TaskType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MatrixMultiplierTask implements Task {
    private final MyMatrix matrixA;
    private final MyMatrix matrixB;
    private static final int numThreads = Runtime.getRuntime().availableProcessors();
    private static ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    public MatrixMultiplierTask(MyMatrix matrixA, MyMatrix matrixB) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
    }

    @Override
    public TaskType getType() {
        return TaskType.MULTIPLY;
    }

    @Override
    public Future<MyMatrix> initiate() {
        return new Future<MyMatrix>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public MyMatrix get() throws InterruptedException, ExecutionException {
                MyMatrix result = new MyMatrix(matrixA.getName() + matrixB.getName(), matrixA.getRows(), matrixB.getCols());

                int rowsPerThread = Math.max(3, matrixA.getRows() / numThreads);
                int colsPerThread = Math.max(3, matrixB.getCols() / numThreads);

                for (int i = 0; i < matrixA.getRows(); i += rowsPerThread) {
                    for (int j = 0; j < matrixB.getCols(); j += colsPerThread) {
                        int startRow = i;
                        int endRow = Math.min(i + rowsPerThread, matrixA.getRows());
                        int startCol = j;
                        int endCol = Math.min(j + colsPerThread, matrixB.getCols());

                        executor.submit(() -> {
                            MyMatrix segmentResult = MyMatrix.multiplySegment(matrixA, matrixB, startRow, endRow, startCol, endCol);
                            for (int r = startRow; r < endRow; r++) {
                                for (int c = startCol; c < endCol; c++) {
                                    result.setValue(r, c, segmentResult.getValue(r - startRow, c - startCol));
                                }
                            }
                        });
                    }
                }

                return result;
            }

            @Override
            public MyMatrix get(long timeout, TimeUnit unit) {
                return null;
            }
        };
    }

    public MyMatrix getMatrixA() {
        return matrixA;
    }

    public MyMatrix getMatrixB() {
        return matrixB;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void setExecutor(ExecutorService executor) {
        MatrixMultiplierTask.executor = executor;
    }

}
