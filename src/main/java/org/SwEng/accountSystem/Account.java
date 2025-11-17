package org.SwEng.accountSystem;

import java.util.Objects;

public class Account {
    private String email;
    private String password;

    public Account(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        if(email==null || !email.contains("@")){
            throw new IllegalArgumentException("Invalid email address");
        }

        this.email = email;
    }

    public void setPassword(String password) {
        if (password ==null || password.length() < 6){
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        this.password = password;
    }

    public void displayInfo() {
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
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

        return Objects.hash(email, password);
    }
}
