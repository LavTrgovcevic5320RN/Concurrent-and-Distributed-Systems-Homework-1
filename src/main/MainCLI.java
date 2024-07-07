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

        } else if (command.startsWith("multiply") && !command.contains("-async")) { // ex. multiply A1,c1  or multiply A4A4,C4
            if(command.contains(" ")) {
                String[] parts = command.split(" ");
                if(parts[1].contains(",")) {
                    String[] matrice = parts[1].split(",");
                    if(matrice.length == 2) {
                        matrixBrain.multiplyMatrices(matrice[0], matrice[1]);
                    } else {
                        System.out.println("There needs to be two matrix names");
                    }
                } else {
                    System.out.println("There needs to be a comma between matrix names");
                }
            } else {
                System.out.println("There needs to be space between command and matrix names");
            }

        } else if (command.startsWith("multiply") && command.contains("-async")) { // ex. multiply A1,c1
//            String[] parts = command.split(" ");
//            String[] matrice = parts[1].split(",");
//            matrixBrain.multiplyMatrices(matrice[0], matrice[1]);
            System.out.println("Async multiply not implemented yet");

        } else if (command.startsWith("save")) { // ex. save -name A1 -file matrix1.rix
            if(command.contains(" ") && command.contains("-name") && command.contains("-file")){
                String[] parts = command.split(" ");
                if(parts.length == 5) {
                    String matrixName = "", fileName = "";
                    if (parts[1].equals("-name")) {
                        matrixName = parts[2];
                        fileName = parts[4];
                    }
                    matrixBrain.saveMatrixToFile(matrixName, fileName);
                } else {
                    System.out.println("Proper format is: save -name mat_name -file file_name");
                }
            } else {
                System.out.println("Proper format is: save -name mat_name -file file_name");
            }

        } else if (command.startsWith("clear") && !command.contains(".")) {
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
                    matrixBrain.clearMatrices(argument.toLowerCase());
                    for(File file : systemExplorer.getFileLastModifiedMap().keySet()) {
                        if(file.getName().toLowerCase().contains(argument.toLowerCase())){
                            System.out.println("Removing file: " + file.getName());
                            systemExplorer.getFileLastModifiedMap().remove(file);
//                            System.out.println("Exploring parent directory: " + file.getParentFile().getName());
                            systemExplorer.exploreDirectory(file.getParentFile());
                        }
                    }
                    System.out.println("Cleared and recollecting data for matrix: " + argument);
                }
            } else {
                System.out.println("Error: Invalid clear command format");
            }

        } else if (command.startsWith("info")) {
            if(command.contains(" ")){
                String[] parts = command.split(" ");
                if(parts[0].equalsIgnoreCase("info")){
                    if (parts.length == 2) {
                        if (parts[1].equalsIgnoreCase("-all")) {
                            matrixBrain.displayAllMatrices();
                        } else if (parts[1].equalsIgnoreCase("-asc")) {
                            System.out.println("Ascending order");
                            matrixBrain.displayMatricesSorted(true);
                        } else if (parts[1].equalsIgnoreCase("-desc")) {
                            System.out.println("Descending order");
                            matrixBrain.displayMatricesSorted(false);
                        } else {
                            matrixBrain.displayMatrixInfo(parts[1]);
                        }
                    } else if(parts.length == 3 && parts[2].matches("\\d+")){
                        int n = Integer.parseInt(parts[2]);
                        if (parts[1].startsWith("-s")) {
                            matrixBrain.displayFirstNMatrices(n);
                        } else if (parts[1].startsWith("-e")) {
                            matrixBrain.displayLastNMatrices(n);
                        }
                    } else {
                        System.out.println("Error: Invalid info command format");
                    }
                } else {
                    System.out.println("You are missing 'info' command");
                }
            }else {
                System.out.println("You are missing a space between command and argument");
            }

        } else if (command.equalsIgnoreCase("help")) {
            System.out.println("Available commands:");
            System.out.println(" - dir dir_name: Add directory to search");
            System.out.println(" - multiply mat1, mat2: Multiply two matrices");
            System.out.println(" - multiply -async mat1, mat2: Multiply two matrices asynchronously");
            System.out.println(" - save -name mat_name -file file_name: Save matrix to file");
            System.out.println(" - clear mat_name: Clear matrix data");
            System.out.println(" - clear dir_name: Clear directory data");
            System.out.println(" - info mat_name: Display matrix information");
            System.out.println(" - info -all: Display information for all matrices");
            System.out.println(" - info -asc: Display information for all matrices in ascending order");
            System.out.println(" - info -desc: Display information for all matrices in descending order");
            System.out.println(" - info -s n: Display information for first n matrices");
            System.out.println(" - info -e n: Display information for last n matrices");
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
