package org.commerce;

import org.commerce.config.MongoDBConfig;
import com.mongodb.client.MongoDatabase;

/**
 * Simple test to verify MongoDB connection
 */
public class MongoDBTest {
    
    public static void main(String[] args) {
        System.out.println("🔍 Testing MongoDB Connection...\n");
        
        // Initialize MongoDB
        MongoDBConfig.initialize();
        
        // Check connection status
        if (MongoDBConfig.isConnected()) {
            System.out.println("✅ Connection Status: CONNECTED");
            
            MongoDatabase db = MongoDBConfig.getDatabase();
            System.out.println("📊 Database Name: " + db.getName());
            
            System.out.println("\n📁 Available Collections:");
            db.listCollectionNames().forEach(name -> System.out.println("  - " + name));
            
            System.out.println("\n✨ MongoDB is ready to use!");
        } else {
            System.out.println("❌ Connection Status: FAILED");
            System.out.println("\n💡 Tips:");
            System.out.println("  1. Make sure MONGO_URI environment variable is set");
            System.out.println("  2. Replace <db_password> with your actual password");
            System.out.println("  3. Check your IP is whitelisted in MongoDB Atlas");
        }
        
        // Close connection
        MongoDBConfig.close();
        System.out.println("\n👋 Test completed!");
    }
}
