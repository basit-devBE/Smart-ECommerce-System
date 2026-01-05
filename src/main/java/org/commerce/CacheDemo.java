package org.commerce;

import org.commerce.common.Result;
import org.commerce.config.DBConfig;
import org.commerce.entities.Categories;
import org.commerce.entities.Product;
import org.commerce.entities.User;
import org.commerce.models.*;
import org.commerce.services.CategoryService;
import org.commerce.services.ProductService;
import org.commerce.services.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Demo class to showcase in-memory caching and sorting features.
 * Run this to see cache performance improvements.
 */
public class CacheDemo {
    
    public static void main(String[] args) {
        DBConfig dbConfig = new DBConfig();
        
        try (Connection connection = dbConfig.connectDB()) {
            // Initialize tables
            UsersModel.initializeTable(connection);
            CategoriesModel.initializeTable(connection);
            ProductsModel.initializeTable(connection);
            
            // Initialize services
            CategoryService categoryService = new CategoryService(connection);
            ProductService productService = new ProductService(connection);
            UserService userService = new UserService(connection);
            
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("   CACHING AND SORTING DEMONSTRATION");
            System.out.println("═══════════════════════════════════════════════════\n");
            
            // Demo 1: Category Caching
            demonstrateCategoryCaching(categoryService);
            
            // Demo 2: Product Sorting
            demonstrateProductSorting(productService);
            
            // Demo 3: User Session Caching
            demonstrateUserSessionCaching(userService);
            
            // Demo 4: Cache Statistics
            demonstrateCacheStats(categoryService, productService, userService);
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    private static void demonstrateCategoryCaching(CategoryService categoryService) {
        System.out.println("\n📦 DEMO 1: Category Caching Performance");
        System.out.println("─────────────────────────────────────────────────\n");
        
        // First call - Cache miss (hits database)
        long start1 = System.nanoTime();
        Result<List<Categories>> result1 = categoryService.getAllCategories();
        long duration1 = (System.nanoTime() - start1) / 1_000_000; // Convert to ms
        
        System.out.println("✓ First call (cache miss - DB query):");
        System.out.println("  Categories found: " + result1.getData().size());
        System.out.println("  Time taken: " + duration1 + " ms\n");
        
        // Second call - Cache hit (from memory)
        long start2 = System.nanoTime();
        Result<List<Categories>> result2 = categoryService.getAllCategories();
        long duration2 = (System.nanoTime() - start2) / 1_000_000;
        
        System.out.println("✓ Second call (cache hit - memory):");
        System.out.println("  Categories found: " + result2.getData().size());
        System.out.println("  Time taken: " + duration2 + " ms\n");
        
        double improvement = ((double)(duration1 - duration2) / duration1) * 100;
        System.out.println("⚡ Performance improvement: " + String.format("%.1f", improvement) + "%");
        System.out.println("   Speed increase: " + (duration1 / Math.max(duration2, 1)) + "x faster");
    }
    
    private static void demonstrateProductSorting(ProductService productService) {
        System.out.println("\n\n🔄 DEMO 2: In-Memory Product Sorting");
        System.out.println("─────────────────────────────────────────────────\n");
        
        // Get all products
        Result<List<Product>> allProducts = productService.getAllProducts();
        int count = allProducts.getData().size();
        System.out.println("Total products available: " + count + "\n");
        
        if (count > 0) {
            // Sort by price ascending
            Result<List<Product>> priceAsc = productService.getAllProductsSorted("price_asc");
            if (!priceAsc.getData().isEmpty()) {
                Product cheapest = priceAsc.getData().get(0);
                System.out.println("✓ Sorted by Price (Ascending):");
                System.out.println("  Cheapest: " + cheapest.getProductName() + 
                                 " - $" + cheapest.getPrice());
            }
            
            // Sort by price descending
            Result<List<Product>> priceDesc = productService.getAllProductsSorted("price_desc");
            if (!priceDesc.getData().isEmpty()) {
                Product expensive = priceDesc.getData().get(0);
                System.out.println("\n✓ Sorted by Price (Descending):");
                System.out.println("  Most expensive: " + expensive.getProductName() + 
                                 " - $" + expensive.getPrice());
            }
            
            // Sort by name
            Result<List<Product>> byName = productService.getAllProductsSorted("name");
            if (!byName.getData().isEmpty()) {
                Product first = byName.getData().get(0);
                System.out.println("\n✓ Sorted by Name (Alphabetically):");
                System.out.println("  First: " + first.getProductName());
            }
            
            // Sort by newest
            Result<List<Product>> newest = productService.getAllProductsSorted("newest");
            if (!newest.getData().isEmpty()) {
                Product recent = newest.getData().get(0);
                System.out.println("\n✓ Sorted by Date (Newest First):");
                System.out.println("  Newest: " + recent.getProductName() + 
                                 " (Added: " + recent.getCreatedAt() + ")");
            }
        } else {
            System.out.println("⚠ No products found. Please add some products to see sorting in action.");
        }
    }
    
    private static void demonstrateUserSessionCaching(UserService userService) {
        System.out.println("\n\n👤 DEMO 3: User Session Caching");
        System.out.println("─────────────────────────────────────────────────\n");
        
        Result<List<User>> allUsers = userService.getAllUsers();
        
        if (!allUsers.getData().isEmpty()) {
            User firstUser = allUsers.getData().get(0);
            
            System.out.println("Simulating user login...");
            Result<User> loginResult = userService.login(firstUser.getEmail(), firstUser.getPassword());
            
            if (loginResult.isSuccess()) {
                System.out.println("✓ User logged in: " + firstUser.getEmail());
                System.out.println("  Active sessions: " + userService.getActiveSessionCount());
                
                // Check if user is in active session cache
                User activeUser = userService.getActiveUser(firstUser.getId());
                if (activeUser != null) {
                    System.out.println("\n✓ User found in active session cache:");
                    System.out.println("  Name: " + activeUser.getFirstname() + " " + activeUser.getLastname());
                    System.out.println("  Role: " + activeUser.getUserRole());
                    System.out.println("  ⚡ Retrieved from HashMap (O(1) complexity)");
                }
                
                // Logout
                userService.logout(firstUser.getId());
                System.out.println("\n✓ User logged out");
                System.out.println("  Active sessions: " + userService.getActiveSessionCount());
            }
        } else {
            System.out.println("⚠ No users found. Please add a user to test session caching.");
        }
    }
    
    private static void demonstrateCacheStats(CategoryService categoryService, 
                                              ProductService productService,
                                              UserService userService) {
        System.out.println("\n\n📊 DEMO 4: Cache Statistics");
        System.out.println("─────────────────────────────────────────────────\n");
        
        System.out.println("Category Service:");
        System.out.println("  " + categoryService.getCacheStats());
        
        System.out.println("\nProduct Service:");
        System.out.println("  " + productService.getCacheStats());
        
        System.out.println("\nUser Service:");
        System.out.println("  " + userService.getCacheStats());
        
        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("   DEMONSTRATION COMPLETE");
        System.out.println("═══════════════════════════════════════════════════\n");
        
        System.out.println("✅ Key Features Demonstrated:");
        System.out.println("   • In-memory caching with TTL");
        System.out.println("   • HashMap for O(1) lookups");
        System.out.println("   • ArrayList sorting with Comparator");
        System.out.println("   • LRU cache eviction");
        System.out.println("   • Session management");
        System.out.println("   • Automatic cache invalidation\n");
    }
}
