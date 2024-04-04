package main;

import main.matrix.MatrixBrain;
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

public class MainCLI implements Runnable {

    private static Integer sysExplorerSleepTime;
    private static Integer maxFileChunkSize;
    private static Integer maxRowsSize;
    private static TaskQueue taskQueue;
    private static TaskCoordinator taskCoordinator;
    private static SystemExplorer systemExplorer;
    private static MatrixBrain matrixBrain;
//    ExecutorService es = Executors.newFixedThreadPool(1);

    public static void main(String[] args) {
        readConfiguration();
        taskQueue = new TaskQueue();
        taskCoordinator = new TaskCoordinator(taskQueue, maxFileChunkSize, maxRowsSize);
        systemExplorer = new SystemExplorer(taskQueue);
        matrixBrain = new MatrixBrain(taskQueue);

        Thread coordinatorThread = new Thread(taskCoordinator);
        Thread explorerThread = new Thread(systemExplorer);
        MainCLI mainCLI = new MainCLI();
        Thread mainThread = new Thread(mainCLI);

        coordinatorThread.start();
        explorerThread.start();
        mainThread.start();
//        taskQueue = new TaskQueue();
//        taskCoordinator = new TaskCoordinator(taskQueue);
//        systemExplorer = new SystemExplorer(taskQueue);
//        matrixBrain = new MatrixBrain(taskQueue);
//        readConfiguration();
//
//        Thread coordinatorThread = new Thread(taskCoordinator);
//        Thread explorerThread = new Thread(systemExplorer);
//
//        coordinatorThread.start();
//        explorerThread.start();
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
//            String command;
//            while (true) {
//                System.out.print("Enter command: ");
//                command = reader.readLine();
//                if (command.equalsIgnoreCase("stop")) {
//                    System.out.println("Exiting program. Goodbye!");
//                    break;
//                } else {
//                    processCommand(command);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
                    System.out.println("Exiting program. Goodbye!");
                    break;
                } else {
                    processCommand(command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processCommand(String command) {
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

        } else if (command.contains("multiply")) { // ex. multiply A1,c1
            String[] parts = command.split(" ");
            String[] matrice = parts[1].split(",");
            matrixBrain.multiplyMatrices(matrice[0], matrice[1]);

        } else if (command.equalsIgnoreCase("help")) {
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

            sysExplorerSleepTime = Integer.valueOf(dataMap.get("sys_explorer_sleep_time"));
            maxFileChunkSize = Integer.valueOf(dataMap.get("maximum_file_chunk_size"));
            maxRowsSize = Integer.valueOf(dataMap.get("maximum_rows_size"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
