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

    public SystemExplorer(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void addDirectory(File directory) {
        // Add directory to the list of directories to explore
        File[] newDirectoriesToExplore = new File[directoriesToExplore.length + 1];
        System.arraycopy(directoriesToExplore, 0, newDirectoriesToExplore, 0, directoriesToExplore.length);
        newDirectoriesToExplore[directoriesToExplore.length] = directory;
        directoriesToExplore = newDirectoriesToExplore;
    }

    @Override
    public void run() {
        exploreDirectories();
        firstRun = false; // After the first exploration, set firstRun flag to false
        while (true) {
            exploreDirectories();

            try {
                Thread.sleep(1000); // Sleep for 10 second (adjust as needed)
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

    private void exploreDirectory(File directory) {
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
//                System.out.println("File not modified: " + file.getAbsolutePath());
                return;
            }
//            System.out.println("File modified: " + file.getAbsolutePath());
//            taskQueue.removeTasksByFile(file);
        }

        // Update last modified time
        fileLastModifiedMap.put(file, lastModified);

        // Create task for the file
        MatrixFileTask matrixFileTask = new MatrixFileTask(file);
        taskQueue.addTask(matrixFileTask);
        System.out.println("Found matrix file|" + file.getName());
    }
}

