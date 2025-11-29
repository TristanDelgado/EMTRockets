package org.SwEng.messagingSystem;

import org.SwEng.subsystems.general.Account;
import org.SwEng.subsystems.general.AccountType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessagingDB {

    // Define the file path for messaging data
    private static final String MESSAGES_FILE_NAME = "messages.txt";
    private static final String MESSAGES_FILE_PATH = "src/main/java/org/SwEng/MessagingSystem/dataFiles/" + MESSAGES_FILE_NAME;

    // --- READ OPERATIONS ---

    /**
     * Retrieves all customer emails that have an existing message history.
     * Used by Workers to select a conversation.
     */
    public static List<String> getAllCustomerEmailsWithHistory() {
        List<String> emails = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MESSAGES_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    emails.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading messages file: " + e.getMessage());
        }
        return emails;
    }

    /**
     * Retrieves the conversation history for a specific customer email.
     * Formats the raw CSV data into a readable string for the Coordinator.
     */
    public static String getConversationHistory(String customerEmail) {
        StringBuilder history = new StringBuilder();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(MESSAGES_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                // Check if this line belongs to the requested customer (Primary Key)
                if (parts.length > 0 && parts[0].trim().equalsIgnoreCase(customerEmail)) {
                    found = true;

                    // Iterate through the parts. Format: email, Source, Msg, Source, Msg...
                    // We start at index 1.
                    for (int i = 1; i < parts.length - 1; i += 2) {
                        String source = parts[i];
                        String msg = parts[i + 1];
                        msg = msg.replace(";",",");

                        history.append("[").append(source).append("]: ").append(msg).append("\n");
                    }
                    break; // Found the user, stop reading
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading messages file.");
        }

        if (!found) {
            return "\nNo previous message history.\n";
        }

        return history.toString();
    }

    // --- WRITE OPERATIONS ---

    /**
     * Saves a new message to the file.
     * If the customer exists, it appends to their line.
     * If the customer is new, it creates a new line.
     */
    public static void saveCustomerMessage(Account account, String message) {
        File file = new File(MESSAGES_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean customerFound = false;

        // 1. Read all existing lines
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading for save: " + e.getMessage());
                return;
            }
        }

        // 2. Modify the specific line or create a new one
        List<String> newLines = new ArrayList<>();
        // Sanitize message to remove commas so it doesn't break the CSV format
        String sanitizedMessage = message.replace(",", ";");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 0 && parts[0].trim().equalsIgnoreCase(account.getEmail())) {
                // Found the customer, append the new message
                String updatedLine = line + "," + account.getAccountType() + "," + sanitizedMessage;
                newLines.add(updatedLine);
                customerFound = true;
            } else {
                newLines.add(line);
            }
        }

        if (!customerFound) {
            // New customer conversation
            String newLine = account.getEmail() + "," + account.getAccountType() + "," + sanitizedMessage;
            newLines.add(newLine);
        }

        // 3. Write everything back to the file
        try (FileWriter fw = new FileWriter(file, false)) { // Overwrite mode
            for (String line : newLines) {
                fw.write(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing message: " + e.getMessage());
        }
    }

    /**
     * Saves a new message to the file.
     * If the customer exists, it appends to their line.
     * If the customer is new, it creates a new line.
     */
    public static void saveWorkerMessage(String customerEmail, String message) {
        File file = new File(MESSAGES_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean customerFound = false;

        // 1. Read all existing lines
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading for save: " + e.getMessage());
                return;
            }
        }

        // 2. Modify the specific line or create a new one
        List<String> newLines = new ArrayList<>();
        // Sanitize message to remove commas so it doesn't break the CSV format
        String sanitizedMessage = message.replace(",", ";");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 0 && parts[0].trim().equalsIgnoreCase(customerEmail)) {
                // Found the customer, append the new message
                String updatedLine = line + "," + AccountType.WORKER + "," + sanitizedMessage;
                newLines.add(updatedLine);
                customerFound = true;
            } else {
                newLines.add(line);
            }
        }

        // If the conversation does not exist, the work is leaving a message
        // for a customer when they log into the conversation subsystem in the future
        if (!customerFound) {
            // New customer conversation
            String newLine = customerEmail + "," + AccountType.WORKER + "," + sanitizedMessage;
            newLines.add(newLine);
        }

        // 3. Write everything back to the file
        try (FileWriter fw = new FileWriter(file, false)) { // Overwrite mode
            for (String line : newLines) {
                fw.write(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing message: " + e.getMessage());
        }
    }
}
