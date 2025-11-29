package org.SwEng.accountSystem;

import org.SwEng.accountSystem.PaymentSystem.PaymentCoordinator;
import org.SwEng.subsystems.general.Account;
import org.SwEng.subsystems.general.AccountType;
import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.List;
import java.util.Objects;

public class AccountCoordinator {
    // System components
    private final PaymentCoordinator paymentCoordinator;
    // Properties
    private List<Account> allAccounts;
    private Account currentAccount;
    private Screen curScreen;

    public AccountCoordinator() {
        // Initialize properties
        allAccounts = AccountDB.loadAccounts();
        currentAccount = null;
        curScreen = Screen.loadScreen;

        // Initialize account system components
        paymentCoordinator = new PaymentCoordinator();
    }

    public Account getCurrentUserAccount() {
        return currentAccount;
    }

    private Account getAccountByEmail(String email) {
        for (Account account : allAccounts) {
            if (account.getEmail().equals(email)) {
                return account;
            }
        }
        return null;
    }

    public void logoutCurrentUserAccount() {
        currentAccount = null;
    }

    public void saveCurrentUserAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
        AccountDB.saveAccount(currentAccount);
    }

    // Handle User Input
    public InternalSystemMessage handleUserInput(InternalSystemMessage message) {
        switch (curScreen) {
            case Screen.loadScreen:
                switch (message.message) {
                    case (""): //Used to retrieve the basic login main menu
                        logoutCurrentUserAccount();
                        return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.MAIN_MENU_PROMPT);
                    case ("buyCart"): //Prompts the system to start the user's cart buying process
                        curScreen = Screen.buyItemsScreen;
                        return paymentCoordinator.buyCart(message, currentAccount);
                    case ("getUserByEmail"):
                        StringBuilder output = new StringBuilder();
                        Account tempAccount = getAccountByEmail(message.additionalInfo);
                        if (tempAccount != null) {
                            output.append(tempAccount.getEmail()).append(",");
                            output.append(!tempAccount.getAddress().isEmpty() ? tempAccount.getAddress() : "N/A").append(",");
                            output.append(!tempAccount.getCreditCardNumber().isEmpty() ? tempAccount.getCreditCardNumber() : "N/A").append(",");
                            output.append(!tempAccount.getDebitCardNumber().isEmpty() ? tempAccount.getDebitCardNumber() : "N/A").append(",");

                            return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, output.toString(), "MessagingSystemMessage");
                        } else {
                            return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, "", "MessagingSystemMessage");
                        }
                    case "1":
                        curScreen = Screen.loginScreen;
                        // Create an empty account object to store login attempt data
                        currentAccount = new Account("", "", AccountType.CUSTOMER);
                        return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.PROMPT_EMAIL_COLON);
                    case "2":
                        curScreen = Screen.createAccScreen;
                        // Create an empty account object to store new account data
                        currentAccount = new Account("", "", AccountType.CUSTOMER);
                        return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.PROMPT_EMAIL_COLON);
                    default:
                        return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.INVALID_OPTION_MAIN_MENU);
                }

            case Screen.loginScreen:
                // We are in the process of logging in.
                // Populate the currentAccount object with the user's input.
                if (Objects.equals(currentAccount.getEmail(), "")) {
                    // User just sent their email
                    currentAccount = new Account(message.message, "", AccountType.CUSTOMER);
                } else if (Objects.equals(currentAccount.getPassword(), "")) {
                    // User just sent their password
                    currentAccount = new Account(currentAccount.getEmail(), message.message, AccountType.CUSTOMER);
                }
                // Call the helper function to check the state and respond
                return loginToAccount();

            case Screen.createAccScreen:
                // We are in the process of creating an account.
                // Populate the currentAccount object.
                if (Objects.equals(currentAccount.getEmail(), "")) {
                    // User just sent their email. Validate it.
                    if (!message.message.contains("@")) {
                        return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.INVALID_EMAIL_FORMAT);
                    }

                    // Check if email already exists
                    for (Account acc : allAccounts) {
                        if (acc.getEmail().equals(message.message)) {
                            currentAccount = null;
                            curScreen = Screen.loadScreen;
                            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.EMAIL_EXISTS_MAIN_MENU);
                        }
                    }
                    // Email is valid and unique, store it.
                    currentAccount = new Account(message.message, "", AccountType.CUSTOMER);
                } else if (Objects.equals(currentAccount.getPassword(), "")) {
                    // User just sent their password.
                    currentAccount = new Account(currentAccount.getEmail(), message.message, AccountType.CUSTOMER);
                }
                // Call the helper function to check the state and respond
                return createAccount();
            case Screen.buyItemsScreen:
                InternalSystemMessage returnedMessage = paymentCoordinator.buyCart(message);
                if (returnedMessage.subsystem != Subsystems.ACCOUNT_SYSTEM) {
                    curScreen = Screen.loadScreen;
                }
                return returnedMessage;
            default:
                // Default case, reset to load screen
                curScreen = Screen.loadScreen;
                return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.GENERIC_ERROR_MAIN_MENU);
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
            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.PROMPT_EMAIL_NO_COLON);
        } else if (Objects.equals(currentAccount.getPassword(), "")) {
            // This is the expected state after user has provided email.
            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.PROMPT_PASSWORD_NO_COLON);
        } else {
            // Both email and password are provided. Check credentials.
            if (allAccounts.contains(currentAccount)) {
                // Login success. Find the *actual* account object to make it current.
                currentAccount = allAccounts.get(allAccounts.indexOf(currentAccount));

                curScreen = Screen.loadScreen; // Reset state machine for the next session
                if (currentAccount.getAccountType() == AccountType.CEO) {
                    // Login by the CEO which needs to tell the head coordinator to direct the CEO
                    // to communicate with the system
                    return new InternalSystemMessage(Subsystems.SYSTEM, "");
                } else {
                    // Normal login by either a customer or worker
                    // Returns store service as a signal you've been logged in
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "");
                }

            } else {
                // Login failure.
                currentAccount = null;
                curScreen = Screen.loadScreen;
                return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.LOGIN_FAILED_MAIN_MENU);
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
            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.PROMPT_EMAIL_NO_COLON);
        } else if (Objects.equals(currentAccount.getPassword(), "")) {
            // This is the expected state after user has provided email.
            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, outputStrings.PROMPT_PASSWORD_NO_COLON);
        } else {
            // Both email and password are provided. Time to create the account.
//            currentAccount
            AccountDB.saveAccount(currentAccount);
            allAccounts = AccountDB.loadAccounts();
            curScreen = Screen.loadScreen; // Reset state machine
            // Log the user in immediately
            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "");
        }
    }

    enum Screen {
        loadScreen,
        loginScreen,
        createAccScreen,
        buyItemsScreen,
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
        public static final String LOGIN_FAILED_MAIN_MENU = "Incorrect Username or Password.\n1. Login\n2. Create-Account\nInput: ";
    }
}