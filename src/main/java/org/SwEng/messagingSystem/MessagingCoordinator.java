package org.SwEng.messagingSystem;

import org.SwEng.subsystems.general.Account;
import org.SwEng.subsystems.general.AccountType;
import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.List;

public class MessagingCoordinator {

    private Screen curScreen;
    private Account curAccount;
    private String activeConversationEmail;
    private String[] customerAccountContentsForWorker;

    public MessagingCoordinator() {
        this.curScreen = Screen.INIT;
    }

    /**
     * Setup method to pass the account in before handling input.
     */
    public InternalSystemMessage handleInput(InternalSystemMessage message, Account account) {
        this.curAccount = account;
        return handleInput(message);
    }

    public InternalSystemMessage handleInput(InternalSystemMessage message) {
        return switch (curScreen) {
            case INIT -> handleInit();
            case WORKER_SELECT_USER -> handleWorkerSelection(message);
            case CUSTOMER_CONVERSATION_VIEW -> handleCustomerConversationInput(message);
            case WORKER_CONVERSATION_VIEW -> handleWorkerConversationInput(message);
        };
    }

    private InternalSystemMessage handleInit() {
        // Determine role and direct to appropriate screen
        if (curAccount.getAccountType() == AccountType.CUSTOMER) {
            this.curScreen = Screen.CUSTOMER_CONVERSATION_VIEW;
            // Recursively call to populate the view immediately
            return handleCustomerConversationInput(new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, ""));
        } else {
            this.curScreen = Screen.WORKER_SELECT_USER;
            return handleWorkerSelection(new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, ""));
        }
    }

    // --- SCREEN HANDLERS ---

    private InternalSystemMessage handleCustomerConversationInput(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();

        // 1. Display History (Initial Entry or after a message sent)
        // We trigger display if input is empty (just arrived) OR if we just processed a message.
        // However, the logic below handles "processing" then "re-displaying".

        if (!input.isEmpty()) {
            switch (input) {
                case "1": // Return to store
                    curScreen = Screen.INIT;
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "");
                case "2": // Logout of system
                    curScreen = Screen.INIT;
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "");
                default:
                    MessagingDB.saveCustomerMessage(curAccount, input);
                    output.append(">> Message Sent.\n\n");
            }
        }

        // 2. Build the View
        String history = MessagingDB.getConversationHistory(curAccount.getEmail());
        output.append("=== Conversation with store ===\n");
        output.append(history);
        output.append("\n=============================================\n");
        output.append("Commands:\nNote: To send a message, type your message and hit enter.\n");
        output.append("1. Return to store\n");
        output.append("2. Logout of system\nInput: ");

        return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, output.toString());
    }

    private InternalSystemMessage handleWorkerSelection(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        message.message = message.message.trim();

        // Initially show list of conversations
        if (message.message.isEmpty()) {
            List<String> emails = MessagingDB.getAllCustomerEmailsWithHistory();
            output.append("--- Support Inboxes ---\n");

            if (emails.isEmpty()) {
                output.append("No active conversations found.\n");
            } else {
                for (String email : emails) {
                    output.append("- ").append(email).append("\n");
                }
            }
            // Worker input instructions
            output.append("\nCommands:\nNote: To select a customer, type their email and hit enter.\n");
            output.append("1. Return to store\n");
            output.append("2. Logout of system\nInput: ");
            return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, output.toString());
        }

        // Input before the account system returns with a selected account or no account.
        switch (message.message) {
            case "1": // Return to the store
                curScreen = Screen.INIT;
                this.activeConversationEmail = null;
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "");
            case "2": // Logout of the system
                curScreen = Screen.INIT;
                this.activeConversationEmail = null;
                return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "");
            default:
                // If additionalInfo has a value, then we know it came from the account system
                if (message.additionalInfo == null) {

                    // Request the selected account information from the account system
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "getUserByEmail", message.message);
                } else {
                    // If the returned message.message is empty, then there is no account system
                    if (message.message.isEmpty()) {
                        // TODO: Handle isEmpty() always being false
                        return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, "No customer with that email. Hit enter to return to selection screen.");
                    } else {
                        this.curScreen = Screen.WORKER_CONVERSATION_VIEW;
                        customerAccountContentsForWorker = message.message.split(",");
                        this.activeConversationEmail = customerAccountContentsForWorker[0];
                        return handleWorkerConversationInput(new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, ""));
                    }
                }
        }
    }

    private InternalSystemMessage handleWorkerConversationInput(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();
        
        if (!input.isEmpty()) {
            switch (input) {
                case "1": // Return to the conversation list
                    curScreen = Screen.WORKER_SELECT_USER;
                    this.activeConversationEmail = null;
                    return handleWorkerSelection(new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, ""));
                case "2": // Return to store
                    curScreen = Screen.INIT;
                    this.activeConversationEmail = null;
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "");
                case "3": // Logout of the system
                    curScreen = Screen.INIT;
                    this.activeConversationEmail = null;
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "");
                default:
                    MessagingDB.saveWorkerMessage(activeConversationEmail, input);
                    output.append(">> Message Sent.\n\n");
            }
        }

        // Build the View
        // The "title" of the view
        String history = MessagingDB.getConversationHistory(activeConversationEmail);
        output.append("=== Conversation with: ").append(activeConversationEmail).append(" ===\n");
        output.append(history);
        output.append("\n=============================================\n\n");

        // Showing customer-specific information
        output.append("Email: ").append(this.activeConversationEmail).append("\n");
        output.append("Address: ").append(this.customerAccountContentsForWorker[1].replace(":", ",")).append("\n");
        output.append("Debit Card on file: ").append(this.customerAccountContentsForWorker[2]).append("\n");
        output.append("Credit Card on file: ").append(this.customerAccountContentsForWorker[3]).append("\n");
        output.append("\n=============================================\n");

        // Worker input instructions
        output.append("Commands:\nNote: To send a message, type your message and hit enter.\n");
        output.append("1. Return to the conversation list\n");
        output.append("2. Return to store\n");
        output.append("3. Logout of system\nInput: ");

        return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, output.toString());
    }

    public Boolean isCustomerLoggedIn() {
        return curAccount != null;
    }

    public void setCurUser(Account user) {
        curAccount = user;
    }

    enum Screen {
        INIT,
        WORKER_SELECT_USER,
        CUSTOMER_CONVERSATION_VIEW,
        WORKER_CONVERSATION_VIEW
    }
}
