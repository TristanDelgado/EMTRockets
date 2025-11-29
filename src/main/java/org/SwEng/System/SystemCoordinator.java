package org.SwEng.System;

import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.List;

public class SystemCoordinator {

    private Screen curScreen;
    // Store the specific report text to display in the view screen
    //private String currentReportView = "";

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
            //case VIEWING_REPORT -> handleReportView(message);
        };
    }

    private InternalSystemMessage handleMainMenu(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();

        // Check if we are just entering this screen (no input or returning from another state)
        if (input.isEmpty()) {
            output.append("Welcome, CEO.\n1. View Daily Reports\n2. View Monthly Reports\n3. Exit System\nInput: ");
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
            default -> new InternalSystemMessage(Subsystems.SYSTEM, "Invalid Selection.\nHit [ENTER] to return to report selection.");
        };
    }

    // --- SCREEN HANDLERS ---

    private InternalSystemMessage handleDailySelection(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();

        // 1. Display the list of available dates
        if (input.isEmpty()) {
            List<String> availableDates = SystemDB.getAllDailyReportsByDateOnly();

            output.append("--- Available Daily Reports ---\n");
            if (availableDates.isEmpty()) {
                output.append("No reports found.\n");
            } else {
                for (String date : availableDates) {
                    output.append("- ").append(date).append("\n");
                }
            }
            output.append("\nEnter the date (YYYY-MM-DD) to view, or '0' to return.\nInput: ");
            return new InternalSystemMessage(Subsystems.SYSTEM, output.toString());
        }

        // 2. Handle User Input
        if (input.equalsIgnoreCase("0")) {
            curScreen = Screen.MAIN_MENU;
            return handleMainMenu(new InternalSystemMessage(Subsystems.SYSTEM, ""));
        }

        // Try to fetch the report from StoreDB
        String report = SystemDB.getDailyReport(input);
        if (report != null) {
            //this.currentReportView = report;
            //this.curScreen = Screen.VIEWING_REPORT;
            return displayReport(report);
        } else {
            return new InternalSystemMessage(Subsystems.SYSTEM, "Report not found. Hit [ENTER] to return to report selection.");
        }
    }

    private InternalSystemMessage handleMonthlySelection(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();

        // 1. Display the list of available dates
        if (input.isEmpty()) {
            List<String> availableDates = SystemDB.getAllMonthlyReportsByDateOnly();

            output.append("--- Available Monthly Reports ---\n");
            if (availableDates.isEmpty()) {
                output.append("No reports found.\n");
            } else {
                for (String date : availableDates) {
                    output.append("- ").append(date).append("\n");
                }
            }
            output.append("\nEnter the date (YYYY-MM-DD) to view, or '0' to return.\nInput: ");
            return new InternalSystemMessage(Subsystems.SYSTEM, output.toString());
        }

        // 2. Handle User Input
        if (input.equalsIgnoreCase("0")) {
            curScreen = Screen.MAIN_MENU;
            return handleMainMenu(new InternalSystemMessage(Subsystems.SYSTEM, ""));
        }

        // Try to fetch the report from StoreDB
        String report = SystemDB.getMonthlyReport(input);
        if (report != null) {
            return displayReport(report);
        } else {
            return new InternalSystemMessage(Subsystems.SYSTEM, "Report not found. Hit [ENTER] to return to report selection.");

        }
    }

    public void generateDailyReport(String salesData, String targetDate) {
        SystemDB.generateDailySalesReport(salesData, targetDate);
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
        MONTHLY_SELECTION//,
        //VIEWING_REPORT
    }
}