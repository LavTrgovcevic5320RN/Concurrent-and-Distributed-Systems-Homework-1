package main.models;

import java.util.concurrent.Future;

public interface Task {
    TaskType getType();

    Future<MyMatrix> initiate();
}
