package org.SwEng.System;

import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.List;

public class SystemCoordinator {

    private Screen curScreen;

    public SystemCoordinator() {
        this.curScreen = Screen.MAIN_MENU;
    }

    /**
     * Main entry point for the CEO interaction.
     */
    public InternalSystemMessage handleInput(InternalSystemMessage message) {
        return switch (curScreen) {
            case MAIN_MENU -> handleMainMenu(message);
            case DAILY_SELECTION -> handleDailySelection(message);
            case MONTHLY_SELECTION -> handleMonthlySelection(message);
        };
    }

    private InternalSystemMessage handleMainMenu(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();

        // Check if we are just entering this screen (no input or returning from another state)
        if (input.isEmpty()) {
            output.append("Welcome, CEO.\n\nCommands:\n1. View Daily Reports\n2. View Monthly Reports\n3. Exit System\nInput: ");
            return new InternalSystemMessage(Subsystems.SYSTEM, output.toString());
        }

        return switch (input) {
            case "1" -> {
                curScreen = Screen.DAILY_SELECTION;
                // Recursive call to immediately show the next screen's output
                yield handleDailySelection(new InternalSystemMessage(Subsystems.SYSTEM, ""));
                // Recursive call to immediately show the next screen's output
            }
            case "2" -> {
                curScreen = Screen.MONTHLY_SELECTION;
                // Recursive call to immediately show the next screen's output
                yield handleMonthlySelection(new InternalSystemMessage(Subsystems.SYSTEM, ""));
                // Recursive call to immediately show the next screen's output
            }
            case "3" ->
                // Return control to the Account System (Login screen)
                    new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "");
            default -> new InternalSystemMessage(Subsystems.SYSTEM, "Invalid Selection.\nHit [ENTER] to return welcome screen.");
        };
    }

    // --- SCREEN HANDLERS ---

    private InternalSystemMessage handleDailySelection(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String[] parts = message.message.trim().split(" ");

        // 1. Display the list of available dates
        if (parts.length == 0 || parts[0].isEmpty()) {
            List<String> availableDates = SystemDB.getAllDailyReportsByDateOnly();

            output.append("--- Available Daily Reports ---\n");
            if (availableDates.isEmpty()) {
                output.append("No reports found.\n");
            } else {
                for (String date : availableDates) {
                    output.append("- ").append(date).append("\n");
                }
            }
            output.append("Commands:\n1. Select report [YYYY-MM-DD]\n2. Return to welcome screen.\nInput: ");
            return new InternalSystemMessage(Subsystems.SYSTEM, output.toString());
        }

        // 2. Handle User Input
        switch (parts[0]) {
            case "1" :
                if (parts.length == 2) {
                    String report = SystemDB.getDailyReport(parts[1]);
                    if (report != null) {
                        return displayReport(report);
                    } else {
                        return new InternalSystemMessage(Subsystems.SYSTEM, "Report not found.\nHit [ENTER] to return to report selection.");
                    }
                }
                else {
                    return new InternalSystemMessage(Subsystems.SYSTEM, "Error of command usage.\nCorrect Usage: \"1 REPORT_DATE\"\nHit [ENTER] to return to report selection.");
                }

            case "2" :
                curScreen = Screen.MAIN_MENU;
                return handleMainMenu(new InternalSystemMessage(Subsystems.SYSTEM, ""));

            default:
                return new InternalSystemMessage(Subsystems.SYSTEM, "Unknown command.\nHit [Enter] to return to report selection.");
        }
    }

    private InternalSystemMessage handleMonthlySelection(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String[] parts = message.message.trim().split(" ");

        // Display the list of available dates
        if (parts.length == 0 || parts[0].isEmpty()) {
            List<String> availableDates = SystemDB.getAllMonthlyReportsByDateOnly();

            output.append("--- Available Monthly Reports ---\n");
            if (availableDates.isEmpty()) {
                output.append("No reports found.\n");
            } else {
                for (String date : availableDates) {
                    output.append("- ").append(date).append("\n");
                }
            }
            // Logic Match: Updated prompts to match the switch statement commands used in Daily
            output.append("Commands:\n1. Select report [YYYY-MM]\n2. Return to welcome screen.\nInput: ");
            return new InternalSystemMessage(Subsystems.SYSTEM, output.toString());
        }

        // Handle User Input
        switch (parts[0]) {
            case "1" :
                if (parts.length == 2) {
                    // Logic Match: Fetching Monthly report instead of Daily
                    String report = SystemDB.getMonthlyReport(parts[1]);
                    if (report != null) {
                        return displayReport(report);
                    } else {
                        return new InternalSystemMessage(Subsystems.SYSTEM, "Report not found.\nHit [ENTER] to return to report selection.");
                    }
                }
                else {
                    return new InternalSystemMessage(Subsystems.SYSTEM, "Error of command usage.\nCorrect Usage: \"1 REPORT_DATE\"\nHit [ENTER] to return to report selection.");
                }

            case "2" :
                // Logic Match: Return to Main Menu
                curScreen = Screen.MAIN_MENU;
                return handleMainMenu(new InternalSystemMessage(Subsystems.SYSTEM, ""));

            default:
                return new InternalSystemMessage(Subsystems.SYSTEM, "Unknown command.\nHit [Enter] to return to report selection.");
        }
    }

    public void generateDailyReport(String salesData, String targetDate) {
        SystemDB.generateDailySalesReport(salesData, targetDate);
    }

    public void generateMonthlyReport(String salesData, String targetDate) {
        SystemDB.generateMonthlySalesReport(salesData, targetDate);
    }

    // --- REPORT GENERATION HANDLERS ---

    /**
     * Prepares a message to display the report and prompts the user to hit "enter" to return.
     * * @param reportContent The text of the report to display.
     *
     * @return An InternalSystemMessage containing the report and the prompt.
     */
    private InternalSystemMessage displayReport(String reportContent) {

        String output = "================ REPORT VIEW ================\n" +
                reportContent +
                "\n=============================================\n" +
                "Hit enter to return to the previous screen.";

        // Return the message. The Main loop will print this, wait for input,
        // and then call handleInput() again with the user's input (empty string).
        return new InternalSystemMessage(Subsystems.SYSTEM, output);
    }

    // --- REPORT DISPLAY HANDLERS ---

    enum Screen {
        MAIN_MENU,
        DAILY_SELECTION,
        MONTHLY_SELECTION
    }
}