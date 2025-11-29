package org.SwEng.storeSystem;

/**
 * Represents a single product in the store with basic details and inventory count.
 */
public class Product {
    private final String id;
    private final String name;
    private final double price;
    private int likes;
    private int inventoryCount; // New field for inventory

    /**
     * Constructor for creating a Product object.
     * The file format will now be: id,name,price,likes,inventoryCount
     */
    public Product(String id, String name, double price, int likes, int inventoryCount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.likes = likes;
        this.inventoryCount = inventoryCount;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getLikes() {
        return likes;
    }

    public int getInventoryCount() {
        return inventoryCount;
    }

    // --- Setters ---

    public void setInventoryCount(int inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public void addLike() {
        this.likes++;
    }
}