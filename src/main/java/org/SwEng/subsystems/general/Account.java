package org.SwEng.subsystems.general;

import java.util.List;
import java.util.Objects;

public class Account {
    private String email;
    private String password;
    private AccountType accountType;
    private String creditCardNumber; // New Field
    private String debitCardNumber;  // New Field
    private String address;          // New Field
    private final List<String> itemIdsInCart;

    // Main Updated Constructor with all fields
    public Account(String email, String password, AccountType accountType,
                   String creditCardNumber, String debitCardNumber, String address,
                   List<String> itemIds) {
        this.email = email;
        this.password = password;
        this.accountType = accountType;
        this.creditCardNumber = creditCardNumber;
        this.debitCardNumber = debitCardNumber;
        this.address = address;
        this.itemIdsInCart = itemIds;
    }

    public Account(String email, String password, AccountType accountType) {
        this.email = email;
        this.password = password;
        this.accountType = accountType;
        this.creditCardNumber = null;
        this.debitCardNumber = null;
        this.address = null;
        this.itemIdsInCart = null;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one symbol");
        }


        this.password = password;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("Account type can't be null");
        }
        this.accountType = accountType;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getDebitCardNumber() {
        return debitCardNumber;
    }

    public void setDebitCardNumber(String debitCardNumber) {
        this.debitCardNumber = debitCardNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getItemIdsInCart() {
        return itemIdsInCart;
    }

    public Boolean isItemInCart(String itemId) {
        return itemIdsInCart.contains(itemId);
    }

    public void addItemToCart(String itemId) {
        itemIdsInCart.add(itemId);
    }

    public void removeItemFromCart(String itemId) {
        itemIdsInCart.remove(itemId);
    }

    public void clearCart() {
        itemIdsInCart.clear();
    }

    @Override
    public boolean equals(Object o) {
        // 1. Check if it's the exact same object
        if (this == o) return true;

        // 2. Check if the other object is null or a different class
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Cast the object to an Account
        Account account = (Account) o;

        // 4. Compare the fields that matter for equality (email and password)
        return Objects.equals(email, account.email) &&
                Objects.equals(password, account.password);
    }

    /**
     * Whenever you override equals, you must override hashCode.
     * This generates a hash based on the same fields.
     */
    @Override
    public int hashCode() {

        return Objects.hash(email, password, accountType);
    }
}
