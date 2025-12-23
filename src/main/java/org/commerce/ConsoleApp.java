package org.commerce;

import org.commerce.config.DBConfig;
import org.commerce.entities.User;
import org.commerce.enums.UserRole;
import org.commerce.models.*;
import org.commerce.services.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class ConsoleApp {
    private static Scanner scanner = new Scanner(System.in);
    private static UserService userService;

    public static void main(String[] args) {
        DBConfig dbConfig = new DBConfig();
        try (Connection connection = dbConfig.connectDB()) {
            System.out.println("Database connected successfully.\n");
            
            userService = new UserService(connection);
            UsersModel.initializeTable(connection);
            CategoriesModel.initializeTable(connection);
            ProductsModel.initializeTable(connection);
            InventoryModel.initializeTable(connection);
            OrdersModel.initializeTable(connection);
            OrderItemsModel.initializeTable(connection);
            ReviewsModel.initializeTable(connection);

            
            boolean running = true;
            while(running){
                System.out.println("\n=== User Management ===");
                System.out.println("1. Create User");
                System.out.println("2. Delete User");
                System.out.println("3. Edit User");
                System.out.println("4. Exit");
                System.out.print("Choose option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch(choice){
                    case 1 -> createUserForm();
                    case 2 -> deleteUserForm();
                    case 3 -> editUserForm();
                    case 4 -> running = false;
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
}
