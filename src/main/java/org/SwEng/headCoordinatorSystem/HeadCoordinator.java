package org.SwEng.headCoordinatorSystem;

import org.SwEng.System.SystemCoordinator;
import org.SwEng.accountSystem.AccountCoordinator;
import org.SwEng.messagingSystem.MessagingCoordinator;
import org.SwEng.storeSystem.StoreCoordinator;
import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

// Main Head Coordinator class
public class HeadCoordinator {
    private final Scanner scanner;
    // System components
    private final AccountCoordinator accountCoordinator;
    private final StoreCoordinator storeCoordinator;
    private final SystemCoordinator systemCoordinator;
    private final MessagingCoordinator messagingCoordinator;
    private Subsystems subsystemInCommunication;
    private final Boolean run;
    private String outputMessage;

    /**
     * HeadCoordinator constructor.
     */
    public HeadCoordinator() {
        scanner = new Scanner(System.in);
        subsystemInCommunication = Subsystems.ACCOUNT_SYSTEM;
        run = true;
        accountCoordinator = new AccountCoordinator();
        storeCoordinator = new StoreCoordinator();
        systemCoordinator = new SystemCoordinator();
        messagingCoordinator = new MessagingCoordinator();
    }

    /**
     * Prints n lines to "clear" the screen
     */
    private static void clearScreen() {
        for (int index = 0; index < 50; index++) {
            System.out.print("\n");
        }
    }

    /**
     * Processes the inputted command.
     *
     * @param message The user's input to process
     */
    public void processCommand(InternalSystemMessage message) {
        switch (subsystemInCommunication) {
            case ACCOUNT_SYSTEM: {
                // Responsible for authentication, authorization, profiles, and configuration.
                InternalSystemMessage returnedMessage = accountCoordinator.handleUserInput(message);
                if (returnedMessage.subsystem != Subsystems.ACCOUNT_SYSTEM) {
                    subsystemInCommunication = returnedMessage.subsystem;
                    processCommand(returnedMessage);
                    break;
                }
                outputMessage = returnedMessage.message;
                break;
            }

            case MESSAGING_SYSTEM: {
                InternalSystemMessage returnedMessage;
                if (!messagingCoordinator.isCustomerLoggedIn()) {
                    returnedMessage = messagingCoordinator.handleInput(message, accountCoordinator.getCurrentUserAccount());
                }
                else{
                    returnedMessage = messagingCoordinator.handleInput(message);
                }
                if (returnedMessage.subsystem != Subsystems.MESSAGING_SYSTEM) {
                    messagingCoordinator.setCurUser(null);
                    subsystemInCommunication = returnedMessage.subsystem;
                    processCommand(returnedMessage);
                    break;
                }
                outputMessage = returnedMessage.message;
                break;
            }

            case SYSTEM: {
                // Manages logging, configuration, health checks, and scheduled tasks.
                InternalSystemMessage returnedMessage = systemCoordinator.handleInput(message);
                if (returnedMessage.subsystem != Subsystems.SYSTEM) {
                    subsystemInCommunication = returnedMessage.subsystem;
                    processCommand(returnedMessage);
                    break;
                }
                outputMessage = returnedMessage.message;
                break;
            }

            case STORE_SYSTEM: {
                // Manages catalog, inventory, orders, and payment processing.
                if (!storeCoordinator.isCustomerLoggedIn()) {
                    storeCoordinator.setCurUser(accountCoordinator.getCurrentUserAccount());
                }
                InternalSystemMessage returnedMessage = storeCoordinator.handleInput(message);
                if (returnedMessage.subsystem != Subsystems.STORE_SYSTEM) {
                    accountCoordinator.saveCurrentUserAccount(storeCoordinator.getUserAccount());
                    storeCoordinator.setCurUser(null);
                    subsystemInCommunication = returnedMessage.subsystem;
                    processCommand(returnedMessage);
                    break;
                }
                outputMessage = returnedMessage.message;
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

        // Used for generating daily reports automatically
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(20, 55); // 8:55 PM (24-hour format)
        LocalTime end = LocalTime.of(21, 5);   // 9:05 PM (24-hour format)

        InternalSystemMessage message = new InternalSystemMessage(subsystemInCommunication, "");
        processCommand(message);

        while (run) {
            // Clears the screen to make the UI feel reactive
            clearScreen();
            // Checks if we are +-5 minutes from 9 PM, if so generate a report
            if (!now.isBefore(start) && !now.isAfter(end)) {
                systemCoordinator.generateDailyReport(storeCoordinator.getSalesData(), LocalDate.now().toString());
            }
            System.out.print(outputMessage);
            String input = scanner.nextLine();
            message = new InternalSystemMessage(subsystemInCommunication, input);
            processCommand(message);
        }
    }
}