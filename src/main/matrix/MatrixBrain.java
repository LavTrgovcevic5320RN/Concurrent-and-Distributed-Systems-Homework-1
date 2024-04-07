package main.matrix;

import main.models.MyMatrix;
import main.tasks.MatrixMultiplierTask;
import main.tasks.TaskQueue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MatrixBrain {

    private final CopyOnWriteArrayList<MyMatrix> matrices = new CopyOnWriteArrayList<>();
    private final TaskQueue taskQueue;

    public MatrixBrain(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void addMatrix(MyMatrix matrix) {
        matrices.add(matrix);
//        if (matrix.getName().equals("A1C1")) {
//            System.out.println("Matrix file: " + matrix.getMatrixFile().getAbsolutePath());
//            System.out.println("Directory path: " + matrix.getMatrixFile().getParentFile().getAbsolutePath());
//        }
    }

    public void multiplyMatrices(String matrica1, String matrica2) {
        MyMatrix matrixA = null;
        MyMatrix matrixB = null;
        for (MyMatrix matrix : matrices) {
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
            System.out.println("Matrices are multiplied.");
            MatrixMultiplierTask task = new MatrixMultiplierTask(matrixA, matrixB);
            taskQueue.addTask(task);
        } else {
            System.out.println("Matrices cannot be multiplied because of incompatible dimensions.");
        }
    }

    public void saveMatrixToFile(String matrixName, String fileName) {
        MyMatrix matrix = null;
        for(MyMatrix m : matrices) {
            if(m.getName().equals(matrixName)) {
                matrix = m;
                break;
            }
        }
        if(matrix == null) return;
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("matrix-name=" + matrix.getName() + ", rows=" + matrix.getRows() + ", cols=" + matrix.getCols());
            for (int j = 0; j < matrix.getCols(); j++) {
                for (int i = 0; i < matrix.getRows(); i++) {
                    writer.println(i + "," + j + " = " + matrix.getValue(i, j) + " ");
                }
                writer.println();
            }
            writer.flush();
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

    public void clearMatrices(String fileName) {
        for(MyMatrix matrix : matrices) {
            if(matrix.getName().contains(fileName.toUpperCase())) {
                System.out.println("Matrix " + matrix.getName() + " removed.");
                matrices.remove(matrix);
            }
        }
    }

    public void clearMatricesFromDir(String argument) {
        List<MyMatrix> toRemove = new ArrayList<>();
        for(MyMatrix matrix : matrices) {
            if (!isNameDuplicated(matrix.getName()) && matrix.getMatrixFile().getParent().equals(argument)) {
                System.out.println("Matrix " + matrix.getName() + " removed.");
                toRemove.add(matrix);
            }
        }
        matrices.removeAll(toRemove);
    }
    private boolean isNameDuplicated(String name) {
        if (name == null || name.length() % 2 != 0)
            return false;

        String firstHalf = name.substring(0, name.length() / 2);
        String secondHalf = name.substring(name.length() / 2);

        return firstHalf.equals(secondHalf);
    }
}
