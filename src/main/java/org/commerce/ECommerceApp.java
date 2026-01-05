package org.commerce;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.commerce.config.DBConfig;
import org.commerce.config.MongoDBConfig;
import org.commerce.models.*;
import org.commerce.services.UserService;
import org.commerce.services.ProductService;
import org.commerce.services.CategoryService;
import org.commerce.services.InventoryService;
import org.commerce.services.ReviewService;
import org.commerce.services.ActivityLogService;
import org.commerce.entities.User;
import org.commerce.enums.UserRole;
import org.commerce.common.Result;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main JavaFX Application for Smart E-Commerce System
 */
public class ECommerceApp extends Application {
    private static Connection connection;
    private static UserService userService;
    private static ProductService productService;
    private static CategoryService categoryService;
    private static InventoryService inventoryService;
    private static ReviewService reviewService;
    private static ActivityLogService activityLogService;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Initialize database connection and services
        DBConfig dbConfig = new DBConfig();
        connection = dbConfig.connectDB();
        
        // Initialize MongoDB
        MongoDBConfig.initialize();
        
        // Initialize PostgreSQL services
        userService = new UserService(connection);
        productService = new ProductService(connection);
        categoryService = new CategoryService(connection);
        inventoryService = new InventoryService(connection);
        
        // Initialize MongoDB services
        reviewService = new ReviewService();
        activityLogService = new ActivityLogService();
        
        // Wire up inventory service with product service for cache invalidation
        inventoryService.setProductService(productService);
        
        // Initialize database tables
        UsersModel.initializeTable(connection);
        CategoriesModel.initializeTable(connection);
        ProductsModel.initializeTable(connection);
        InventoryModel.initializeTable(connection);
        OrdersModel.initializeTable(connection);
        OrderItemsModel.initializeTable(connection);
        ReviewsModel.initializeTable(connection);
        
        // Seed admin user if it doesn't exist
        seedAdminUser();
        
        // Seed sample data (categories, products, inventory)
        // seedData();
        
        // Log application startup
        if (activityLogService != null) {
            activityLogService.logActivity(0, "System", "APP_START");
        }
        
        // Load login view
        showLoginView();
        
        primaryStage.setTitle("Smart E-Commerce System");
        primaryStage.setOnCloseRequest(event -> {
            closeConnection();
        });
        primaryStage.show();
    }

    public static void showLoginView() throws Exception {
        FXMLLoader loader = new FXMLLoader(ECommerceApp.class.getResource("/views/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(ECommerceApp.class.getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showDashboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(ECommerceApp.class.getResource("/views/dashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(ECommerceApp.class.getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showProductListing() throws Exception {
        FXMLLoader loader = new FXMLLoader(ECommerceApp.class.getResource("/views/product-listing.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(ECommerceApp.class.getResource("/styles/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void closeConnection() {
        // Log application shutdown
        if (activityLogService != null) {
            activityLogService.logActivity(0, "System", "APP_STOP");
        }
        
        // Close PostgreSQL
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        // Close MongoDB
        MongoDBConfig.close();
    }

    public static Connection getConnection() {
        return connection;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static ProductService getProductService() {
        return productService;
    }

    public static CategoryService getCategoryService() {
        return categoryService;
    }

    public static InventoryService getInventoryService() {
        return inventoryService;
    }
    
    public static ReviewService getReviewService() {
        return reviewService;
    }
    
    public static ActivityLogService getActivityLogService() {
        return activityLogService;
    }

    
    private static void seedAdminUser() {
        User user = new User();
        user.setFirstname("Admin");
        user.setLastname("User");
        user.setEmail("mohammedbasit362@gmail.com");
        user.setPassword("bece2018");
        user.setPhone("0257323294");
        user.setUserRole(UserRole.ADMIN);
        
        Result<User> result = userService.createUser(user);
        if (result.isSuccess()) {
            System.out.println("✓ Admin user created successfully");
        } else if (result.getMessage() != null && result.getMessage().contains("already exists")) {
            System.out.println("ℹ Admin user already exists - ready to login");
        } else {
            System.err.println("✗ Failed to create admin user: " + result.getMessage());
        }
    }
    
    private static void seedData() {
        try {
            SeedData seeder = new SeedData(connection);
            seeder.seedAll();
        } catch (Exception e) {
            System.err.println("Note: Sample data seeding skipped (may already exist)");
        }
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
