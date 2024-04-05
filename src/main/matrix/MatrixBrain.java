package main.matrix;

import main.models.MyMatrix;
import main.tasks.MatrixMultiplierTask;
import main.tasks.TaskQueue;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class MatrixBrain {

    private final CopyOnWriteArrayList<MyMatrix> matrices = new CopyOnWriteArrayList<>();
    private final TaskQueue taskQueue;

    public MatrixBrain(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public synchronized void addMatrix(MyMatrix matrix) {
            matrices.add(matrix);
        if(matrix.getName().equals("A1C1"))
            displayMatrix(matrix);
    }

    public synchronized void multiplyMatrices(String matrica1, String matrica2) throws InterruptedException, ExecutionException {
        MyMatrix matrixA = null;
        MyMatrix matrixB = null;
//            Iterator<MyMatrix> iterator2 = matrices.iterator();
//            List<MyMatrix> result2 = new LinkedList<>();
//            iterator2.forEachRemaining(result2::add);
//            while(iterator2.hasNext()) {
//                MyMatrix matrix = iterator2.next();
//                if (matrix.getName().equalsIgnoreCase(matrica1)) {
//                    matrixA = matrix;
//                } else if (matrix.getName().equalsIgnoreCase(matrica2)) {
//                    matrixB = matrix;
//                }
//            }
        for (MyMatrix matrix : matrices) {
            System.out.println("Matrix name: " + matrix.getName());
            if (matrix.getName().equalsIgnoreCase(matrica1)) {
                matrixA = matrix;
            } else if (matrix.getName().equalsIgnoreCase(matrica2)) {
                matrixB = matrix;
            }
        }

        if (matrixA == null || matrixB == null) {
            System.out.println("Matrix wasn't found.");
            return;
        }

        if (matrixA.getCols() == matrixB.getRows()) {
            System.out.println("Matrices can be multiplied.");
            MatrixMultiplierTask task = new MatrixMultiplierTask(matrixA, matrixB);
            taskQueue.addTask(task);
        } else {
            System.out.println("Matrices cannot be multiplied because of incompatible dimensions.");
        }
    }

    // Save matrix to a file
    public void saveMatrixToFile(MyMatrix matrix, String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println(matrix.getName());
            writer.println(matrix.getRows() + " " + matrix.getCols());
            for (int i = 0; i < matrix.getRows(); i++) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    writer.print(matrix.getValue(i, j) + " ");
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayMatrix(MyMatrix matrix) {
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                BigInteger value = matrix.getValue(i, j);
                if (value != null) {
                    System.out.print(value + "\t");
                } else {
                    System.out.print("0\t");
                }
            }
            System.out.println();
        }
    }

    // Clear all matrices
    public void clearMatrices() {
        matrices.clear();
    }

//    public CopyOnWriteArrayList<MyMatrix> getMatrices() {
//        return matrices;
//    }
//
//    public TaskQueue getTaskQueue() {
//        return taskQueue;
//    }
}
