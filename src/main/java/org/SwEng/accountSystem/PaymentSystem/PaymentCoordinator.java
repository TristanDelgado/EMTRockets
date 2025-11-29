package org.SwEng.accountSystem.PaymentSystem;

import org.SwEng.accountSystem.AccountDB;
import org.SwEng.subsystems.general.Account;
import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.Objects;

public class PaymentCoordinator {
    private Account currentAccount;
    private String currentCost;
    private String paymentType; // Stores "Credit" or "Debit"
    private CheckoutState checkoutState = CheckoutState.INIT;

    public InternalSystemMessage buyCart(InternalSystemMessage message, Account curAccount) {
        this.currentAccount = curAccount;
        return buyCart(message);
    }

    /**
     * Handles the multi-step checkout process (buy cart).
     * The process flows: Cost -> Card Type -> Card Number (if missing) -> Address (if missing) -> Complete.
     *
     * @param message The incoming message, containing the cost on the first call, and user input on subsequent calls.
     * @return An InternalSystemMessage prompting the user for the next piece of data or confirming completion.
     */
    public InternalSystemMessage buyCart(InternalSystemMessage message) {
        // --- STEP 0: INITIAL CALL (Receives Cost) ---
        if (checkoutState == CheckoutState.INIT) {
            // The first message content is assumed to contain the cost as a double
            this.currentCost = message.additionalInfo;
            this.checkoutState = CheckoutState.AWAITING_CARD_TYPE;
            // Transition to State 1
            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, OutputStrings.PROMPT_CARD_TYPE);
        }

        // --- STEP 1: PROMPT CARD TYPE / AWAITING_CARD_TYPE ---
        if (checkoutState == CheckoutState.AWAITING_CARD_TYPE) {
            String choice = message.message.toLowerCase();

            if (choice.equals("1")) {
                this.paymentType = "Credit";
                // Check if credit card number is missing
                if (currentAccount.getCreditCardNumber().isEmpty()) {
                    this.checkoutState = CheckoutState.AWAITING_CARD_NUMBER;
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, OutputStrings.PROMPT_CARD_NUMBER);
                }
            } else if (choice.equals("2")) {
                this.paymentType = "Debit";
                // Check if debit card number is missing
                if (currentAccount.getDebitCardNumber().isEmpty()) {
                    this.checkoutState = CheckoutState.AWAITING_CARD_NUMBER;
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, OutputStrings.PROMPT_CARD_NUMBER);
                }
            } else {
                // Invalid choice, re-prompt for card type
                return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, OutputStrings.INVALID_CARD_TYPE);
            }
            // If card exists, skip to address check (fall-through to next check)
        }

        // --- STEP 2: PROMPT CARD NUMBER / AWAITING_CARD_NUMBER ---
        if (checkoutState == CheckoutState.AWAITING_CARD_NUMBER) {
            // Save the new card number to the account object
            if (Objects.equals(paymentType, "Credit")) {
                currentAccount.setCreditCardNumber(message.message);
            } else if (Objects.equals(paymentType, "Debit")) {
                currentAccount.setDebitCardNumber(message.message);
            }
            // Card number is captured, now check the address
            // Fall-through to next check
        }

        // --- STEP 3 & 4: ADDRESS CHECK & INPUT ---
        // This handles the transition from State 1/2 to State 3 (AWAITING_ADDRESS_INPUT)

        // Check if address is missing (regardless of how we got here)
        if (currentAccount.getAddress().isEmpty()) {
            if (checkoutState != CheckoutState.AWAITING_ADDRESS_INPUT) {
                // First time checking, set state and prompt
                this.checkoutState = CheckoutState.AWAITING_ADDRESS_INPUT;
                return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, OutputStrings.PROMPT_ADDRESS);
            } else {
                // User has provided the address input
                // The input uses colons, replace them with commas for consistency in storage
                currentAccount.setAddress(message.message);
                checkoutState = CheckoutState.COMPLETE;
                // Address is captured, fall-through to completion
            }
        } else if (checkoutState != CheckoutState.COMPLETE) {
            checkoutState = CheckoutState.COMPLETE;
        } else {
            checkoutState = CheckoutState.POST_CHECKOUT;
        }

        // Step 5 Checkout Complete
        if (checkoutState == CheckoutState.COMPLETE) {
            String output = "You have successfully checked out, your total was " + this.currentCost +
                    " million dollars.\nA receipt has been sent to your email on file: " + this.currentAccount.getEmail() +
                    "\nYour items will be delivered to: " + this.currentAccount.getAddress().replace(":", ",") +
                    ".\nHit [ENTER] to return to store.";

            // Clear the customer's cart
            currentAccount.clearCart();

            // Save updated account info
            AccountDB.saveAccount(currentAccount);

            return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, output);
        }

        // Everything has been fallen through and therefore checkout is complete
        checkoutState = CheckoutState.INIT;
        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "");
    }

    // State machine steps for the checkout process
    enum CheckoutState {
        INIT,                     // State 0: Initial call, cost received.
        AWAITING_CARD_TYPE,       // State 1: Waiting for 'Credit' or 'Debit'.
        AWAITING_CARD_NUMBER,     // State 2: Waiting for card number input.
        AWAITING_ADDRESS_INPUT,   // State 3: Waiting for address input.
        COMPLETE,                  // State 4: Checkout finished.
        POST_CHECKOUT
    }

    static class OutputStrings {
        public static final String PROMPT_CARD_TYPE = "Checkout with\n1. Credit Card\n2. Debit Card\nInput: ";
        public static final String PROMPT_CARD_NUMBER = "Please enter your card number.\nInput: ";
        public static final String PROMPT_ADDRESS = "Please enter your shipping address.\nInput: ";
        public static final String INVALID_CARD_TYPE = "Invalid Card Type. Checkout with\n1. Credit Card\n2. Debit Card\nInput: ";
    }
}
