package org.SwEng.accountSystem;


public class PaymentSys {
    private String email;     // link to account
    private double amount;
    private String method;    // e.g. "Credit Card", "Cash", "PayPal"
    private String date;

    public PaymentSys(String email, double amount, String method, String date) {
        this.email = email;
        this.amount = amount;
        this.method = method;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getDate() {
        return date;
    }

    public void displayPayment() {
        System.out.println("Email: " + email);
        System.out.println("Amount: $" + amount);
        System.out.println("Method: " + method);
        System.out.println("Date: " + date);
        System.out.println("----------------------");
    }
}
