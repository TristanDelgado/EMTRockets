package org.SwEng.accountSystem;

import java.util.List;
import java.util.Objects;
import org.SwEng.subsystems.helpers.InternalSystemMessage;

import static org.SwEng.subsystems.helpers.Subsystems.ACCOUNT_SERVICE;
import static org.SwEng.subsystems.helpers.Subsystems.STORE_SERVICE;

public class AccountCoordinator {
    private List<Account> allAccounts;
    private Account currentAccount;
    private Screen curScreen;

    enum Screen {
        loadScreen,
        loginScreen,
        createAccScreen,
    }

    /**
     * Contains all static strings for user-facing output.
     * This centralization makes it easy to edit prompts or add translations later.
     */
    private static class outputStrings {
        // From handleInput
        public static final String MAIN_MENU_PROMPT = "1. Login\n2. Create-Account\nInput: ";
        public static final String PROMPT_EMAIL_COLON = "Input Email\nInput: ";
        public static final String INVALID_OPTION_MAIN_MENU = "Invalid option.\n1.Login\n2.Create-Account\nInput: ";
        public static final String INVALID_EMAIL_FORMAT = "Invalid email format. Input Email\nInput: ";
        public static final String EMAIL_EXISTS_MAIN_MENU = "Email already exists.\n1.Login\n2.Create-Account\nInput: ";
        public static final String GENERIC_ERROR_MAIN_MENU = "Error. Returning to main menu.1. Login\n2. Create-Account\nInput: ";

        // From helper methods (loginToAccount, createAccount)
        public static final String PROMPT_EMAIL_NO_COLON = "Input Email\nInput: ";
        public static final String PROMPT_PASSWORD_NO_COLON = "Input Password\nInput: ";
        public static final String SUCCESS_LOGIN = "Successfully logged in\nInput: ";
        public static final String LOGIN_FAILED_MAIN_MENU = "Incorrect Username or Password.\n1. Login\n2. Create-Account\nInput: ";
        public static final String SUCCESS_ACCOUNT_CREATED = "Account created. Successfully logged in.";
    }

    public AccountCoordinator() {
        allAccounts = AccountDB.loadAccounts();
        currentAccount = null;
        curScreen = Screen.loadScreen;
    }

    // Handle Input
    public InternalSystemMessage handleInput(InternalSystemMessage message) {
        switch (curScreen) {
            case Screen.loadScreen:
                // Could probably change the below code into a switch statement
                if (Objects.equals(message.message, "")) {
                    return (new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.MAIN_MENU_PROMPT));
                } else {
                    // Start the login process
                    if (Objects.equals(message.message, "1")) {
                        curScreen = Screen.loginScreen;
                        // Create an empty account object to store login attempt data
                        currentAccount = new Account("", "");
                        return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.PROMPT_EMAIL_COLON);
                    }
                    // Start the account creation process
                    if (Objects.equals(message.message, "2")) {
                        curScreen = Screen.createAccScreen;
                        // Create an empty account object to store new account data
                        currentAccount = new Account("", "");
                        return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.PROMPT_EMAIL_COLON);
                    }
                    // Handle invalid input
                    return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.INVALID_OPTION_MAIN_MENU);
                }

            case Screen.loginScreen:
                // We are in the process of logging in.
                // Populate the currentAccount object with the user's input.
                if (Objects.equals(currentAccount.getEmail(), "")) {
                    // User just sent their email
                    currentAccount = new Account(message.message, "");
                } else if (Objects.equals(currentAccount.getPassword(), "")) {
                    // User just sent their password
                    currentAccount = new Account(currentAccount.getEmail(), message.message);
                }
                // Call the helper function to check the state and respond
                return loginToAccount();

            case Screen.createAccScreen:
                // We are in the process of creating an account.
                // Populate the currentAccount object.
                if (Objects.equals(currentAccount.getEmail(), "")) {
                    // User just sent their email. Validate it.
                    if (!message.message.contains("@")) {
                        return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.INVALID_EMAIL_FORMAT);
                    }

                    // Check if email already exists
                    for (Account acc : allAccounts) {
                        if (acc.getEmail().equals(message.message)) {
                            currentAccount = null;
                            curScreen = Screen.loadScreen;
                            return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.EMAIL_EXISTS_MAIN_MENU);
                        }
                    }
                    // Email is valid and unique, store it.
                    currentAccount = new Account(message.message, "");
                } else if (Objects.equals(currentAccount.getPassword(), "")) {
                    // User just sent their password.
                    currentAccount = new Account(currentAccount.getEmail(), message.message);
                }
                // Call the helper function to check the state and respond
                return createAccount();

            default:
                // Default case, reset to load screen
                curScreen = Screen.loadScreen;
                return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.GENERIC_ERROR_MAIN_MENU);
        }
    }

    /**
     * Helper method to process login state.
     * This method is called after handleInput has updated the currentAccount object.
     * It checks the state of currentAccount (what info is filled) and responds.
     */
    private InternalSystemMessage loginToAccount() {
        if (Objects.equals(currentAccount.getEmail(), "")) {
            // This case should not be reached if handleInput is correct, but as a safeguard.
            return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.PROMPT_EMAIL_NO_COLON);
        } else if (Objects.equals(currentAccount.getPassword(), "")) {
            // This is the expected state after user has provided email.
            return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.PROMPT_PASSWORD_NO_COLON);
        } else {
            // Both email and password are provided. Check credentials.
            if (allAccounts.contains(currentAccount)) {
                // Login success. Find the *actual* account object to make it current.
                currentAccount = allAccounts.get(allAccounts.indexOf(currentAccount));

                curScreen = Screen.loadScreen; // Reset state machine for the next session
                // Returns store service as a signal you've been logged in
                return new InternalSystemMessage(STORE_SERVICE, outputStrings.SUCCESS_LOGIN);
            } else {
                // Login failure.
                currentAccount = null;
                curScreen = Screen.loadScreen;
                return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.LOGIN_FAILED_MAIN_MENU);
            }
        }
    }

    /**
     * Helper method to process account creation state.
     * This method is called *after* handleInput has updated the currentAccount object.
     * It checks the state of currentAccount (what info is filled) and responds.
     */
    private InternalSystemMessage createAccount() {
        if (Objects.equals(currentAccount.getEmail(), "")) {
            // Safeguard
            return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.PROMPT_EMAIL_NO_COLON);
        } else if (Objects.equals(currentAccount.getPassword(), "")) {
            // This is the expected state after user has provided email.
            return new InternalSystemMessage(ACCOUNT_SERVICE, outputStrings.PROMPT_PASSWORD_NO_COLON);
        } else {
            // Both email and password are provided. Time to create the account.
            AccountDB.saveAccount(currentAccount);
            allAccounts = AccountDB.loadAccounts();
            curScreen = Screen.loadScreen; // Reset state machine
            // Log the user in immediately
            return new InternalSystemMessage(STORE_SERVICE, outputStrings.SUCCESS_ACCOUNT_CREATED);
        }
    }
}