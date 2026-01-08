package org.commerce;

import org.commerce.daos.entities.Categories;
import org.commerce.daos.entities.Inventory;
import org.commerce.daos.entities.Product;
import org.commerce.services.CategoryService;
import org.commerce.services.InventoryService;
import org.commerce.services.ProductService;
import org.commerce.common.Result;
import org.commerce.config.DBConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds the database with sample data for testing and demonstration.
 */
public class SeedData {
    
    private final CategoryService categoryService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final Random random = new Random();
    
    public SeedData(Connection connection) {
        this.categoryService = new CategoryService(connection);
        this.productService = new ProductService(connection);
        this.inventoryService = new InventoryService(connection);
        this.inventoryService.setProductService(productService);
    }
    
    /**
     * Seeds all data: categories, products, and inventory.
     */
    public void seedAll() {
        System.out.println("🌱 Starting data seeding...");
        
        List<Categories> categories = seedCategories();
        System.out.println("✅ Created " + categories.size() + " categories");
        
        List<Product> products = seedProducts(categories);
        System.out.println("✅ Created " + products.size() + " products");
        
        int inventoryCount = seedInventory(products);
        System.out.println("✅ Created " + inventoryCount + " inventory records");
        
        System.out.println("🎉 Data seeding completed successfully!");
    }
    
    /**
     * Seeds category data.
     */
    private List<Categories> seedCategories() {
        String[][] categoryData = {
            {"Electronics", "Electronic devices and accessories"},
            {"Computers & Laptops", "Desktop computers, laptops, and accessories"},
            {"Mobile Phones", "Smartphones and mobile accessories"},
            {"Tablets", "Tablets and e-readers"},
            {"Audio & Headphones", "Headphones, speakers, and audio equipment"},
            {"Cameras", "Digital cameras and photography equipment"},
            {"Gaming", "Video games, consoles, and gaming accessories"},
            {"Smart Home", "Smart home devices and automation"},
            {"Wearables", "Smartwatches and fitness trackers"},
            {"Computer Components", "CPUs, GPUs, RAM, and other components"},
            
            {"Home & Kitchen", "Home appliances and kitchen essentials"},
            {"Furniture", "Home and office furniture"},
            {"Kitchen Appliances", "Small and large kitchen appliances"},
            {"Bedding & Bath", "Bedding, towels, and bathroom accessories"},
            {"Home Decor", "Decorative items and wall art"},
            {"Storage & Organization", "Storage solutions and organizers"},
            {"Lighting", "Indoor and outdoor lighting"},
            {"Garden & Outdoor", "Garden tools and outdoor equipment"},
            
            {"Fashion", "Clothing and fashion accessories"},
            {"Men's Clothing", "Men's fashion and apparel"},
            {"Women's Clothing", "Women's fashion and apparel"},
            {"Shoes", "Footwear for all occasions"},
            {"Bags & Luggage", "Handbags, backpacks, and luggage"},
            {"Watches", "Wristwatches and smartwatches"},
            {"Jewelry", "Fashion and fine jewelry"},
            {"Sunglasses", "Sunglasses and eyewear"},
            
            {"Sports & Outdoors", "Sports equipment and outdoor gear"},
            {"Fitness Equipment", "Home gym and fitness equipment"},
            {"Camping & Hiking", "Camping gear and hiking equipment"},
            {"Cycling", "Bicycles and cycling accessories"},
            {"Water Sports", "Equipment for water activities"},
            {"Team Sports", "Equipment for team sports"},
            
            {"Books & Media", "Books, movies, and music"},
            {"Books", "Fiction, non-fiction, and educational books"},
            {"Movies & TV", "DVDs, Blu-rays, and digital media"},
            {"Music", "CDs, vinyl records, and music accessories"},
            {"Video Games", "Video games for all platforms"},
            
            {"Toys & Games", "Toys and games for all ages"},
            {"Action Figures", "Collectible action figures and toys"},
            {"Board Games", "Board games and puzzles"},
            {"Educational Toys", "Learning and educational toys"},
            {"Outdoor Play", "Outdoor toys and play equipment"},
            
            {"Health & Beauty", "Health and beauty products"},
            {"Skincare", "Skincare products and treatments"},
            {"Makeup", "Cosmetics and makeup products"},
            {"Hair Care", "Hair care products and tools"},
            {"Personal Care", "Personal hygiene and care products"},
            {"Vitamins & Supplements", "Health supplements and vitamins"},
            
            {"Automotive", "Car parts and automotive accessories"},
            {"Car Electronics", "GPS, dash cams, and car audio"},
            {"Car Accessories", "Interior and exterior car accessories"},
            {"Tools & Equipment", "Automotive tools and equipment"},
            
            {"Office Supplies", "Office and school supplies"},
            {"Stationery", "Pens, pencils, and stationery items"},
            {"Office Electronics", "Printers, scanners, and office equipment"},
            {"Office Furniture", "Desks, chairs, and office furniture"}
        };
        
        List<Categories> categories = new ArrayList<>();
        
        for (String[] data : categoryData) {
            try {
                Categories category = new Categories();
                category.setCategoryName(data[0]);
                category.setDescription(data[1]);
                
                Result<Categories> result = categoryService.createCategory(category);
                if (result.isSuccess()) {
                    categories.add(result.getData());
                }
            } catch (Exception e) {
                // Category might already exist, skip
            }
        }
        
        return categories;
    }
    
