package org.SwEng.headCoordinatorSystems;

import org.SwEng.accountSystem.AccountCoordinator;
import org.SwEng.subsystems.helpers.InternalSystemMessage;
import org.SwEng.subsystems.helpers.Subsystems;

import java.util.Scanner;
import java.util.stream.Collectors;

// Main Head Coordinator class
public class HeadCoordinator {
    private final Scanner scanner;
    private Subsystems subsystemInCommunication;
    private Boolean run;
    private String outputMessage;

    // System components
    private AccountCoordinator accountCoordinator;

    /**
     * HeadCoordinator constructor.
     */
    public HeadCoordinator() {
        scanner = new Scanner(System.in);
        subsystemInCommunication = Subsystems.ACCOUNT_SERVICE;
        run = true;
        accountCoordinator = new AccountCoordinator();
    }

    /**
     * Processes the inputted command.
     *
     * @param message The user's input to process
     */
    public void processCommand(InternalSystemMessage message) {
        switch (subsystemInCommunication) {
            case ACCOUNT_SERVICE: {
                // TODO: Route message to Account Service and wait for return message to display
                // Responsible for authentication, authorization, profiles, and configuration.
                InternalSystemMessage returnedMessage = accountCoordinator.handleInput(message);
                if (returnedMessage.subsystem == Subsystems.ACCOUNT_SERVICE){
                    outputMessage = returnedMessage.message;
                }
                else {
                    subsystemInCommunication = returnedMessage.subsystem;
                    outputMessage = returnedMessage.message;
                }
                break;
            }

            case MESSAGING_SERVICE: {
                // TODO: Route message to Messaging Service and wait for return message to display
                // Handles internal/external communications (email, push notifications, etc.).
                // Example: messagingService.handleMessage(internalMessage);
                break;
            }

            case SYSTEM_SERVICE: {
                // TODO: Route message to System Service and wait for return message to display
                // Manages logging, configuration, health checks, and scheduled tasks.
                // Example: systemService.handleMessage(internalMessage);
                break;
            }

            case STORE_SERVICE: {
                // TODO: Route message to Store Service and wait for return message to display
                // Manages catalog, inventory, orders, and payment processing.
                // Example: storeService.handleMessage(internalMessage);
                break;
            }

            default: {
                // TODO: Handle unknown or unsupported subsystem
                // Example: log a warning or throw an exception.
                // Example: logger.warn("Unknown subsystem: " + subsystemInCommunicaiton);
                break;
            }
        }
    }

    /**
     * Starts the main application loop for the Head Coordinator.
     */
    public void start() {
        // Initialize the system
        System.out.println("System Started");

        InternalSystemMessage message = new InternalSystemMessage(subsystemInCommunication, "");
        processCommand(message);

        while (run) {

            System.out.print(formatLinesWithSuffix(outputMessage));
            System.out.print("\nInput: ");
            String input = scanner.nextLine();
            message = new InternalSystemMessage(subsystemInCommunication, input);
            processCommand(message);
        }
    }

    /**
     * Takes a string with multiple lines and prefixes a "> " to each line.
     *
     * @param inputString The string to process, (e.g., "Line 1\nLine 2")
     * @return A new string with the formatted lines (e.g., "> Line 1\n> Line 2")
     */
    private static String formatLinesWithSuffix(String inputString) {
        // The .lines() method splits the string by line-ending characters.
        return inputString.lines()
                // .map() transforms each line by adding ">"
                .map(line -> "> " + line)
                // .collect() joins the modified lines back together with "\n"
                .collect(Collectors.joining("\n"));
    }
}