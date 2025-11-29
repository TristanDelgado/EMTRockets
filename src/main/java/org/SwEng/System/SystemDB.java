package org.SwEng.System;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemDB {
    // Define the file path for reports data
    private static final String DAILY_REPORT_FILE_NAME = "dailySalesReports.txt";
    private static final String DAILY_REPORT_PATH = "src/main/java/org/SwEng/System/dataFiles/" + DAILY_REPORT_FILE_NAME;

    private static final String MONTHLY_REPORT_FILE_NAME = "monthlySalesReports.txt";
    private static final String MONTHLY_REPORT_PATH = "src/main/java/org/SwEng/System/dataFiles/" + MONTHLY_REPORT_FILE_NAME;

    // --- REPORT GENERATION OPERATIONS ---

    /**
     * Generates a report for a specific day and appends it to dailySalesReports.txt.
     *
     * @param salesData  A string of all sales data to be used for generating a report.
     * @param targetDate The date to generate the report for (Format: "YYYY-MM-DD").
     */
    public static void generateDailySalesReport(String salesData, String targetDate) {

        String filteredDates = filterSalesByDate(salesData, targetDate);
        writeReport(filteredDates, DAILY_REPORT_PATH, "Daily Report for: " + targetDate);
    }

    /**
     * Generates a report for a specific month and appends it to monthlySalesReports.txt.
     */
    public static void generateMonthlySalesReport(String salesData, String targetDateStr) {
        String filteredDates = filterSalesByMonth(salesData, targetDateStr);
        // TODO: Change the "Monthly Report for: " to only append the month and year.
        writeReport(filteredDates, MONTHLY_REPORT_PATH, "Monthly Report for: " + targetDateStr);
    }

    // --- REPORT RETRIEVAL OPERATIONS ---

    /**
     * Scans the daily reports file and returns a List<String> of all available dates.
     * Output format:
     * YYYY-MM-DD
     */
    public static List<String> getAllDailyReportsByDateOnly() {
        String headerPrefix = "--- Daily Report for: ";
        String headerSuffix = " ---";
        List<String> availableDatesList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DAILY_REPORT_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Check if the line contains the report header signature
                if (line.contains(headerPrefix) && line.contains(headerSuffix)) {

                    // Extract the date between the prefix and suffix
                    int startIndex = line.indexOf(headerPrefix) + headerPrefix.length();
                    int endIndex = line.indexOf(headerSuffix, startIndex);

                    if (endIndex > startIndex) {
                        String date = line.substring(startIndex, endIndex).trim();
                        availableDatesList.add(date);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading daily reports file: " + e.getMessage());
        }

        return availableDatesList;
    }

    /**
     * Retrieves the text of a previously generated daily report.
     *
     * @param targetDate The date to look for (e.g., "2023-11-25").
     * @return The String content of the report if found, otherwise null.
     */
    public static String getDailyReport(String targetDate) {
        String headerSignature = "--- Daily Report for: " + targetDate + " ---";
        return extractReportFromFile(DAILY_REPORT_PATH, headerSignature);
    }

    /**
     * Scans the monthly reports file and returns a List<String> of all available months with reports.
     * Output format:
     * YYYY-MM-DD
     * YYYY-MM-DD
     */
    public static List<String> getAllMonthlyReportsByDateOnly() {
        String headerPrefix = "--- Monthly Report for: ";
        String headerSuffix = " ---";
        List<String> availableDatesList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(MONTHLY_REPORT_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Check if the line contains the report header signature
                if (line.contains(headerPrefix) && line.contains(headerSuffix)) {

                    // Extract the date between the prefix and suffix
                    int startIndex = line.indexOf(headerPrefix) + headerPrefix.length();
                    int endIndex = line.indexOf(headerSuffix, startIndex);

                    if (endIndex > startIndex) {
                        String date = line.substring(startIndex, endIndex).trim();
                        availableDatesList.add(date);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading monthly reports file: " + e.getMessage());
        }

        return availableDatesList;
    }

    /**
     * Retrieves the text of a previously generated monthly report.
     *
     * @param targetMonth The month to look for (e.g., "2023-11").
     * @return The String content of the report if found, otherwise null.
     */
    public static String getMonthlyReport(String targetMonth) {
        String headerSignature = "--- Monthly Report for: " + targetMonth + " ---";
        return extractReportFromFile(MONTHLY_REPORT_PATH, headerSignature);
    }

    // --- HELPER FUNCTIONS ---

    /**
     * Helper to parse the raw filtered sales string, count occurrences by name, and append the report to a file.
     * Use this for both Daily and Monthly reports.
     *
     * @param rawSalesData The string output from filterSalesByDate() containing "Name,Date" lines.
     * @param filePath     The path to the report file (dailySalesReports.txt or monthlySalesReports.txt).
     * @param header       The header title for this specific report (e.g., "Daily Report for: 2025-11-26").
     */
    private static void writeReport(String rawSalesData, String filePath, String header) {
        // 1. Process the raw string into a count map (Name -> Count)
        Map<String, Integer> salesCount = new HashMap<>();

        if (rawSalesData != null && !rawSalesData.isEmpty()) {
            // Split by new line to get individual records
            String[] lines = rawSalesData.split("\\R");

            for (String line : lines) {
                String[] parts = line.split(",");
                // Expecting format: Name,Date
                if (parts.length >= 1) {
                    String productName = parts[0].trim();
                    if (!productName.isEmpty()) {
                        salesCount.put(productName, salesCount.getOrDefault(productName, 0) + 1);
                    }
                }
            }
        }

        // 2. Write the formatted report to the file
        try (FileWriter fw = new FileWriter(filePath, true)) { // Append mode is TRUE
            fw.write("\n" + header + "\n");

            if (salesCount.isEmpty()) {
                fw.write("No sales found for this period.\n");
            } else {
                for (Map.Entry<String, Integer> entry : salesCount.entrySet()) {
                    String name = entry.getKey();
                    int count = entry.getValue();
                    fw.write(name + ": " + count + "\n");
                }
            }
            // The footer is required for the extraction logic to know where a report ends
            fw.write("----------------------------\n");
        } catch (IOException e) {
            System.err.println("Error writing report to: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Scans a report file for a specific header and extracts content until the footer.
     * Returns the full report text block, or null if not found.
     */
    private static String extractReportFromFile(String filePath, String headerSignature) {
        StringBuilder reportContent = new StringBuilder();
        boolean insideTargetBlock = false;
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Check if this line is the start of the report we want
                if (!insideTargetBlock && line.contains(headerSignature)) {
                    insideTargetBlock = true;
                    found = true;
                }

                if (insideTargetBlock) {
                    reportContent.append(line).append("\n");

                    // Check for the footer that signifies the end of the report
                    if (line.trim().equals("----------------------------")) {
                        break; // Stop reading this report
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading report file: " + filePath);
            return null;
        }

        return found ? reportContent.toString() : null;
    }

    /**
     * Filters the raw sales data string and returns only the lines matching the target date.
     * * @param salesData. The complete string of sales data (lines separated by \n).
     *
     * @param targetDate The date to filter by (e.g., "2025-11-05").
     * @return A string containing all sales lines for that specific date.
     */
    public static String filterSalesByDate(String salesData, String targetDate) {
        if (salesData == null || salesData.isEmpty()) {
            return "";
        }

        StringBuilder filteredSales = new StringBuilder();
        // Normalize newlines and split into an array of lines
        String[] lines = salesData.split("\\R");

        for (String line : lines) {
            String[] parts = line.split(",");

            // Ensure the line has valid format: name,dateSold
            if (parts.length == 2) {
                String dateSold = parts[1].trim();

                // Check if the date matches exactly
                if (dateSold.equals(targetDate.trim())) {
                    filteredSales.append(line).append("\n");
                }
            }
        }

        return filteredSales.toString();
    }

    /**
     * Filters the sales data string to return lines where the date falls within the same month/year as the provided date.
     *
     * @param salesData     The complete string of sales data.
     * @param targetDateStr A single date string used to determine the target month (e.g., "2023-11-26" targets November 2023).
     * @return A string containing only the sales lines matching that month and year.
     */
    public static String filterSalesByMonth(String salesData, String targetDateStr) {
        if (salesData == null || salesData.isEmpty()) {
            return "";
        }

        StringBuilder filteredSales = new StringBuilder();

        try {
            // Parse the target date to get the required Year and Month
            LocalDate targetDate = LocalDate.parse(targetDateStr);
            int targetYear = targetDate.getYear();
            var targetMonth = targetDate.getMonth();

            // Split data into lines
            String[] lines = salesData.split("\\R");

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String dateStr = parts[1].trim();

                    try {
                        // Parse the sale date
                        LocalDate saleDate = LocalDate.parse(dateStr);

                        // Check if Year and Month match
                        if (saleDate.getYear() == targetYear && saleDate.getMonth() == targetMonth) {
                            filteredSales.append(line).append("\n");
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Skipping invalid date in sales data: " + dateStr);
                    }
                }
            }
        } catch (DateTimeParseException e) {
            System.err.println("Error: Invalid target date format provided.");
        }

        return filteredSales.toString();
    }
}
