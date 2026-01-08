package org.commerce;

import org.commerce.common.Result;
import org.commerce.config.DBConfig;
import org.commerce.daos.entities.User;
import org.commerce.daos.entities.Product;
import org.commerce.daos.entities.Categories;
import org.commerce.daos.entities.Inventory;
import org.commerce.enums.UserRole;
import org.commerce.daos.models.*;
import org.commerce.services.UserService;
import org.commerce.services.ProductService;
import org.commerce.services.CategoryService;
import org.commerce.services.InventoryService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static ProductService productService;
    private static CategoryService categoryService;
    private static InventoryService inventoryService;
    private static User currentUser = null;

    public static void main(String[] args) {
        DBConfig dbConfig = new DBConfig();
        try (Connection connection = dbConfig.connectDB()) {
            System.out.println("Database connected successfully.\n");
            
            userService = new UserService(connection);
            productService = new ProductService(connection);
            categoryService = new CategoryService(connection);
            inventoryService = new InventoryService(connection);
            UsersModel.initializeTable(connection);
            CategoriesModel.initializeTable(connection);
            ProductsModel.initializeTable(connection);
            InventoryModel.initializeTable(connection);
            OrdersModel.initializeTable(connection);
            OrderItemsModel.initializeTable(connection);
            ReviewsModel.initializeTable(connection);

            seedUser();

            // Login first
            if(!loginForm()){
                System.out.println("Login failed. Exiting...");
                return;
            }
            
            boolean running = true;
            while(running){
                System.out.println("\n=== Admin Menu ===");
                System.out.println("Logged in as: " + currentUser.getFirstname() + " (" + currentUser.getUserRole() + ")");
                System.out.println("\n--- User Management ---");
                System.out.println("1. Create User");
                System.out.println("2. Delete User");
                System.out.println("3. Edit User");
                System.out.println("4. View User by ID");
                System.out.println("5. View All Users");
                System.out.println("\n--- Product Management ---");
                System.out.println("6. Create Product");
                System.out.println("7. Create Category");
                System.out.println("8. Create Inventory");
                System.out.println("9. View All Products");
                System.out.println("\n10. Logout");
                System.out.print("\nChoose option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch(choice){
                    case 1 -> createUserForm();
                    case 2 -> deleteUserForm();
                    case 3 -> editUserForm();
                    case 4 -> viewUserForm();
                    case 5 -> viewAllUsersForm();
                    case 6 -> createProductForm();
                    case 7 -> createCategoryForm();
                    case 8 -> createInventoryForm();
                    case 9 -> viewAllProductsForm();
                    case 10 -> {
                        currentUser = null;
                        running = false;
                        System.out.println("Logged out successfully!");
                    }
                    default -> System.out.println("Invalid option");
                }
            }
            
            System.out.println("Goodbye!");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
    
    private static void createUserForm(){
        System.out.println("\n--- Create New User ---");
        
        System.out.print("First Name: ");
        String firstname = scanner.nextLine();
        
        System.out.print("Last Name: ");
        String lastname = scanner.nextLine();
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Role (CUSTOMER/SELLER/ADMIN): ");
        String roleStr = scanner.nextLine().toUpperCase();
        
        User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        
        try{
            user.setUserRole(UserRole.valueOf(roleStr));
        }catch(IllegalArgumentException e){
            user.setUserRole(UserRole.CUSTOMER);
            System.out.println("Invalid role, defaulting to CUSTOMER");
        }
        
        Result<User> result = userService.createUser(user);
        
        if(result.isSuccess()){
            User created = result.getData();
            System.out.println("\n✓ User created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getFirstname() + " " + created.getLastname());
            System.out.println("Email: " + created.getEmail());
        }else{
            System.out.println("\n✗ Failed to create user: " + result.getMessage());
        }
    }
    
    private static void deleteUserForm(){
        System.out.println("\n--- Delete User ---");
        
        System.out.print("Enter User ID to delete: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        
        Result<Boolean> deleted = userService.deleteUser(userId);
        
        if(deleted.isSuccess()){
            System.out.println("\n✓ User deleted successfully!");
        }else{
            System.out.println("\n✗ Failed to delete user: " + deleted.getMessage());
        }
    }
    
    private static void editUserForm(){
        System.out.println("\n--- Edit User ---");
        
        System.out.print("Enter User ID to edit: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        
        System.out.println("\nLeave blank to keep current value");
        
        System.out.print("First Name: ");
        String firstname = scanner.nextLine();
        
        System.out.print("Last Name: ");
        String lastname = scanner.nextLine();
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Password (min 6 chars): ");
        String password = scanner.nextLine();
        
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Role (CUSTOMER/SELLER/ADMIN): ");
        String roleStr = scanner.nextLine().toUpperCase();
        
        User user = new User();
        user.setId(userId);
        
        if(!firstname.isEmpty()) user.setFirstname(firstname);
        if(!lastname.isEmpty()) user.setLastname(lastname);
        if(!email.isEmpty()) user.setEmail(email);
        if(!password.isEmpty()) user.setPassword(password);
        if(!phone.isEmpty()) user.setPhone(phone);
        
        if(!roleStr.isEmpty()){
            try{
                user.setUserRole(UserRole.valueOf(roleStr));
            }catch(IllegalArgumentException e){
                System.out.println("Invalid role, keeping current value");
            }
        }
        
        Result<User> updated = userService.updateUser(user);
        
        if(updated.isSuccess()){
            System.out.println("\n✓ User updated successfully!");
            System.out.println("ID: " + updated.getData().getId());
            System.out.println("Name: " + updated.getData().getFirstname() + " " + updated.getData().getLastname());
            System.out.println("Email: " + updated.getData().getEmail());
        }else{
            System.out.println("\n✗ Failed to update user: " + updated.getMessage());
        }
    }
    
    private static void viewUserForm(){
        System.out.println("\n--- View User ---");
        
        System.out.print("Enter User ID: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        
        Result<User> result = userService.getUserById(userId);
        
        if(result.isSuccess()){
            User user = result.getData();
            System.out.println("\n=== User Details ===");
            System.out.println("ID: " + user.getId());
            System.out.println("Name: " + user.getFirstname() + " " + user.getLastname());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Phone: " + user.getPhone());
            System.out.println("Role: " + user.getUserRole());
            System.out.println("Created: " + user.getCreatedAt());
        }else{
            System.out.println("\n✗ User not found: " + result.getMessage());
        }
    }

    public static  void seedUser(){
        User user = new User();
        user.setFirstname("Admin");
        user.setLastname("User");
        user.setEmail("mohammedbasit362@gmail.com");
        user.setPassword("bece2018");
        user.setPhone("0257323294");

        user.setUserRole(UserRole.ADMIN);
        Result<User> result = userService.createUser(user);
        if (result.isSuccess()){
            System.out.println("✓ Admin user created successfully:");
            System.out.println("  Email: " + result.getData().getEmail());
            System.out.println("  Password: " + user.getPassword());
        } else {
            // Check if it's a duplicate user error
            if (result.getMessage() != null && result.getMessage().contains("already exists")) {
                System.out.println("ℹ Admin user already exists - skipping seed");
            } else {
                System.err.println("✗ Failed to create admin user: " + result.getMessage());
            }
        }
    }
    
    private static void viewAllUsersForm(){
        System.out.println("\n--- All Users ---");
        
        Result<List<User>> result = userService.getAllUsers();
        List<User> users = result.getData();
        
        if(users.isEmpty()){
            System.out.println("No users found");
        }else{
            System.out.println("\nTotal users: " + users.size());
            System.out.println("\n" + "=".repeat(80));
            for(User user : users){
                System.out.printf("ID: %-5d | Name: %-20s | Email: %-25s | Role: %-10s%n",
                    user.getId(),
                    user.getFirstname() + " " + user.getLastname(),
                    user.getEmail(),
                    user.getUserRole());
            }
            System.out.println("=".repeat(80));
        }
    }
    
    private static boolean loginForm(){
        System.out.println("\n=== Login ===");
        
        int attempts = 0;
        while(attempts < 3){
            System.out.print("Email: ");
            String email = scanner.nextLine();
            
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            Result<User> result = userService.login(email, password);
            
            if(result.isSuccess()){
                currentUser = result.getData();
                System.out.println("\n✓ Login successful!");
                System.out.println("Welcome, " + currentUser.getFirstname() + " " + currentUser.getLastname());
                return true;
            }
            
            attempts++;
            if(attempts < 3){
                System.out.println("\n✗ Login failed. " + (3 - attempts) + " attempts remaining.\n");
            }
        }
        
        System.out.println("\n✗ Maximum login attempts exceeded.");
        return false;
    }
    
    private static void createProductForm(){
        System.out.println("\n--- Create New Product ---");
        
        System.out.print("Product Name: ");
        String productName = scanner.nextLine();
        
        System.out.print("Description: ");
        String description = scanner.nextLine();
        
        System.out.print("Price: ");
        BigDecimal price = scanner.nextBigDecimal();
        scanner.nextLine();
        
        System.out.print("Category ID: ");
        int categoryId = scanner.nextInt();
        scanner.nextLine();
        
        Product product = new Product();
        product.setProductName(productName);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategoryId(categoryId);
        
        Result<Product> result = productService.createProduct(product);
        
        if(result.isSuccess()){
            Product created = result.getData();
            System.out.println("\n✓ Product created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getProductName());
            System.out.println("Price: $" + created.getPrice());
            System.out.println("Category ID: " + created.getCategoryId());
        }else{
            System.out.println("\n✗ Failed to create product: " + result.getMessage());
        }
    }
    
    private static void createCategoryForm(){
        System.out.println("\n--- Create New Category ---");
        
        System.out.print("Category Name: ");
        String categoryName = scanner.nextLine();
        
        System.out.print("Description: ");
        String description = scanner.nextLine();
        
        Categories category = new Categories();
        category.setCategoryName(categoryName);
        category.setDescription(description);
        
        Result<Categories> result = categoryService.createCategory(category);
        
        if(result.isSuccess()){
            Categories created = result.getData();
            System.out.println("\n✓ Category created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getCategoryName());
            System.out.println("Description: " + created.getDescription());
        }else{
            System.out.println("\n✗ Failed to create category: " + result.getMessage());
        }
    }
    
    private static void createInventoryForm(){
        System.out.println("\n--- Create New Inventory Record ---");
        
        System.out.print("Product ID: ");
        int productId = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Warehouse Location: ");
        String warehouseLocation = scanner.nextLine();
        
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        inventory.setWarehouseLocation(warehouseLocation);
        
        Result<Inventory> result = inventoryService.createInventory(inventory);
        
        if(result.isSuccess()){
            Inventory created = result.getData();
            System.out.println("\n✓ Inventory record created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Product ID: " + created.getProductId());
            System.out.println("Quantity: " + created.getQuantity());
            System.out.println("Warehouse: " + created.getWarehouseLocation());
        }else{
            System.out.println("\n✗ Failed to create inventory: " + result.getMessage());
        }
    }
    
    private static void viewAllProductsForm(){
        System.out.println("\n--- All Products ---");
        
        Result<List<Product>> result = productService.getAllProducts();
        List<Product> products = result.getData();
        
        if(products.isEmpty()){
            System.out.println("No products found");
        }else{
            System.out.println("\nTotal products: " + products.size());
            System.out.println("\n" + "=".repeat(120));
            System.out.printf("%-5s | %-30s | %-12s | %-15s | %-40s%n",
                "ID", "Product Name", "Price", "Total Stock", "Description");
            System.out.println("=".repeat(120));
            
            for(Product product : products){
                // Get total stock across all warehouses
                Result<Integer> stockResult = productService.getTotalStock(product.getId());
                int totalStock = stockResult.isSuccess() ? stockResult.getData() : 0;
                
                // Truncate description if too long
                String desc = product.getDescription();
                if(desc != null && desc.length() > 40){
                    desc = desc.substring(0, 37) + "...";
                }
                
                System.out.printf("%-5d | %-30s | $%-11.2f | %-15d | %-40s%n",
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    totalStock,
                    desc != null ? desc : "N/A");
            }
            System.out.println("=".repeat(120));
        }
    }
}
