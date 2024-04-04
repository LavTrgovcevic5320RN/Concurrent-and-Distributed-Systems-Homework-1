package main.models;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;

@Getter
@Setter
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
}
