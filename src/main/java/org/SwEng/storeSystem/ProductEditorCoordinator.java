package org.SwEng.storeSystem;

import org.SwEng.subsystems.general.InternalSystemMessage;
import org.SwEng.subsystems.general.Subsystems;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductEditorCoordinator {
    private Product currentProduct; // Null if creating new, populated if modifying
    private List<Product> productList; //Update once complete.
    private ModificationState state = ModificationState.INIT;

    // Temporary storage for building the new/updated product
    private String tempName;
    private double tempPrice;
    private int tempLikes;
    private int tempInventory;

    /**
     * Entry point. Call this to start the interaction if modifying a product.
     *
     * @param message The empty init message.
     * @param product The product to edit, or NULL to create a new one.
     */
    public InternalSystemMessage manageProduct(InternalSystemMessage message, List<Product> productList, Product product) {
        this.currentProduct = product;
        this.productList = productList;
        return manageProduct(message);
    }

    /**
     * Entry point. Call this to start the interaction if creating a new product.
     *
     * @param message The empty init message.
     */
    public InternalSystemMessage manageProduct(InternalSystemMessage message, List<Product> productList) {
        this.productList = productList;
        return manageProduct(message);
    }

    /**
     * Handles the multi-step product modification/creation process.
     */
    public InternalSystemMessage manageProduct(InternalSystemMessage message) {

        // --- STEP 0: INITIALIZATION ---
        if (state == ModificationState.INIT) {
            state = ModificationState.AWAITING_NAME;
            if (currentProduct != null) {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM,
                        String.format(OutputStrings.EDITING_PRODUCT_INSTRUCTIONS + OutputStrings.FMT_PROMPT_NAME, currentProduct.getName()));
            } else {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.CREATING_PRODUCT_INSTRUCTIONS + OutputStrings.NEW_PROMPT_NAME);
            }
        }

        String input = message.message.trim();

        // --- STEP 1: NAME ---
        if (state == ModificationState.AWAITING_NAME) {
            if (input.isEmpty()) {
                if (currentProduct != null) {
                    this.tempName = currentProduct.getName();
                } else {
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.CREATING_PRODUCT_INSTRUCTIONS + OutputStrings.NEW_PROMPT_NAME);
                }
            } else {
                this.tempName = input;
            }

            // Transition to Price
            state = ModificationState.AWAITING_PRICE;
            if (currentProduct != null) {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM,
                        String.format(OutputStrings.EDITING_PRODUCT_INSTRUCTIONS + OutputStrings.FMT_PROMPT_PRICE, currentProduct.getPrice()));
            } else {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.CREATING_PRODUCT_INSTRUCTIONS + OutputStrings.NEW_PROMPT_PRICE);
            }
        }

        // --- STEP 2: PRICE ---
        if (state == ModificationState.AWAITING_PRICE) {
            try {
                if (input.isEmpty() && currentProduct != null) {
                    this.tempPrice = currentProduct.getPrice();
                } else {
                    this.tempPrice = Double.parseDouble(input);
                }

                // Transition to Likes
                state = ModificationState.AWAITING_LIKES;
                if (currentProduct != null) {
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM,
                            String.format(OutputStrings.EDITING_PRODUCT_INSTRUCTIONS + OutputStrings.FMT_PROMPT_LIKES, currentProduct.getLikes()));
                } else {
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.CREATING_PRODUCT_INSTRUCTIONS + OutputStrings.NEW_PROMPT_LIKES);
                }

            } catch (NumberFormatException e) {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.ERROR_INVALID_NUMBER);
            }
        }

        // --- STEP 3: LIKES ---
        if (state == ModificationState.AWAITING_LIKES) {
            try {
                if (input.isEmpty()) {
                    // If new product, default to 0, otherwise keep old
                    this.tempLikes = (currentProduct != null) ? currentProduct.getLikes() : 0;
                } else {
                    this.tempLikes = Integer.parseInt(input);
                }

                // Transition to Inventory
                state = ModificationState.AWAITING_INVENTORY;
                if (currentProduct != null) {
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM,
                            String.format(OutputStrings.EDITING_PRODUCT_INSTRUCTIONS + OutputStrings.FMT_PROMPT_INVENTORY, currentProduct.getInventoryCount()));
                } else {
                    return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.CREATING_PRODUCT_INSTRUCTIONS + OutputStrings.NEW_PROMPT_INVENTORY);
                }
            } catch (NumberFormatException e) {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.ERROR_INVALID_NUMBER);
            }
        }

        // --- STEP 4: INVENTORY & COMPLETE ---
        if (state == ModificationState.AWAITING_INVENTORY) {
            try {
                if (input.isEmpty() && currentProduct != null) {
                    this.tempInventory = currentProduct.getInventoryCount();
                } else {
                    this.tempInventory = Integer.parseInt(input);
                }

                state = ModificationState.COMPLETE;

            } catch (NumberFormatException e) {
                return new InternalSystemMessage(Subsystems.STORE_SYSTEM, OutputStrings.ERROR_INVALID_NUMBER);
            }
        }

        // --- Product Complete ---
        if (state == ModificationState.COMPLETE) {
            Product finalProduct = null;

            // When we are editing a product
            if (currentProduct != null) {
                finalProduct = new Product(currentProduct.getId(), tempName, tempPrice, tempLikes, tempInventory);
                StoreDB.updateProduct(finalProduct);
                this.productList.remove(currentProduct);
                this.productList.add(finalProduct);
            } else { // When we are creating a new product
                String uniqueId = generateUniqueId(productList);
                finalProduct = new Product(uniqueId, tempName, tempPrice, tempLikes, tempInventory);
                StoreDB.saveProduct(finalProduct);
                this.productList.add(finalProduct);
            }

            this.state = ModificationState.INIT;
            this.currentProduct = null;

            return new InternalSystemMessage(Subsystems.STORE_SYSTEM,
                    String.format(OutputStrings.SUCCESS_MSG, finalProduct.getName()), "DONE");
        }

        return new InternalSystemMessage(Subsystems.STORE_SYSTEM, "Error: Unknown State");
    }

    private String generateUniqueId(List<Product> productList) {
        // 1. Collect all existing IDs into a Set for fast O(1) lookup
        Set<String> existingIds = productList.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        // 2. Iterate from 0 to 999 to find the first available 3-digit ID
        for (int i = 11; i < 1000; i++) {
            // Format as 3 digits with leading zeros (e.g., 5 becomes "005")
            String candidateId = String.format("%03d", i);

            if (!existingIds.contains(candidateId)) {
                return candidateId;
            }
        }

        throw new IllegalStateException("Error: Maximum product limit (1000) reached. Cannot generate new ID.");
    }

    // State machine steps
    enum ModificationState {
        INIT,
        AWAITING_ID,
        AWAITING_NAME,
        AWAITING_PRICE,
        AWAITING_LIKES,
        AWAITING_INVENTORY,
        COMPLETE
    }

    static class OutputStrings {
        // Prompts for existing items being edited
        public static final String EDITING_PRODUCT_INSTRUCTIONS = "=== Editing Product ===\n\nTo edit, type value and hit [Enter].\nTo skip, hit [ENTER].\n\n";
        public static final String FMT_PROMPT_NAME = "Enter Product Name (Current: %s)\nInput: ";
        public static final String FMT_PROMPT_PRICE = "Enter Product Price (Current: %.2f)\nInput: ";
        public static final String FMT_PROMPT_LIKES = "Enter Product Likes (Current: %d)\nInput: ";
        public static final String FMT_PROMPT_INVENTORY = "Enter Inventory Count (Current: %d)\nInput: ";

        // Prompts for new products being created
        public static final String CREATING_PRODUCT_INSTRUCTIONS = "=== Creating Product ===\n\nType value and hit [Enter] to save.\n\n";
        public static final String NEW_PROMPT_NAME = "Enter Product Name: ";
        public static final String NEW_PROMPT_PRICE = "Enter Product Price: ";
        public static final String NEW_PROMPT_LIKES = "Enter Initial Likes (Default 0): ";
        public static final String NEW_PROMPT_INVENTORY = "Enter Initial Inventory: ";

        public static final String ERROR_INVALID_NUMBER = "Invalid number format. Please try again.\nInput: ";
        public static final String SUCCESS_MSG = "Product created and saved successfully: %s\nHit enter to continue.";
    }
}
