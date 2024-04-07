package main.tasks;

import main.models.MyMatrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.concurrent.RecursiveTask;

public class FileReadTask extends RecursiveTask<MyMatrix> {
    private final File file;
    private final long startByte;
    private final long endByte;
    private static final int THRESHOLD = 2000;
    private MyMatrix matrix;
    private long segmentSize;

    public FileReadTask(File file, long startByte, long endByte, String name, int rows, int cols, long segmentSize) {
        this.file = file;
        this.startByte = adjustToLineStart(file, startByte);
        this.endByte = adjustToLineEnd(file, endByte);
        this.matrix = new MyMatrix(name, rows, cols);
        this.segmentSize = segmentSize;
    }

    @Override
    protected MyMatrix compute() {
        long length = endByte - startByte;

        if (length <= THRESHOLD || length <= segmentSize) {
//            System.out.println("Length: " + length);
            return readMatrixFromFile(file, startByte, endByte);
        } else {
            long splitPoint = startByte + segmentSize;
            splitPoint = adjustToNextLineStart(file, splitPoint);

            if (splitPoint - startByte > segmentSize * 2)
                splitPoint = adjustToLineEnd(file, startByte + segmentSize / 2);

            if (splitPoint >= endByte)
                splitPoint = endByte;

            FileReadTask left = new FileReadTask(file, startByte, splitPoint, matrix.getName(), matrix.getRows(), matrix.getCols(), segmentSize);
            FileReadTask right = new FileReadTask(file, splitPoint, endByte, matrix.getName(), matrix.getRows(), matrix.getCols(), segmentSize);

            left.fork();
            MyMatrix rightResult = right.compute();
            MyMatrix leftResult = left.join();
            return combineMatrices(leftResult, rightResult);
        }
    }

    private long adjustToLineStart(File file, long startByte) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (startByte > 0) {
                raf.seek(startByte);
                while (startByte > 0 && raf.read() != '\n') {
                    startByte--;
                    raf.seek(startByte);
                }
            }
            return startByte;
        } catch (IOException e) {
            e.printStackTrace();
            return startByte;
        }
    }

    private long adjustToNextLineStart(File file, long position) {
        long originalPosition = position;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(position);
            while (position < raf.length()) {
                int readByte = raf.read();
                if (readByte == '\n' || readByte == -1) {
                    break;
                }
                position++;
            }

            if (position - originalPosition > segmentSize)
                return adjustToLineEnd(file, originalPosition - segmentSize / 2);

            return position;
        } catch (IOException e) {
            e.printStackTrace();
            return originalPosition;
        }
    }


    private long adjustToLineEnd(File file, long endByte) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(endByte);
            while (endByte < raf.length() && raf.read() != '\n')
                endByte++;

            return endByte;
        } catch (IOException e) {
            e.printStackTrace();
            return endByte;
        }
    }

    private MyMatrix readMatrixFromFile(File file, long startByte, long endByte) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(startByte);
            String line;
            while (raf.getFilePointer() < endByte && (line = raf.readLine()) != null) {
                if (line.isBlank() || line.isEmpty())
                    continue;

                if (!line.startsWith("matrix_name=")) {
                    String[] parts = line.split(" = ");
                    String[] indices = parts[0].split(",");
                    if(parts[1].contains(" "))
                        parts[1] = parts[1].replace(" ", "");

                    BigInteger row = new BigInteger(indices[0]);
                    BigInteger col = new BigInteger(indices[1]);
                    BigInteger value = new BigInteger(parts[1]);
                    matrix.setValue(row.intValue(), col.intValue(), value);
                    matrix.setMatrixFile(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    private MyMatrix combineMatrices(MyMatrix left, MyMatrix right) {
        MyMatrix combined = new MyMatrix(left.getName(), left.getRows(), left.getCols());
        combined.setMatrixFile(left.getMatrixFile());

        for (int row = 0; row < left.getRows(); row++) {
            for (int col = 0; col < left.getCols(); col++) {
                if(!left.getValue(row, col).equals(BigInteger.ZERO))
                    combined.setValue(row, col, left.getValue(row, col));
            }
        }

        for (int row = 0; row < right.getRows(); row++) {
            for (int col = 0; col < right.getCols(); col++) {
                if(!right.getValue(row, col).equals(BigInteger.ZERO))
                    combined.setValue(row, col, right.getValue(row, col));
            }
        }

        return combined;
    }
}
