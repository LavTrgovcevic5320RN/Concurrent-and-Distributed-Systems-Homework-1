package main.tasks;

import main.models.MyMatrix;
import main.models.Task;
import main.models.TaskType;

import java.io.*;
import java.util.concurrent.*;

public class MatrixFileTask implements Task {
    private File file;
    private String name;
    private int rows;
    private int cols;
    private long segmentSize;

    public MatrixFileTask(File file) {
        this.file = file;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            if((line = reader.readLine()) != null){
                if(line.startsWith("matrix_name=")) {
                    String[] comaSplitted = line.split(",");
                    name = comaSplitted[0].split("=")[1];
                    rows = Integer.parseInt(comaSplitted[1].split("=")[1]);
                    cols = Integer.parseInt(comaSplitted[2].split("=")[1]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.CREATE;
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
            public MyMatrix get() throws ExecutionException, InterruptedException {
                ForkJoinPool pool = new ForkJoinPool();
                FileReadTask task = new FileReadTask(file, 0, file.length(), name, rows, cols, segmentSize);

                Future<MyMatrix> future = pool.submit(task);
                return future.get();
            }

            @Override
            public MyMatrix get(long timeout, TimeUnit unit) {
                return null;
            }
        };
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
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

    public long getSegmentSize() {
        return segmentSize;
    }

    public void setSegmentSize(long segmentSize) {
        this.segmentSize = segmentSize;
    }
}
