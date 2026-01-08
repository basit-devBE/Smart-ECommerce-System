package org.commerce.common;

import org.commerce.daos.entities.Product;

import java.util.Comparator;

/**
 * Comparators for sorting Product entities by different criteria.
 * Provides reusable sorting strategies for in-memory product collections.
 */
public class ProductComparator {
    
    /**
     * Sorts products by name (alphabetically, ascending).
     */
    public static final Comparator<Product> BY_NAME = 
        Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER);
    
    /**
     * Sorts products by name (descending).
     */
    public static final Comparator<Product> BY_NAME_DESC = BY_NAME.reversed();
    
    /**
     * Sorts products by price (ascending - lowest first).
     */
    public static final Comparator<Product> BY_PRICE_ASC = 
        Comparator.comparing(Product::getPrice);
    
    /**
     * Sorts products by price (descending - highest first).
     */
    public static final Comparator<Product> BY_PRICE_DESC = BY_PRICE_ASC.reversed();
    
    /**
     * Sorts products by creation date (newest first).
     */
    public static final Comparator<Product> BY_NEWEST = 
        Comparator.comparing(Product::getCreatedAt).reversed();
    
    /**
     * Sorts products by creation date (oldest first).
     */
    public static final Comparator<Product> BY_OLDEST = 
        Comparator.comparing(Product::getCreatedAt);
    
    /**
     * Sorts products by category ID, then by name.
     */
    public static final Comparator<Product> BY_CATEGORY_AND_NAME = 
        Comparator.comparing(Product::getCategoryId)
                  .thenComparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER);
    
    /**
     * Gets a comparator by string identifier.
     * 
     * @param sortBy Sort criteria: "name", "name_desc", "price_asc", "price_desc", "newest", "oldest"
     * @return The appropriate comparator
     */
    public static Comparator<Product> getComparator(String sortBy) {
        if (sortBy == null) {
            return BY_NAME;
        }
        
        return switch (sortBy.toLowerCase()) {
            case "name", "name_asc" -> BY_NAME;
            case "name_desc" -> BY_NAME_DESC;
            case "price", "price_asc" -> BY_PRICE_ASC;
            case "price_desc" -> BY_PRICE_DESC;
            case "newest", "date_desc" -> BY_NEWEST;
            case "oldest", "date_asc" -> BY_OLDEST;
            case "category" -> BY_CATEGORY_AND_NAME;
            default -> BY_NAME;
        };
    }
}
