package main.tasks;

import main.models.MyMatrix;
import main.models.Task;
import main.models.TaskType;

import java.util.concurrent.Future;

public class StopTask implements Task {
    @Override
    public TaskType getType() {
        return TaskType.POISON;
    }

    @Override
    public Future<MyMatrix> initiate() {
        return null;
    }
}
