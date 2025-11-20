package org.SwEng;
import org.SwEng.accountSystem.Account;
import org.SwEng.accountSystem.AccountCoordinator;
import org.SwEng.accountSystem.AccountDB;
import org.SwEng.headCoordinatorSystems.HeadCoordinator;
import java.io.File;
import org.SwEng.accountSystem.PaymentSys;
import org.SwEng.headCoordinatorSystems.headCoordinator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public void createAccount() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter new email: ");
    String email = scanner.nextLine();
    System.out.print("Enter new password (at least 6 characters): ");
    String password = scanner.nextLine();

    try {
        // Construct Account object (validation is inside Account.java)
        Account newAccount = new Account(email, password);

        // Load existing accounts from file/database
        AccountCoordinator accountCoordinator = new AccountCoordinator();

        // Check if email already exists
        boolean exists = false;
        for (Account acc : accountCoordinator.getAllAccounts()) {
            if (acc.getEmail().equals(email)) {
                exists = true;
                break;
            }
        }

        if (exists) {
            System.out.println("Email already exists. Please try another.");
            return;
        }

        // Save new account
        AccountDB.saveAccount(newAccount);
        System.out.println("Account created successfully!");

    } catch (IllegalArgumentException e) {
        // Throws from Account.java for invalid email or password
        System.out.println("Error: " + e.getMessage());
    }
}

class Product {
    private int id;
    private String name;
    private double price;
    private int likes;
    private boolean soldOut;

    public Product(int id, String name, double price, boolean soldOut) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.soldOut = soldOut;
        this.likes = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public boolean isSoldOut() {return soldOut;}
    public int getLikes() {return likes;}

    public void likes(){
        likes++;
    }

    @Override
    public String toString() {
        return id + ": " + name + " - $" + price + (soldOut ? "Sold Out" : "") + " Likes: " + likes;
    }
}
class Message{
    private static int idCounter = 1;
    private int id;
    private String from;
    private String content;
    private boolean read;

    public Message(String from, String content){
        this.id = idCounter++;
        this.from = from;
        this.content = content;
        this.read = false;
    }
    public int getId() { return id; }
    public String getFrom() { return from; }
    public String getContent() {return content;}
    public boolean isRead() {return  read;}
    public void markRead() { read = true; }

    @Override
    public String toString() {
        return "Message ID: " + id + " From: " +
                from + " - " + (read ? "[Read] " : "[Unread] ") + content;
    }
}

public class StoreCoordinator {
    private List<Product> productList;
    private List<Product> cart;
    private List<Message> messages;
    private Scanner scanner;
    private String customerEmail;

    public StoreCoordinator() {
        productList = new ArrayList<>();
        cart = new ArrayList<>();
        messages = new ArrayList<>();
        scanner = new Scanner(System.in);
        customerEmail = "customer@gmail.com";
        initProducts();
    }

    private void initProducts() {
        productList.add(new Product(1, "Enterprise", 2000000000.99, false));
        productList.add(new Product(2, "Columbia", 1400000000.99, true));
        productList.add(new Product(3, "Challenger", 1990000000.99, false));
        productList.add(new Product(4, "Discovery", 200000000.99, false));
        productList.add(new Product(5, "Atlantis", 17600000000.99, true));
        productList.add(new Product(6, "Endeavour", 195000000.99, false));
        productList.add(new Product(7, "Falcon 9", 20000000.99, false));
        productList.add(new Product(8, "Starship", 687000000.99, true));
        productList.add(new Product(9, "Mercury", 193200000.99, false));
    }

    private void showProducts() {
        // Sorts the products by descending likes
        Collections.sort(productList, Comparator.comparing(Product::getLikes).reversed());
        System.out.println("Available Products (sorted by likes):");
        for (Product p : productList) {
            System.out.println(p.toString());
        }
    }

    private void showCart() {
        System.out.println("Your Shopping Cart:");
        if (cart.isEmpty()) {
            System.out.println("(empty)");
        } else {
            for (Product p : cart) {
                System.out.println(p.toString());
            }
        }
    }

