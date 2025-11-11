package AccountSystem;

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



}
