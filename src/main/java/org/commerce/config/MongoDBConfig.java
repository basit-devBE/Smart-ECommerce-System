package org.commerce.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDB Configuration and Connection Manager
 */
public class MongoDBConfig {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private static final String MONGO_URI = System.getenv().getOrDefault("MONGO_URI", "");
    private static final String MONGO_DB = System.getenv().getOrDefault("MONGO_DB", "ecommerce_reviews");

    /**
     * Initializes MongoDB connection
     */
    public static void initialize() {
        if (mongoClient == null) {
            try {
                if (MONGO_URI.isEmpty()) {
                    System.err.println("⚠️  MONGO_URI environment variable not set. MongoDB features disabled.");
                    return;
                }
                
                // Configure MongoDB client with stable API
                ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
                
                MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(MONGO_URI))
                    .serverApi(serverApi)
                    .build();
                
                mongoClient = MongoClients.create(settings);
                database = mongoClient.getDatabase(MONGO_DB);
                
                // Test connection
                database.listCollectionNames().first();
                
                System.out.println("✅ MongoDB connected successfully to: " + MONGO_DB);
            } catch (Exception e) {
                System.err.println("❌ MongoDB connection failed: " + e.getMessage());
                mongoClient = null;
                database = null;
            }
        }
    }

    /**
     * Gets the MongoDB database instance
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            initialize();
        }
        return database;
    }
    
    /**
     * Checks if MongoDB is connected
     */
    public static boolean isConnected() {
        return database != null;
    }

    /**
     * Closes MongoDB connection
     */
    public static void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                mongoClient = null;
                database = null;
                System.out.println("MongoDB connection closed");
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}