    private void addToCart(int productId) {
        for (Product p : productList) {
            if (p.getId() == productId) {
                cart.add(p);
                System.out.println(p.getName() + " added to the cart.");
                return;
            }
        }
        System.out.println("Product ID not found.");
    }

    private void likeProduct(int productId) {
        for (Product p : productList) {
            if (p.getId() == productId) {
                p.likes();
                System.out.println("You liked " + p.getName() + ". Total " +
                        "likes: " + p.getLikes());
                return;
            }
            System.out.println("Product ID not found");
        }
        System.out.println("Product ID not found");
    }

    private void purchaseCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Add products to your cart!");
            return;
        }
        System.out.println("Choose payment method: credit or debit");
        String paymentMethod = scanner.nextLine().trim().toLowerCase();
        if (!paymentMethod.equals("credit") && !paymentMethod.equals("debit")){
            System.out.println("Purchasing items: ");
            return;
        }

        double total = 0.0;
        System.out.println("Purchasing items:");
        for(
                Product p :cart)

        {
            System.out.print(p.getName() + "- $" + p.getPrice());
            total += p.getPrice();
        }
        System.out.println("Total: $"+total );
        System.out.println("Payment made by "+paymentMethod +" card. ");

        sendReceipt();
        cart.clear();
    }
    private void sendReceipt(){
        System.out.println("Your receipt has been sent to your email!");
    }
    private void sendMessage() {
        System.out.println("Enter your message to the staff:");
        String content = scanner.nextLine();
        Message msg = new Message("Customer", content);
        messages.add(msg);
        System.out.println("Message sent.");
    }
    private void viewAndReplyMessages() {
        List<Message> unread = new ArrayList<>();
        for (Message m : messages) {
            if (!m.isRead()) {
                unread.add(m);
            }
        }
        if (unread.isEmpty()) {
            System.out.println("No unread messages.");
            return;
        }
        for (Message m : unread) {
            System.out.println(m.toString());
        }
        System.out.println("Enter message ID to reply or 'skip' to exit:");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("skip")) {
            return;
        }
        try {
            int msgId = Integer.parseInt(input);
            Message toReply = null;
            for (Message m : unread) {
                if (m.getId() == msgId) {
                    toReply = m;
                    break;
                }
            }
            if (toReply == null) {
                System.out.println("Message ID not found.");
                return;
            }
            System.out.println("Enter reply message:");
            String replyContent = scanner.nextLine();
            messages.add(new Message("Staff (re: " + toReply.getId() + ")", replyContent));
            toReply.markRead();
            System.out.println("Reply sent.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid message ID.");
        }
    }


    public void run() {
        System.out.println("Welcome to the EMT Rockets!");

        while (true) {
            System.out.println("\nCommands: show, addcart [id], cart, buy, like [id], messages, quit, createaccount");
            System.out.print("Enter command: ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ");

            try {
                switch (parts[0].toLowerCase()) {
                    case "createaccount":
                        createAccount();
                        break;
                    case "show":
                        showProducts();
                        break;
                    case "addcart":
                        if (parts.length == 2) {
                            int id = Integer.parseInt(parts[1]);
                            addToCart(id);
                        } else {
                            System.out.println("Usage: addcart [product_id]");
                        }
                        break;
                    case "cart":
                        showCart();
                        break;
                    case "buy":
                        purchaseCart();
                        break;

                    case "like":
                        if (parts.length == 2) {
                            int id = Integer.parseInt(parts[1]);
                            likeProduct(id);
                        } else {
                            System.out.println("Usage: like [product_id]");
                        }
                        break;
                    case "message":
                        sendMessage();
                        break;
                    case "replymsgs":
                        viewAndReplyMessages();
                        break;
                    case "quit":
                        System.out.println("Exiting. Thank you for shopping!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Unknown command.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }
    }

    public static void main(String[] args) {
        StoreCoordinator app = new StoreCoordinator();
        app.run();
    }
}