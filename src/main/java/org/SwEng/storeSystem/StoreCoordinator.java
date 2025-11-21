package main.java.org.SwEng.storeSystem;
import org.SwEng.subsystems.helpers.InternalSystemMessage;
import org.SwEng.subsystems.helpers.Subsystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StoreCoordinator {
    private final List<Product> productList;
    private final List<Product> cart;
    private final List<Message> messages;
    private final Scanner scanner;
    private final String customerEmail;

    public StoreCoordinator() {
        productList = new ArrayList<>();
        cart = new ArrayList<>();
        messages = new ArrayList<>();
        scanner = new Scanner(System.in);
        customerEmail = "customer@gmail.com";

    }

    public InternalSystemMessage handleInput(InternalSystemMessage message) {
        String input = message.message.trim();
        String[] parts = input.split(" ");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return new InternalSystemMessage(Subsystems.STORE_SERVICE,
                    "Commands: show, addcart [id], cart, buy, like [id], message, replymsgs, quit, createaccount\n");
        }

        StringBuilder output = new StringBuilder();

        try {
            switch (parts[0].toLowerCase()) {
                case "createaccount":
                    // If you have account creation wired here, call it; otherwise just stub text
                    // createAccount();
                    output.append("Account creation is not yet wired to AccountCoordinator from StoreCoordinator.\n");
                    break;

                case "show":
                    // reuse showProducts() but redirect its prints into output if you refactor,
                    // or for now just call it and return a generic acknowledgment
                    showProducts();
                    output.append("Products listed in console.\n");
                    break;

                case "addcart":
                    if (parts.length == 2) {
                        int id = Integer.parseInt(parts[1]);
                        addToCart(id);
                        output.append("Attempted to add product ").append(id).append(" to cart.\n");
                    } else {
                        output.append("Usage: addcart [product_id]\n");
                    }
                    break;

                case "cart":
                    showCart();
                    output.append("Cart shown in console.\n");
                    break;

                case "buy":
                    purchaseCart();
                    output.append("Purchase process executed.\n");
                    break;

                case "like":
                    if (parts.length == 2) {
                        int id = Integer.parseInt(parts[1]);
                        likeProduct(id);
                        output.append("Attempted to like product ").append(id).append(".\n");
                    } else {
                        output.append("Usage: like [product_id]\n");
                    }
                    break;

                case "message":
                    sendMessage();
                    output.append("Message prompt handled in console.\n");
                    break;

                case "replymsgs":
                    viewAndReplyMessages();
                    output.append("Reply messages flow handled in console.\n");
                    break;

                case "quit":
                    output.append("Exiting Store Service.\n");
                    // Tell HeadCoordinator to go somewhere else if you want:
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SERVICE, output.toString());

                default:
                    output.append("Unknown command.\n");
            }
        } catch (NumberFormatException e) {
            output.append("Invalid number format.\n");
        }

        // Stay in STORE_SERVICE after handling the command
        return new InternalSystemMessage(Subsystems.STORE_SERVICE, output.toString());
    }


    public List<Product> getProductList() {
        return productList;
    }

    public List<Product> getCart() {
        return cart;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}
