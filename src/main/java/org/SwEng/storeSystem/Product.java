package main.java.org.SwEng.storeSystem;

public class Product {private int id;
    private String name;
    private double price;

    public Product(int var1, String var2, double var3) {
        this.id = var1;
        this.name = var2;
        this.price = var3;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public double getPrice() {
        return this.price;
    }

    public String toString() {
        return this.id + ": " + this.name + " - $" + this.price;
    }
}
