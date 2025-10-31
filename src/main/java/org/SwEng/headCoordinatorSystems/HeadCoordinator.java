package org.SwEng.headCoordinatorSystems;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.SwEng.subsystems.helpers.Subsystems;

// Main Head Coordinator class
public class HeadCoordinator {
    private Map<String, CommandHandler> handlers;
    private Scanner scanner;
    private Subsystems subsystemInCommunicaiton;

    /**
     * HeadCoordinator constructor.
     */
    public HeadCoordinator() {
        handlers = new HashMap<>();
        scanner = new Scanner(System.in);
        subsystemInCommunicaiton = Subsystems.ACCOUNT_SERVICE;
        initializeHandlers();
    }

    /**
     * Each handler must be initialized.
     */
    private void initializeHandlers() {
        // Register all command handlers
        registerHandler(new CmdHandlers.HelpHandler(handlers));
    }

    /**
     * Each handler must also be registered with the coordinator.
     *
     * @param handler The handler to register with the coordinator.
     */
    private void registerHandler(CommandHandler handler) {
        handlers.put(handler.getCommandName().toLowerCase(), handler);
    }

    /**
     * Processes the inputted command.
     *
     * @param input The user's input to process
     */
    public void processCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        // TODO: I don't think we need a lot of this but it is here if we want it later
//        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
//
//        CommandHandler handler = handlers.get(command);
//
//        if (handler != null) {
//            try {
//                handler.handle(args);
//            } catch (Exception e) {
//                System.err.println("Error executing command: " + e.getMessage());
//            }
//        } else {
//            System.out.println("Unknown command: " + command);
//            System.out.println("Type 'help' for available commands.");
//        }

        switch (subsystemInCommunicaiton) {
            case ACCOUNT_SERVICE: {
                // TODO: Route message to Account Service and wait for return message to display
                // Responsible for authentication, authorization, profiles, and configuration.
                // Example: accountService.handleMessage(internalMessage);
                System.out.println("User input: " + command);
                System.out.println("In Account Service");
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
     * <p>Initializes the user interface by printing the welcome message and main menu options,
     * then enters an infinite loop to read user input and delegate command processing.</p>
     */
    public void start() {
        System.out.println("Head Coordinator Started");
        System.out.println("Type 'help' or '3' for available commands\n");
        System.out.println("1. Login\n2. Create Account\n3. Help\n4. Quit");
        // I'm thinking we get the initial screen from the account manager.

        //Also for each screen, we have its options say 1-5 and then option 6 and 7 ;) will be filtered out for
        //help and exiting the current screen

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            processCommand(input);
        }
    }
}