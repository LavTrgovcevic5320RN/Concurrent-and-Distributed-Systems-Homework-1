package main;

import main.matrix.MatrixBrain;
import main.tasks.StopTask;
import main.tasks.TaskCoordinator;
import main.tasks.TaskQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainCLI implements Runnable {

    private static Long sysExplorerSleepTime;
    private static Long maxFileChunkSize;
    private static Integer maxRowsSize;
    private static TaskQueue taskQueue;
    private static TaskCoordinator taskCoordinator;
    private static SystemExplorer systemExplorer;
    private static MatrixBrain matrixBrain;

    public static void main(String[] args) throws InterruptedException {
        readConfiguration();
        taskQueue = new TaskQueue();
        matrixBrain = new MatrixBrain(taskQueue);
        taskCoordinator = new TaskCoordinator(taskQueue, maxFileChunkSize, maxRowsSize, matrixBrain);
        systemExplorer = new SystemExplorer(taskQueue, sysExplorerSleepTime);

        MainCLI mainCLI = new MainCLI();
        Thread coordinatorThread = new Thread(taskCoordinator);
        Thread explorerThread = new Thread(systemExplorer);
        Thread mainThread = new Thread(mainCLI);

        coordinatorThread.start();
        explorerThread.start();
        mainThread.start();

        coordinatorThread.join();
        explorerThread.join();
        mainThread.join();
    }

    @Override
    public void run() {
        System.out.println("MainCLI thread started");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while (true) {
                System.out.print("Enter command: ");
                command = reader.readLine();
                if (command.equalsIgnoreCase("stop")) {
                    System.out.println("Stopping...");
                    taskQueue.addTask(new StopTask());
                    systemExplorer.setRunning(false);
                    break;
                } else {
                    processCommand(command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processCommand(String command) throws InterruptedException, ExecutionException {
        if (command.startsWith("dir")) {
            String[] parts = command.split(" ");
            if (parts.length == 2) {
                String dirName = parts[1];
                File directory = new File(dirName);
                if (directory.isDirectory()) {
                    systemExplorer.addDirectory(directory);
                    System.out.println("Directory added: " + dirName);
                } else {
                    System.out.println("Error: Not a valid directory");
                }
            } else {
                System.out.println("Error: Invalid command format");
            }

        } else if (command.contains("multiply") && !command.contains("-async")) { // ex. multiply A1,c1
            String[] parts = command.split(" ");
            String[] matrice = parts[1].split(",");
            matrixBrain.multiplyMatrices(matrice[0], matrice[1]);

        } else if (command.contains("save") && !command.contains("-async")) { // ex. multiply A1,c1
            String[] parts = command.split(" ");
            String matrixName, fileName;
            if(parts[1].equals("-name")) {
                matrixName = parts[2];
                fileName = parts[4];
            } else {
                matrixName = parts[4];
                fileName = parts[2];
            }
            matrixBrain.saveMatrixToFile(matrixName, fileName);

        } else if (command.startsWith("clear")) {
            String[] parts = command.split(" ");
            if (parts.length == 2) {
                String argument = parts[1];
                File directory = new File(argument);
                if (directory.isDirectory()) {
                    System.out.println("Directory name: " + directory.getName());
                    matrixBrain.clearMatricesFromDir(argument);
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        systemExplorer.getFileLastModifiedMap().remove(file);
                    }

                    systemExplorer.exploreDirectory(directory);
                    System.out.println("Cleared and recollecting data for file: " + argument);
                } else {
                    matrixBrain.clearMatrices(argument);
                    System.out.println("Directory name: " + directory.getName());
                    systemExplorer.exploreDirectory(directory);
                    for(File file : systemExplorer.getFileLastModifiedMap().keySet()) {
                        if(file.getName().contains(argument)){
                            System.out.println("Removing file: " + file.getName());
                            systemExplorer.getFileLastModifiedMap().remove(file);
                        }
                    }
                    System.out.println("Cleared and recollecting data for matrix: " + argument);
                }
            } else {
                System.out.println("Error: Invalid clear command format");
            }
        }else if (command.equalsIgnoreCase("help")) {
            System.out.println("Available commands:");
            System.out.println(" - dir <dir_name>: Add directory to search");
            System.out.println(" - help: Display help information");
            System.out.println(" - exit: Exit the program");

        } else {
            System.out.println("Error: Invalid command. Type 'help' for available commands.");
        }
    }

    private static void readConfiguration() {
        String filename = "app.properties";

        try {
            Map<String, String> dataMap = new HashMap<>();
            Files.lines(Paths.get(filename))
                    .filter(line -> !line.trim().isEmpty() && line.contains("="))
                    .map(line -> line.split("="))
                    .forEach(parts -> dataMap.put(parts[0].trim(), parts[1].trim()));

            sysExplorerSleepTime = Long.valueOf(dataMap.get("sys_explorer_sleep_time"));
            maxFileChunkSize = Long.valueOf(dataMap.get("maximum_file_chunk_size"));
            maxRowsSize = Integer.valueOf(dataMap.get("maximum_rows_size"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
