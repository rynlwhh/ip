package Naega.Storage;

import Naega.NaegaException;
import Naega.Task.Deadline;
import Naega.Task.Event;
import Naega.Task.Task;
import Naega.Task.Todo;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Storage {

    private final String filePath;

    // Define the date-time formatter for consistent formatting and parsing
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
    /**
     * Creates a Storage instance with the specified file path.
     *
     * @param filePath the path to the file where tasks are stored
     */
    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads tasks from the file specified by the file path.
     * Parses each line of the file into a Task object.
     *
     * @return an ArrayList of tasks loaded from the file
     * @throws NaegaException if there is an error reading from the file or if task details are invalid
     */
    public ArrayList<Task> load() throws NaegaException {
        ArrayList<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] taskDetails = line.split(" \\| ");
                Task task = parseTask(taskDetails);
                tasks.add(task);
            }
        } catch (FileNotFoundException e) {
            // No file exists yet, so start with an empty list
        } catch (IOException e) {
            throw new NaegaException("Error reading from file: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Parses a task from an array of task details.
     *
     * @param taskDetails an array containing the details of the task
     * @return the parsed Task object
     * @throws NaegaException if the task type is invalid or if task details are insufficient
     */
    private Task parseTask(String[] taskDetails) throws NaegaException {
        System.out.println("taskDetails length: " + taskDetails.length);
        System.out.println("taskDetails: " + Arrays.toString(taskDetails));

        String taskType = taskDetails[0];
        String description = taskDetails[2];

        switch (taskType) {
            case "T":
                return new Todo(description);
            case "D":
                if (taskDetails.length < 4) {
                    throw new NaegaException("Insufficient details for Deadline task.");
                }
                // Parse deadline with the defined formatter
                LocalDateTime deadline = LocalDateTime.parse(taskDetails[3], FORMATTER);
                return new Deadline(description, deadline);
            case "E":
                if (taskDetails.length < 5) {
                    throw new NaegaException("Insufficient details for Event task.");
                }
                // Parse event start and end times with the defined formatter
                LocalDateTime eventStart = LocalDateTime.parse(taskDetails[3], FORMATTER);
                LocalDateTime eventEnd = LocalDateTime.parse(taskDetails[4], FORMATTER);
                return new Event(description, eventStart, eventEnd);
            default:
                throw new NaegaException("Invalid task type in file.");
        }
    }

    /**
     * Saves the list of tasks to the file specified by the file path.
     *
     * @param tasks the list of tasks to save
     */
    public void save(ArrayList<Task> tasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Task task : tasks) {
                writer.println(task.toSaveFormat());
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}