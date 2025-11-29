package org.SwEng.accountSystem;

import org.SwEng.subsystems.general.Account;
import org.SwEng.subsystems.general.AccountType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountDB {
    private static final String ACCOUNTS_FILE_NAME = "accounts.txt";
    private static final String ACCOUNTS_FILE_PATH = "src/main/java/org/SwEng/accountSystem/dataFiles/" + ACCOUNTS_FILE_NAME;

    /**
     * Finds an account by email and either updates the existing account or adds a new one (upsert).
     * The entire file is overwritten with the updated list of accounts.
     * * @param account The Account object to save or update.
     */
    public static void saveAccount(Account account) {
        // 1. Load all existing accounts from the file
        List<Account> accounts = loadAccounts();
        boolean found = false;

        // 2. Try to find and replace the existing account based on email
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getEmail().equalsIgnoreCase(account.getEmail())) {
                accounts.set(i, account); // Replace the old account object with the new one
                found = true;
                break;
            }
        }

        // 3. If no existing account was found, add the new account to the list
        if (!found) {
            accounts.add(account);
        }

        // 4. Overwrite the entire file with the updated list
        saveAllAccounts(accounts);
    }

    /**
     * Private helper method to overwrite the entire database file with the current
     * list of accounts.
     * @param accounts The complete list of accounts to be saved.
     */
    private static void saveAllAccounts(List<Account> accounts) {
        // Use FileWriter with the second parameter set to 'false' to overwrite the file
        try (FileWriter fw = new FileWriter(ACCOUNTS_FILE_PATH, false)) {

            for (Account account : accounts) {
                StringBuilder lineBuilder = new StringBuilder();

                // 1. Email
                String email = account.getEmail();
                lineBuilder.append(email == null ? "" : email).append(",");

                // 2. Password
                String password = account.getPassword();
                lineBuilder.append(password == null ? "" : password).append(",");

                // 3. Account Type
                // Assuming getAccountType() itself is not null, but checking the string value
                String type = (account.getAccountType() != null) ? account.getAccountType().getStringValue() : "";
                lineBuilder.append(type).append(",");

                // 4. Credit Card
                String cc = account.getCreditCardNumber();
                lineBuilder.append(cc == null ? "" : cc).append(",");

                // 5. Debit Card
                String dc = account.getDebitCardNumber();
                lineBuilder.append(dc == null ? "" : dc).append(",");

                // 6. Address
                String address = account.getAddress();
                if (address == null) {
                    address = "";
                } else {
                    // Only replace commas if the address actually exists
                    address = address.replace(",", ":");
                }
                lineBuilder.append(address).append(",");

                // 7. Cart Items (Variable length, added at the end)
                if (account.getItemIdsInCart() != null && !account.getItemIdsInCart().isEmpty()) {
                    // Use String.join to efficiently create the comma-separated list of IDs
                    lineBuilder.append(String.join(",", account.getItemIdsInCart()));
                }

                // Add a newline character
                lineBuilder.append("\n");

                fw.write(lineBuilder.toString());
            }

        } catch (IOException e) {
            System.err.println("Error overwriting account file: " + ACCOUNTS_FILE_PATH);
            e.printStackTrace();
        }
    }

    // Read all accounts (Updated to handle 6 mandatory fields)
    public static List<Account> loadAccounts() {
        List<Account> accounts = new ArrayList<>();
        // Minimum number of mandatory fields is now 6: Email, Pass, Type, CC, DC, Address
        final int MIN_MANDATORY_FIELDS = 6;

        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Use the overloaded split method with limit -1 to capture empty trailing strings
                String[] data = line.split(",", -1);

                // Check for minimum required fields
                if (data.length >= MIN_MANDATORY_FIELDS) {
                    try {
                        String email = data[0].trim();
                        String password = data[1].trim();
                        AccountType type = AccountType.fromString(data[2].trim());

                        // New fields are at indices 3, 4, 5
                        String creditCardNumber = data[3].trim();
                        String debitCardNumber = data[4].trim();
                        String address = data[5].trim();

                        // Collect the variable length item IDs starting from index 6
                        List<String> itemIds = new ArrayList<>();
                        for (int i = MIN_MANDATORY_FIELDS; i < data.length; i++) {
                            String itemId = data[i].trim();
                            // Only add non-empty strings
                            if (!itemId.isEmpty()) {
                                itemIds.add(itemId);
                            }
                        }

                        // Create the new Account object with all 6 mandatory fields and the list of item IDs
                        accounts.add(new Account(email, password, type,
                                creditCardNumber, debitCardNumber, address,
                                itemIds));

                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping malformed account line (Invalid AccountType): " + line);
                    }
                } else {
                    System.err.println("Skipping malformed account line (less than " + MIN_MANDATORY_FIELDS + " required fields): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading account file: " + ACCOUNTS_FILE_PATH);
            // e.printStackTrace(); // Uncomment for detailed error tracing
        }
        return accounts;
    }
}
