package org.SwEng.storeSystem;

import org.SwEng.subsystems.general.Account;
import org.SwEng.subsystems.general.AccountType;
import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StoreCoordinator {
    private final List<Product> productList;

    // Dependencies
    ProductEditorCoordinator productEditorCoordinator;
    private Account curUser;
    private Screen curScreen;

    public StoreCoordinator() {
        productList = StoreDB.loadProducts();
        curUser = null;
        curScreen = Screen.storeFrontScreen;
        productEditorCoordinator = new ProductEditorCoordinator();
    }

    public static String getProductsList(List<Product> productList) {
        // Create a mutable copy of the list to sort without modifying the original list
        List<Product> sortedList = new ArrayList<>(productList);

        // Sort the list by 'likes' in descending order
        sortedList.sort(Comparator.comparing(Product::getLikes).reversed());

        StringBuilder sb = new StringBuilder();

        // Add the header line
        sb.append("=== Available Products (sorted by likes) ===\n\n");

        for (Product product : sortedList) {
            // Append the formatted product details
            sb.append(product.getName()).append("\n");
            sb.append("ID: ").append(product.getId()).append("\n");
            sb.append("Price (M$): ").append(product.getPrice()).append("\n");
            sb.append("# Available: ").append(product.getInventoryCount()).append("\n");

            // Append two new lines to separate this product from the next one
            sb.append("\n");
        }

        return sb.toString();
    }

    public static double getProductsCostByIds(List<Product> productList, List<String> validIds) {
        // Use a HashSet for efficient O(1) lookup of valid IDs
        Set<String> validIdSet = new HashSet<>(validIds);

        // 1. Filter the products
        double cost = 0;
        for (Product product : productList) {
            // Check if the product's ID (converted to a String) is in the set of valid IDs
            if (validIdSet.contains(String.valueOf(product.getId()))) {
                cost += product.getPrice();
            }
        }
        return cost;
    }

    /**
     * Filters a list of Product objects based on a list of valid IDs, sorts the
     * matching products by 'likes' in descending order, and formats them into a single string.
     *
     * @param productList The complete list of available Product objects.
     * @param validIds    A List of Strings containing the IDs (e.g., "101", "104") to include in the output.
     * @return A single String containing the formatted details of the filtered and sorted products.
     */
    public static String getProductsByIdsInString(List<Product> productList, List<String> validIds) {
        // Use a HashSet for efficient O(1) lookup of valid IDs
        Set<String> validIdSet = new HashSet<>(validIds);

        // 1. Filter the products
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            // Check if the product's ID (converted to a String) is in the set of valid IDs
            if (validIdSet.contains(String.valueOf(product.getId()))) {
                filteredList.add(product);
            }
        }

        // 2. Sort the filtered list by 'likes' in descending order
        filteredList.sort(Comparator.comparing(Product::getLikes).reversed());

        // 3. Build the final formatted string
        StringBuilder sb = new StringBuilder();

        if (filteredList.isEmpty()) {
            sb.append("\nCart is empty\nReturn to store.\n\n");
        } else {
            sb.append("Filtered Products (sorted by likes):\n\n");

            for (Product product : filteredList) {
                // Append the formatted product details using the required structure
                sb.append(product.getName()).append("\n");
                sb.append("ID: ").append(product.getId()).append("\n");
                sb.append("Price (M$): ").append(product.getPrice()).append("\n");

                // Append a new line to separate this product from the next one
                sb.append("\n");
            }
        }
        // Return the final string, using trim() to remove any trailing newlines/whitespace
        //return sb.toString().trim();
        return sb.toString();
    }

    public InternalSystemMessage handleInput(InternalSystemMessage message) {
        return switch (curScreen) {
            case storeFrontScreen -> (curUser.getAccountType() == AccountType.CUSTOMER)
                    ? handleStoreFrontScreenCustomerInput(message)
                    : handleStoreFrontScreenWorkerInput(message);

            case cartScreen -> handleCartScreenInput(message);
            case productEditorScreen -> handleProductEditorScreenInput(message);
        };
    }

    public InternalSystemMessage handleStoreFrontScreenCustomerInput(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();
        String[] parts = input.split(" ");
        if (parts.length == 0 || parts[0].isEmpty()) {
            //This will be our "initial return point"
            output.append(getProductsList(productList));
            output.append("=============================================\n");
            output.append("Commands:\n1. Add item to cart [id]\n2. View Cart\n3. Like Item [id]\n4. Message Store\n5. Exit Store\nInput: ");
            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
        }

        try {
            switch (parts[0].toLowerCase()) {
                case "1": //Add item to cart - intake second argument
                    if (parts.length == 2) {
                        String id = parts[1];
                        Product product = findProductById(productList, id);
                        if (product.getInventoryCount() > 0) {
                            curUser.addItemToCart(parts[1]);
                            output.append(product.getName()).append(" added to cart.\nHit [ENTER] to continue.");
                        } else {
                            output.append("Out of product: ").append(product.getName()).append("\n");
                            output.append("Hit [ENTER] to return to store front.");
                        }
                        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
                    } else {
                        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Error of command usage.\nCorrect Usage: \"1 ITEM_ID\"");
                    }

                case "2": //View Cart
                    curScreen = Screen.cartScreen;
                    return handleCartScreenInput(new InternalSystemMessage(Subsystems.STORE_SYSTEM, ""));

                case "3": //Like item in store - intake second argument
                    if (parts.length == 2) {
                        String id = parts[1];
                        Product product = findProductById(productList, id);
                        if (product != null) {
                            product.addLike();
                            StoreDB.updateProduct(product);
                            output.append(product.getName()).append(" has been liked.\nHit [ENTER] to return to store.");
                        } else {
                            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "No product with ID " + parts[1] + " was found.\nHit [ENTER] to return to store.");
                        }
                    } else {
                        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Error of command usage.\nCorrect Usage: \"3 ITEM_ID\"");
                    }
                    break;
                case "4": // Message the store
                    return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, "");

                case "5": // Quit out of the store, sign out and reset.
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "");

                default:
                    output.append("Unknown command.\n");
            }
        } catch (NumberFormatException e) {
            output.append("Invalid number format.\n");
        }

        // Stay in STORE_SERVICE after handling the command
        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
    }

    public InternalSystemMessage handleStoreFrontScreenWorkerInput(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();
        String[] parts = input.split(" ");
        if (parts.length == 0 || parts[0].isEmpty()) {
            //This will be our "initial return point"
            output.append(getProductsList(productList));
            output.append("=============================================\n");
            output.append("Commands:\n1. Modify item [id]\n2. Remove item [id]\n3. Add item\n4. Respond to customer messages\n5. Exit Store\nInput: ");
            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
        }

        try {
            switch (parts[0].toLowerCase()) {
                case "1": //Modify item - intake second argument
                    if (parts.length == 2) {
                        curScreen = Screen.productEditorScreen;
                        String id = parts[1];
                        Product product = findProductById(productList, id);
                        return productEditorCoordinator.manageProduct(message, productList, product);
                    } else {
                        output.append("Usage: 1 [product_id]\n");
                    }
                    break;

                case "2": //Remove item - intake second argument
                    if (parts.length == 2) {
                        String id = parts[1];
                        Product product = findProductById(productList, id);
                        if (product != null) {
                            productList.remove(product);
                            StoreDB.saveAllProducts(productList);
                            output.append(product.getName()).append(" has been removed. Hit [ENTER] to continue.");
                        } else {
                            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "No product with ID " + parts[1] + " was found.\nHit [ENTER] to return to store.");
                        }

                    } else {
                        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Error of command usage.\nCorrect Usage: \"2 ITEM_ID\"");
                    }
                    break;

                case "3": //Add item
                    curScreen = Screen.productEditorScreen;
                    return productEditorCoordinator.manageProduct(message, productList);

                case "4": // Respond to customer messages
                    return new InternalSystemMessage(Subsystems.MESSAGING_SYSTEM, "");

                case "5": // Quit out of the store, sign out and reset.
                    output.append("Exiting Store Service. Hit enter to continue.\n");
                    return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "");

                default:
                    output.append("Unknown command.\n");
            }
        } catch (NumberFormatException e) {
            output.append("Invalid number format.\n");
        }

        // Stay in STORE_SERVICE after handling the command
        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
    }

    private InternalSystemMessage handleProductEditorScreenInput(InternalSystemMessage message) {
        InternalSystemMessage returnedMessage = productEditorCoordinator.manageProduct(message);
        if (returnedMessage.additionalInfo != null) {
            curScreen = Screen.storeFrontScreen;
        }
        return returnedMessage;
    }

    public InternalSystemMessage handleCartScreenInput(InternalSystemMessage message) {
        StringBuilder output = new StringBuilder();
        String input = message.message.trim();
        String[] parts = input.split(" ");
        if (parts.length == 0 || parts[0].isEmpty()) {
            // This will output the general cart
            output.append("=== Communicating with cart ===\n");
            output.append(getProductsByIdsInString(productList, curUser.getItemIdsInCart()));
            output.append("=============================================\n");
            output.append("Commands:\n1. Remove item from cart [id]\n2. Purchase Cart\n3. Exit Cart\nInput: ");
            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
        }

        try {
            switch (parts[0].toLowerCase()) {
                case "1": //Remove item from cart - intake second argument
                    // First checks if the user gave a second argument
                    if (parts.length == 2) {
                        // Check if the item is in the cart
                        if (curUser.isItemInCart(parts[1])) {
                            // Below process is to remove an item from the user's cart
                            curUser.removeItemFromCart(parts[1]);
                            String id = parts[1];
                            Product product = findProductById(productList, id);
                            output.append(product.getName()).append(" removed from cart.\nHit [ENTER] to continue.");
                            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
                        } else {
                            // The given item ID was not found in the user's cart
                            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "No product with ID " + parts[1] + " was found in cart.\nHit [ENTER] to return to cart.");
                        }
                    } else {
                        // With no argument given, return instructions for how to use the command.
                        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Error of command usage.\nCorrect Usage: \"1 ITEM_ID\"");
                    }

                case "2": //Buy items in cart
                    if (curUser.getItemIdsInCart().isEmpty()) {
                        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Cart is empty. Unable to checkout.\nHit [ENTER] to return to cart.");
                    } else {

                        //Check if all items in the cart can be purchased
                        // AKA check if the inventory amount for each item is > 1
                        if (removeUnavailableItemsFromCart(productList, curUser.getItemIdsInCart())) {
                            output.append("Items in your cart were no longer available for purchase.\n");
                            output.append("They have been automatically removed.\n");
                            output.append("Hit enter to return to cart.");
                            return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
                        }

                        // Checkout checks are finished. Remaining code is to save and transfer to payment system.
                        curScreen = Screen.storeFrontScreen;

                        //Update all the items in the inventory.txt file and StoreCoordinator
                        decrementCartItems(productList, curUser.getItemIdsInCart());
                        StoreDB.saveAllProducts(productList);

                        //Record the items that have been sold the sales.txt file.
                        StoreDB.recordSales(productList, curUser.getItemIdsInCart());

                        double costOfItems = getProductsCostByIds(productList, curUser.getItemIdsInCart());
                        // Note the "buyCart" message signals the account system to start the cart buying process
                        return new InternalSystemMessage(Subsystems.ACCOUNT_SYSTEM, "buyCart", Double.toString(costOfItems));
                    }

                case "3": //Exit cart
                    curScreen = Screen.storeFrontScreen;
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Successfully exited cart.\nHit [ENTER] to return to store.");

                default:
                    output.append("Unknown command.\nTry again.\nHit [ENTER] to return to cart.");
            }
        } catch (NumberFormatException e) {
            output.append("Invalid number format.\n");
        }

        // Stay in STORE_SERVICE after handling the command
        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, output.toString());
    }

    public void setCurUser(Account user) {
        curUser = user;
    }

    public Account getUserAccount() {
        Account tempUser = curUser;
        curUser = null;
        return tempUser;
    }

    public Boolean isCustomerLoggedIn() {
        return curUser != null;
    }

    private Product findProductById(List<Product> productList, String idToFind) {

        for (Product product : productList) {
            // Check if the current product's ID matches the target ID
            if (Objects.equals(product.getId(), idToFind)) {
                return product; // Found the product, return it immediately
            }
        }

        // If the loop finishes without finding a product, return null
        return null;
    }

    /**
     * Decrements the inventory counts for all products whose IDs are in the provided list.
     * It efficiently handles the update by first mapping the List<Product> to a
     * Map<String, Product> using the product ID as the key.
     *
     * @param productList  The master list of Product objects.
     * @param purchasedIds The list of product IDs for which inventory should be decremented.
     */
    public void decrementCartItems(List<Product> productList, List<String> purchasedIds) {

        // 1. Create a map for quick lookups using the product ID as the key.
        Map<String, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 2. Iterate through the purchased IDs and update the inventory.
        for (String id : purchasedIds) {
            Product product = productMap.get(id);

            // Check if the product exists and has a positive inventory count.
            if (product != null) {
                int currentCount = product.getInventoryCount();

                // Only decrement if the count is greater than zero
                if (currentCount > 0) {
                    product.setInventoryCount(currentCount - 1);
                }
            }
        }
    }

    public String getSalesData() {
        return StoreDB.getSalesData();
    }

    public boolean removeUnavailableItemsFromCart(List<Product> productList, List<String> itemIdsInCart) {
        // 1. Create a Map for O(1) lookup speed.
        // This prevents nested loops, making the check much faster.
        Map<String, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 2. removeIf iterates through the cart and removes items that match the condition.
        // It returns 'true' if any elements were removed, 'false' otherwise.
        return itemIdsInCart.removeIf(id -> {
            Product product = productMap.get(id);

            // Condition A: The product no longer exists in the productList (deleted from store)
            if (product == null) {
                return true; // Remove from cart
            }

            // Condition B: The product exists, but inventory is 0 or less
            return product.getInventoryCount() < 1; // Remove from cart

            // Otherwise, keep the item
        });
    }

    enum Screen {
        storeFrontScreen,
        cartScreen,
        productEditorScreen
    }
}