    /**
     * Seeds product data.
     */
    private List<Product> seedProducts(List<Categories> categories) {
        String[][] productData = {
            // Electronics & Computers
            {"Dell XPS 13 Laptop", "13-inch laptop with Intel i7, 16GB RAM, 512GB SSD", "799.99"},
            {"MacBook Pro 14-inch", "M2 Pro chip, 16GB RAM, 512GB SSD", "1999.99"},
            {"HP Pavilion Desktop", "Intel i5, 8GB RAM, 1TB HDD", "549.99"},
            {"Lenovo ThinkPad X1", "Business laptop with Intel i7, 32GB RAM", "1499.99"},
            {"ASUS ROG Gaming Laptop", "RTX 4060, Intel i7, 16GB RAM", "1299.99"},
            {"Microsoft Surface Pro 9", "Intel i5, 8GB RAM, 256GB SSD", "899.99"},
            {"Acer Chromebook", "Budget laptop for students", "299.99"},
            
            // Mobile Phones
            {"iPhone 15 Pro", "256GB, Titanium Blue", "1099.99"},
            {"Samsung Galaxy S24", "128GB, Phantom Black", "899.99"},
            {"Google Pixel 8", "128GB, Obsidian", "699.99"},
            {"OnePlus 12", "256GB, Flowy Emerald", "799.99"},
            {"Xiaomi 13 Pro", "256GB, Ceramic White", "649.99"},
            {"iPhone 14", "128GB, Midnight", "799.99"},
            {"Samsung Galaxy A54", "128GB, Awesome Violet", "449.99"},
            
            // Tablets
            {"iPad Air", "64GB, Space Gray", "599.99"},
            {"Samsung Galaxy Tab S9", "128GB, Graphite", "799.99"},
            {"Amazon Fire HD 10", "32GB, Black", "149.99"},
            {"Microsoft Surface Go 3", "64GB, Platinum", "399.99"},
            
            // Audio & Headphones
            {"Sony WH-1000XM5", "Wireless noise-cancelling headphones", "399.99"},
            {"Apple AirPods Pro", "2nd generation with MagSafe", "249.99"},
            {"Bose QuietComfort 45", "Wireless headphones", "329.99"},
            {"JBL Flip 6", "Portable Bluetooth speaker", "129.99"},
            {"Beats Studio Pro", "Wireless over-ear headphones", "349.99"},
            {"Anker Soundcore Q30", "Budget noise-cancelling headphones", "79.99"},
            
            // Cameras
            {"Canon EOS R6", "Full-frame mirrorless camera", "2499.99"},
            {"Sony A7 IV", "Full-frame mirrorless camera", "2498.99"},
            {"Nikon Z6 II", "Full-frame mirrorless camera", "1999.99"},
            {"GoPro Hero 12", "Action camera", "399.99"},
            {"DJI Mini 3 Pro", "Compact drone with 4K camera", "759.99"},
            
            // Gaming
            {"PlayStation 5", "Gaming console with disc drive", "499.99"},
            {"Xbox Series X", "Gaming console 1TB", "499.99"},
            {"Nintendo Switch OLED", "Portable gaming console", "349.99"},
            {"Steam Deck", "Handheld gaming PC", "399.99"},
            {"Razer DeathAdder V3", "Gaming mouse", "69.99"},
            {"Logitech G Pro X", "Mechanical gaming keyboard", "149.99"},
            {"ASUS ROG Swift Monitor", "27-inch 144Hz gaming monitor", "499.99"},
            
            // Smart Home
            {"Amazon Echo Dot", "5th gen smart speaker", "49.99"},
            {"Google Nest Hub", "7-inch smart display", "99.99"},
            {"Ring Video Doorbell", "1080p HD video doorbell", "99.99"},
            {"Philips Hue Starter Kit", "Smart LED bulbs 4-pack", "199.99"},
            {"Nest Learning Thermostat", "Smart thermostat", "249.99"},
            {"TP-Link Kasa Smart Plug", "4-pack WiFi smart plugs", "29.99"},
            
            // Wearables
            {"Apple Watch Series 9", "GPS, 45mm, Midnight", "429.99"},
            {"Samsung Galaxy Watch 6", "44mm, Graphite", "329.99"},
            {"Fitbit Charge 6", "Fitness tracker", "159.99"},
            {"Garmin Forerunner 265", "Running smartwatch", "449.99"},
            {"Amazfit GTS 4", "Budget smartwatch", "199.99"},
            
            // Computer Components
            {"AMD Ryzen 9 7950X", "16-core processor", "549.99"},
            {"Intel Core i9-13900K", "24-core processor", "589.99"},
            {"NVIDIA RTX 4080", "Graphics card 16GB", "1199.99"},
            {"Corsair Vengeance DDR5", "32GB (2x16GB) 6000MHz", "149.99"},
            {"Samsung 990 Pro", "2TB NVMe SSD", "199.99"},
            {"ASUS ROG Strix B650", "AMD motherboard", "249.99"},
            
            // Home & Kitchen
            {"KitchenAid Stand Mixer", "5-quart tilt-head mixer", "379.99"},
            {"Ninja Air Fryer", "6-quart capacity", "129.99"},
            {"Instant Pot Duo", "7-in-1 pressure cooker", "99.99"},
            {"Dyson V15 Detect", "Cordless vacuum cleaner", "649.99"},
            {"iRobot Roomba j7+", "Robot vacuum with auto-empty", "799.99"},
            {"Keurig K-Elite", "Single-serve coffee maker", "189.99"},
            {"Cuisinart Food Processor", "14-cup capacity", "199.99"},
            
            // Furniture
            {"IKEA MARKUS", "Office chair, black", "199.99"},
            {"Herman Miller Aeron", "Ergonomic office chair", "1395.00"},
            {"FlexiSpot Standing Desk", "Electric height-adjustable", "449.99"},
            {"La-Z-Boy Recliner", "Leather recliner chair", "899.99"},
            {"Wayfair Sectional Sofa", "L-shaped fabric sofa", "1299.99"},
            
            // Fashion
            {"Levi's 501 Original Jeans", "Classic fit denim jeans", "69.99"},
            {"Nike Air Max 270", "Men's running shoes", "150.00"},
            {"Adidas Ultraboost 22", "Running shoes", "190.00"},
            {"Ray-Ban Aviator", "Classic sunglasses", "154.00"},
            {"The North Face Jacket", "Waterproof winter jacket", "299.99"},
            {"Timex Weekender", "Casual analog watch", "49.99"},
            {"Fossil Gen 6 Smartwatch", "Touchscreen smartwatch", "299.99"},
            
            // Sports & Fitness
            {"Bowflex Dumbbells", "Adjustable 5-52.5 lbs pair", "549.99"},
            {"Peloton Bike", "Indoor cycling bike", "1445.00"},
            {"Yeti Rambler", "36oz insulated bottle", "49.99"},
            {"Theragun Prime", "Massage gun", "299.99"},
            {"Manduka Pro Yoga Mat", "Premium yoga mat", "120.00"},
            
            // Books & Media
            {"The Great Gatsby", "F. Scott Fitzgerald classic", "14.99"},
            {"Atomic Habits", "James Clear self-help book", "16.99"},
            {"Dune: Complete Series", "Frank Herbert sci-fi collection", "49.99"},
            {"The Office Complete Series", "DVD box set", "89.99"},
            
            // Toys & Games
            {"LEGO Star Wars Set", "Millennium Falcon 1351 pieces", "169.99"},
            {"Monopoly Classic", "Board game", "19.99"},
            {"Rubik's Cube", "3x3 puzzle cube", "9.99"},
            {"Hot Wheels Track Set", "Ultimate garage playset", "99.99"},
            
            // Health & Beauty
            {"CeraVe Moisturizing Cream", "Facial moisturizer 16oz", "19.99"},
            {"Neutrogena Makeup Remover", "Cleansing wipes 25-count", "8.99"},
            {"Oral-B Electric Toothbrush", "Pro 1000 rechargeable", "49.99"},
            {"Gillette Fusion5 Razors", "Men's razor 12-count", "39.99"},
            {"Nature Made Multivitamin", "Daily supplement 150-count", "21.99"},
            
            // Automotive
            {"Garmin DriveSmart 65", "GPS navigator", "249.99"},
            {"Anker Roav DashCam", "1080p dash camera", "99.99"}
        };
        
        List<Product> products = new ArrayList<>();
        
        for (String[] data : productData) {
            try {
                Product product = new Product();
                product.setProductName(data[0]);
                product.setDescription(data[1]);
                product.setPrice(new BigDecimal(data[2]));
                
                // Assign random category
                Categories randomCategory = categories.get(random.nextInt(categories.size()));
                product.setCategoryId(randomCategory.getId());
                
                Result<Product> result = productService.createProduct(product);
                if (result.isSuccess()) {
                    products.add(result.getData());
                }
            } catch (Exception e) {
                System.err.println("Error creating product: " + data[0] + " - " + e.getMessage());
            }
        }
        
        return products;
    }
    
