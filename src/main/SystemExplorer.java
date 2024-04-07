package main;

import main.tasks.MatrixFileTask;
import main.tasks.TaskQueue;

import java.io.File;
import java.util.HashMap;

import java.util.Map;

public class SystemExplorer implements Runnable {
    private final Map<File, Long> fileLastModifiedMap = new HashMap<>();
    private TaskQueue taskQueue;
    private File[] directoriesToExplore = new File[0];
    private boolean firstRun = true;
    private long systemExplorerSleepTime;
    private boolean running = true;

    public SystemExplorer(TaskQueue taskQueue, long systemExplorerSleepTime) {
        this.taskQueue = taskQueue;
        this.systemExplorerSleepTime = systemExplorerSleepTime;
    }

    public void addDirectory(File directory) {
        File[] newDirectoriesToExplore = new File[directoriesToExplore.length + 1];
        System.arraycopy(directoriesToExplore, 0, newDirectoriesToExplore, 0, directoriesToExplore.length);
        newDirectoriesToExplore[directoriesToExplore.length] = directory;
        directoriesToExplore = newDirectoriesToExplore;
    }

    @Override
    public void run() {
        exploreDirectories();
        firstRun = false;
        while (running) {
            exploreDirectories();

            try {
                Thread.sleep(systemExplorerSleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void exploreDirectories() {
        for (File directory : directoriesToExplore) {
            exploreDirectory(directory);
        }
    }

    public void exploreDirectory(File directory) {
        if (!directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Exploring file|" + file.getName());
                exploreDirectory(file);
            } else {
                processFile(file);
            }
        }
    }

    private void processFile(File file) {
        long lastModified = file.lastModified();
        if (!firstRun && fileLastModifiedMap.containsKey(file)) {
            if (fileLastModifiedMap.get(file) == lastModified) {
                return;
            }
        }

        fileLastModifiedMap.put(file, lastModified);

        MatrixFileTask matrixFileTask = new MatrixFileTask(file);
        taskQueue.addTask(matrixFileTask);
        System.out.println("Found matrix file|" + file.getName());
    }

    public SystemExplorer setRunning(boolean running) {
        this.running = running;
        return this;
    }

    public void recollectDataFromFile(String argument) {

    }

    public Map<File, Long> getFileLastModifiedMap() {
        return fileLastModifiedMap;
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public File[] getDirectoriesToExplore() {
        return directoriesToExplore;
    }

    public void setDirectoriesToExplore(File[] directoriesToExplore) {
        this.directoriesToExplore = directoriesToExplore;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public long getSystemExplorerSleepTime() {
        return systemExplorerSleepTime;
    }

    public void setSystemExplorerSleepTime(long systemExplorerSleepTime) {
        this.systemExplorerSleepTime = systemExplorerSleepTime;
    }

    public boolean isRunning() {
        return running;
    }
}

