package org.SwEng;

public class AccountSys {
    private String email;
    private String password;

    public AccountSys(String email, String password) {
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
        this.email = email;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void displayInfo() {
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
    }



}
