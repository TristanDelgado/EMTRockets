package org.SwEng.storeSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StoreDB {

    // Define the file path for product data
    private static final String PRODUCT_FILE_NAME = "products.txt";
    private static final String PRODUCT_FILE_PATH = "src/main/java/org/SwEng/storeSystem/dataFiles/" + PRODUCT_FILE_NAME;

    // Define the file path for sale data
    private static final String SALE_FILE_NAME = "sales.txt";
    private static final String SALE_FILE_PATH = "src/main/java/org/SwEng/storeSystem/dataFiles/" + SALE_FILE_NAME;

    // --- PRODUCT OPERATIONS (WRITE) ---

    /**
     * Saves a single product to the data file.
     * The data is appended to the file in the format: id,name,price,likes,inventoryCount
     *
     * @param product The Product object to save.
     */
    public static void saveProduct(Product product) {
        try (FileWriter fw = new FileWriter(PRODUCT_FILE_PATH, true)) {
            String dataLine = product.getId() + "," +
                    product.getName() + "," +
                    product.getPrice() + "," +
                    product.getLikes() + "," +
                    product.getInventoryCount() + "\n";
            fw.write(dataLine);
        } catch (IOException e) {
            System.err.println("Error saving product: " + product.getName());
            e.printStackTrace();
        }
    }

    /**
     * Overwrites the entire file with a new list of products.
     * Used internally by updateProduct.
     *
     * @param products The complete list of products to save.
     */
    public static void saveAllProducts(List<Product> products) {
        try (FileWriter fw = new FileWriter(PRODUCT_FILE_PATH, false)) {
            for (Product product : products) {
                String dataLine = product.getId() + "," +
                        product.getName() + "," +
                        product.getPrice() + "," +
                        product.getLikes() + "," +
                        product.getInventoryCount() + "\n";
                fw.write(dataLine);
            }
        } catch (IOException e) {
            System.err.println("Error saving all products.");
            e.printStackTrace();
        }
    }

    /**
     * Finds and replaces a product based on its ID.
     *
     * @param updatedProduct The new version of the product to save.
     * @return true if the product was found and updated, false otherwise.
     */
    public static boolean updateProduct(Product updatedProduct) {
        List<Product> products = loadProducts();
        boolean found = false;

        for (int i = 0; i < products.size(); i++) {
            if (Objects.equals(products.get(i).getId(), updatedProduct.getId())) {
                products.set(i, updatedProduct);
                found = true;
                break;
            }
        }

        if (found) {
            saveAllProducts(products); // Overwrite the file with the updated list
        }
        return found;
    }

    // --- SALES OPERATIONS (WRITE) ---

    /**
     * Records a record for all products whose IDs are in the provided list.
     * It efficiently handles the update by first mapping the List<Product> to a
     * Map<String, Product> using the product ID as the key.
     * <p>
     * Records the sales in the form of: ProductName,DateSold\n
     *
     * @param productList  The master list of Product objects.
     * @param purchasedIds The list of product IDs for which inventory should be decremented.
     */
    public static void recordSales(List<Product> productList, List<String> purchasedIds) {
        // 1. Create a map for quick lookups using the product ID as the key.
        Map<String, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        String today = LocalDate.now().toString(); // Format YYYY-MM-DD

        try (FileWriter fw = new FileWriter(SALE_FILE_PATH, true)) {
            for (String id : purchasedIds) {
                fw.write(productMap.get(id).getName() + "," + today + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error recording sales.");
            e.printStackTrace();
        }
    }

    // --- SALES OPERATIONS (READ) ---

    public static String getSalesData() {
        try {
            return new String(Files.readAllBytes(Paths.get(SALE_FILE_PATH)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    // --- PRODUCT OPERATIONS (READ) ---

    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCT_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    try {
                        String id = data[0].trim();
                        String name = data[1].trim();
                        double price = Double.parseDouble(data[2].trim());
                        int likes = Integer.parseInt(data[3].trim());
                        int inventoryCount = Integer.parseInt(data[4].trim());

                        products.add(new Product(id, name, price, likes, inventoryCount));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed product line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Product data file not found or error reading: " + PRODUCT_FILE_PATH);
        }

        return products;
    }
}
