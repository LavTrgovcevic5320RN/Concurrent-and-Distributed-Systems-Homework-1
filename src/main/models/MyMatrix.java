package main.models;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;

public class MyMatrix {
    private String name;
    private int rows;
    private int cols;
    private File matrixFile;
    private BigInteger[][] values;

    public MyMatrix() {
    }

    public MyMatrix(String name, int rows, int cols) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.values = new BigInteger[rows][cols];
        for (int row = 0; row < rows; row++) {
            Arrays.fill(this.values[row], BigInteger.ZERO);
        }
    }

    public void setValue(int row, int col, BigInteger value) {
        values[row][col] = value;
    }

    public BigInteger getValue(int row, int col) {
        return values[row][col];
    }

    public void displayInfo() {
        System.out.println("Matrix Name: " + name);
        System.out.println("Dimensions: " + rows + "x" + cols);
        System.out.println("Values:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(values[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static MyMatrix multiplySegment(MyMatrix A, MyMatrix B, int startRow, int endRow, int startCol, int endCol) {
        MyMatrix result = new MyMatrix("Segment", endRow - startRow, endCol - startCol);
        for (int i = startRow; i < endRow; i++) {
            for (int j = startCol; j < endCol; j++) {
                BigInteger sum = BigInteger.ZERO;
                for (int k = 0; k < A.cols; k++) {
                    sum = sum.add(A.values[i][k].multiply(B.values[k][j]));
                }
                result.setValue(i - startRow, j - startCol, sum);
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public File getMatrixFile() {
        return matrixFile;
    }

    public void setMatrixFile(File matrixFile) {
        this.matrixFile = matrixFile;
    }

    public BigInteger[][] getValues() {
        return values;
    }

    public void setValues(BigInteger[][] values) {
        this.values = values;
    }
}
