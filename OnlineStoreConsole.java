import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Product {
    private int id;
    private String name;
    private double price;

    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return id + ": " + name + " - $" + price;
    }
}

public class OnlineStoreConsole {
    private List<Product> productList;
    private List<Product> cart;

    public OnlineStoreConsole() {
        productList = new ArrayList<>();
        cart = new ArrayList<>();
        initProducts();
    }

    private void initProducts() {
        productList.add(new Product(1, "Apollo", 2000000000.99));
        productList.add(new Product(2, "Luna", 1400000000.99));
        productList.add(new Product(3, "Atlas", 1990000000.99));
    }

    private void showProducts() {
        System.out.println("Available Products:");
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

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nCommands: show (products), add [id], cart, quit");
            System.out.print("Enter command: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("show")) {
                showProducts();
            } else if (input.startsWith("add")) {
                String[] parts = input.split(" ");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[1]);
                        addToCart(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid product ID format.");
                    }
                } else {
                    System.out.println("Usage: add [product_id]");
                }
            } else if (input.equalsIgnoreCase("cart")) {
                showCart();
            } else if (input.equalsIgnoreCase("quit")) {
                System.out.println("Exiting. Thank you for shopping!");
                break;
            } else {
                System.out.println("Unknown command.");
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        OnlineStoreConsole app = new OnlineStoreConsole();
        app.run();
    }
}
