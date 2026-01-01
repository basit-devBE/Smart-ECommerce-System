package org.commerce;

import org.commerce.config.DBConfig;
import org.commerce.entities.User;
import org.commerce.entities.Product;
import org.commerce.enums.UserRole;
import org.commerce.models.*;
import org.commerce.services.UserService;
import org.commerce.services.ProductService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private static Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static ProductService productService;
    private static User currentUser = null;

    public static void main(String[] args) {
        DBConfig dbConfig = new DBConfig();
        try (Connection connection = dbConfig.connectDB()) {
            System.out.println("Database connected successfully.\n");
            
            userService = new UserService(connection);
            productService = new ProductService(connection);
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
                System.out.println("\n=== User Management ===");
                System.out.println("Logged in as: " + currentUser.getFirstname() + " (" + currentUser.getUserRole() + ")");
                System.out.println("1. Create User");
                System.out.println("2. Delete User");
                System.out.println("3. Edit User");
                System.out.println("4. View User by ID");
                System.out.println("5. View All Users");
                System.out.println("6. Create Product");
                System.out.println("7. Logout");
                System.out.print("Choose option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch(choice){
                    case 1 -> createUserForm();
                    case 2 -> deleteUserForm();
                    case 3 -> editUserForm();
                    case 4 -> viewUserForm();
                    case 5 -> viewAllUsersForm();
                    case 6 -> createProductForm();
                    case 7 -> {
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
        
        User created = userService.createUser(user);
        
        if(created != null){
            System.out.println("\n✓ User created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getFirstname() + " " + created.getLastname());
            System.out.println("Email: " + created.getEmail());
        }else{
            System.out.println("\n✗ Failed to create user");
        }
    }
    
    private static void deleteUserForm(){
        System.out.println("\n--- Delete User ---");
        
        System.out.print("Enter User ID to delete: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        
        boolean deleted = userService.deleteUser(userId);
        
        if(deleted){
            System.out.println("\n✓ User deleted successfully!");
        }else{
            System.out.println("\n✗ Failed to delete user");
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
        
        User updated = userService.editUser(user);
        
        if(updated != null){
            System.out.println("\n✓ User updated successfully!");
            System.out.println("ID: " + updated.getId());
            System.out.println("Name: " + updated.getFirstname() + " " + updated.getLastname());
            System.out.println("Email: " + updated.getEmail());
        }else{
            System.out.println("\n✗ Failed to update user");
        }
    }
    
    private static void viewUserForm(){
        System.out.println("\n--- View User ---");
        
        System.out.print("Enter User ID: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        
        User user = userService.getUserById(userId);
        
        if(user != null){
            System.out.println("\n=== User Details ===");
            System.out.println("ID: " + user.getId());
            System.out.println("Name: " + user.getFirstname() + " " + user.getLastname());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Phone: " + user.getPhone());
            System.out.println("Role: " + user.getUserRole());
            System.out.println("Created: " + user.getCreatedAt());
        }else{
            System.out.println("\n✗ User not found");
        }
    }

    public static  void seedUser(){
        User user = new User();
        user.setFirstname("Admin");
        user.setLastname("User");
        user.setEmail("mohammedbasit362@gmail.com");
        user.setPassword("bece2018");
        user.setPhone("0257323294");

        try{
            user.setUserRole(UserRole.ADMIN);
            User created = userService.createUser(user);
            if (created != null){
                System.out.println("Admin user created:");
                System.out.println("Email: " + created.getEmail());
                System.out.println("Password: " + user.getPassword());
            }
        }catch (Exception e){
            System.err.println("Failed to seed admin user: " + e.getMessage());
        }finally {
            System.out.println("Admin user seeding process completed.");

        }
    }
    
    private static void viewAllUsersForm(){
        System.out.println("\n--- All Users ---");
        
        List<User> users = userService.getAllUsers();
        
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
            
            User user = userService.login(email, password);
            
            if(user != null){
                currentUser = user;
                System.out.println("\n✓ Login successful!");
                System.out.println("Welcome, " + user.getFirstname() + " " + user.getLastname());
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
        
        Product created = productService.createProduct(product);
        
        if(created != null){
            System.out.println("\n✓ Product created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getProductName());
            System.out.println("Price: $" + created.getPrice());
            System.out.println("Category ID: " + created.getCategoryId());
        }else{
            System.out.println("\n✗ Failed to create product");
        }
    }
}