    /**
     * Seeds inventory data for products.
     */
    private int seedInventory(List<Product> products) {
        String[] warehouses = {
            "New York Warehouse",
            "Los Angeles Warehouse",
            "Chicago Warehouse",
            "Houston Warehouse",
            "Phoenix Warehouse",
            "Seattle Warehouse",
            "Miami Warehouse",
            "Boston Warehouse"
        };
        
        int count = 0;
        
        for (Product product : products) {
            try {
                // Each product gets 1-3 inventory records in different warehouses
                int numWarehouses = random.nextInt(3) + 1;
                
                for (int i = 0; i < numWarehouses; i++) {
                    Inventory inventory = new Inventory();
                    inventory.setProductId(product.getId());
                    inventory.setQuantity(random.nextInt(200) + 10); // 10-209 units
                    inventory.setWarehouseLocation(warehouses[random.nextInt(warehouses.length)]);
                    
                    Result<Inventory> result = inventoryService.createInventory(inventory);
                    if (result.isSuccess()) {
                        count++;
                    }
                }
            } catch (Exception e) {
                // Duplicate warehouse entries might fail, continue
            }
        }
        
        return count;
    }
    
    /**
     * Main method to run seeding independently.
     */
    public static void main(String[] args) {
        try {
            DBConfig dbConfig = new DBConfig();
            Connection connection = dbConfig.connectDB();
            SeedData seeder = new SeedData(connection);
            seeder.seedAll();
        } catch (Exception e) {
            System.err.println("❌ Error seeding data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
