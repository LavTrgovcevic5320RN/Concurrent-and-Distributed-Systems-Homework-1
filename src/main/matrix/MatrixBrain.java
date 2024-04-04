package main.matrix;

import lombok.Getter;
import lombok.Setter;
import main.models.MyMatrix;
import main.tasks.MatrixMultiplierTask;
import main.tasks.TaskQueue;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MatrixBrain {

    private List<MyMatrix> matrices;
    private TaskQueue taskQueue;

    public MatrixBrain(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
        this.matrices = new ArrayList<>();
    }

    public void addMatrix(MyMatrix matrix) {
        matrices.add(matrix);
//        System.out.println("Matrix added.");
//        displayMatrix(matrix);
    }

    public void multiplyMatrices(String matrica1, String matrica2) {
        MyMatrix matrixA = new MyMatrix();
        MyMatrix matrixB = new MyMatrix();
        for (MyMatrix matrix : matrices) {
            if (matrix.getName().equals(matrica1)) {
                matrixA = matrix;
            } else if (matrix.getName().equals(matrica2)) {
                matrixB = matrix;
            }
        }

        if(matrixA.getCols() == matrixB.getRows()) {
            MatrixMultiplierTask task = new MatrixMultiplierTask(matrixA, matrixB);
            taskQueue.addTask(task);
            System.out.println("Matrices can be multiplied.");
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
}
