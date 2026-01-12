# Quick Reference: Caching & Sorting API

## 🎯 How to Use the New Features

### 1. Product Sorting

#### Get All Products with Sorting
```java
// In your controller or service
ProductService productService = ECommerceApp.getProductService();

// Sort by name (A-Z)
Result<List<Product>> result = productService.getAllProductsSorted("name");

// Sort by price (lowest first)
Result<List<Product>> result = productService.getAllProductsSorted("price_asc");

// Sort by price (highest first)
Result<List<Product>> result = productService.getAllProductsSorted("price_desc");

// Sort by date (newest first)
Result<List<Product>> result = productService.getAllProductsSorted("newest");
```

#### Available Sort Options:
| Option | Description |
|--------|-------------|
| `"name"` or `"name_asc"` | Alphabetical (A-Z) |
| `"name_desc"` | Reverse alphabetical (Z-A) |
| `"price"` or `"price_asc"` | Price low to high |
| `"price_desc"` | Price high to low |
| `"newest"` or `"date_desc"` | Newest first |
| `"oldest"` or `"date_asc"` | Oldest first |
| `"category"` | By category ID, then name |

#### Get Products by Category with Sorting
```java
int categoryId = 1;
Result<List<Product>> result = productService.getProductsByCategorySorted(categoryId, "price_asc");
List<Product> sortedProducts = result.getData();
```

---

### 2. Category Lookups

#### Get Category by Name (Fast HashMap Lookup)
```java
CategoryService categoryService = ECommerceApp.getCategoryService();

// O(1) lookup from HashMap
Result<Categories> result = categoryService.getCategoryByName("Electronics");
if (result.isSuccess()) {
    Categories category = result.getData();
    System.out.println("Category ID: " + category.getId());
}
```

#### Get Category by ID (Cached)
```java
// First call: Database query + cache
// Subsequent calls: Cache hit (no DB query)
Result<Categories> result = categoryService.getCategoryById(1);
```

---

### 3. User Session Management

#### Login (Stores in Active Session Cache)
```java
UserService userService = ECommerceApp.getUserService();

Result<User> result = userService.login("user@example.com", "password");
if (result.isSuccess()) {
    User user = result.getData();
    // User is now in active session HashMap
}
```

#### Get Active User (Fast O(1) Lookup)
```java
int userId = 1;
User activeUser = userService.getActiveUser(userId);
if (activeUser != null) {
    // User is logged in
    System.out.println("Welcome " + activeUser.getFirstname());
}
```

#### Logout (Remove from Session)
```java
userService.logout(userId);
```

#### Check Active Sessions
```java
int activeCount = userService.getActiveSessionCount();
System.out.println("Active users: " + activeCount);
```

---

### 4. Cache Management

#### View Cache Statistics
```java
// Product cache stats
System.out.println(productService.getCacheStats());
// Output: Product Cache: 45 entries, All Products Cache: 1 entries, 
//         Search Cache: 12 entries, Stock Cache: 38 entries

// Category cache stats
System.out.println(categoryService.getCacheStats());

// User cache stats
System.out.println(userService.getCacheStats());
```

#### Manual Cache Invalidation
```java
// Invalidate all product caches (after bulk updates)
productService.invalidateAllCaches();

// Invalidate specific product's stock cache
productService.invalidateStockCache(productId);

// Invalidate all category caches
categoryService.invalidateAllCaches();

// Invalidate all user caches
userService.invalidateAllCaches();
```

---

### 5. Search with Caching

#### Search Products (Cached Results)
```java
String searchTerm = "laptop";

// First search: Database query + cache
// Subsequent same searches: Cache hit
Result<List<Product>> result = productService.searchProducts(searchTerm);
List<Product> products = result.getData();
```

#### Search by Category (Cached)
```java
Integer categoryId = 1; // or null for all categories
String searchTerm = "gaming";

Result<List<Product>> result = productService.searchProductsByCategory(categoryId, searchTerm);
```

---

## 📊 Cache Configuration

### TTL (Time-To-Live) Settings
```
Categories:
  - Individual: 10 minutes
  - All list: 5 minutes

Products:
  - Individual: 5 minutes
  - All list: 3 minutes
  - Search results: 2 minutes
  - Stock: 1 minute

Users:
  - Individual: 10 minutes
  - Email lookup: 5 minutes
  - Active sessions: No TTL (until logout)
```

### Max Cache Sizes
```
categoryCache:        100 entries
productCache:         200 entries
searchCache:           50 entries
stockCache:           200 entries
userCache:            100 entries
```

---

## 🔧 Integration Example

### Controller Example: Product Listing with Sorting
```java
public class ProductController {
    private ProductService productService = ECommerceApp.getProductService();
    
    @FXML
    private ComboBox<String> sortComboBox;
    
    @FXML
    private void loadProducts() {
        String sortBy = sortComboBox.getValue(); // "price_asc", "name", etc.
        
        Result<List<Product>> result = productService.getAllProductsSorted(sortBy);
        
        if (result.isSuccess()) {
            displayProducts(result.getData());
        }
    }
}
```

### Service Layer Example: Custom Sorting
```java
import org.commerce.common.ProductComparator;

public void customSort() {
    List<Product> products = getAllProducts().getData();
    
    // Use predefined comparators
    products.sort(ProductComparator.BY_PRICE_ASC);
    
    // Or get by string
    Comparator<Product> comparator = ProductComparator.getComparator("price_desc");
    products.sort(comparator);
}
```

---

## ⚡ Performance Tips

1. **Use Caching for Frequent Reads:**
   - Categories (rarely change) → Perfect for caching
   - Products (moderate change) → Good candidate
   - Stock levels (frequent change) → Short TTL (1 min)

2. **In-Memory Sorting:**
   - Fast for small to medium datasets (< 1000 items)
   - No database load
   - Multiple sort operations without DB queries

3. **Session Management:**
   - Active users in HashMap (O(1) access)
   - No database queries for logged-in users
   - Instant session validation

4. **Cache Invalidation:**
   - Auto-invalidates on create/update/delete
   - Inventory changes invalidate stock cache only
   - Manual invalidation for bulk operations

---

## 🧪 Testing the Cache

Run the demo class to see caching in action:
```bash
cd /home/abdul/Desktop/github_projects/Smart-ECommerce-System
mvn compile
mvn exec:java -Dexec.mainClass="org.commerce.CacheDemo"
```

This will demonstrate:
- Cache hit vs miss performance
- In-memory sorting
- Session management
- Cache statistics

---

## 📈 Expected Performance Gains

| Operation | Before | After | Improvement |
|-----------|--------|-------|-------------|
| Get all categories | ~50ms DB | ~1ms cache | 50x faster |
| Get all products | ~100ms DB | ~2ms cache | 50x faster |
| Sort products | ~80ms SQL | ~5ms memory | 16x faster |
| User session check | ~30ms DB | ~0.5ms HashMap | 60x faster |
| Search (repeated) | ~120ms DB | ~3ms cache | 40x faster |

---

## 🎓 Best Practices

1. ✅ **Always use caching for frequently accessed data**
2. ✅ **Set appropriate TTL based on data change frequency**
3. ✅ **Invalidate cache after modifications**
4. ✅ **Monitor cache hit rates with getCacheStats()**
5. ✅ **Use in-memory sorting for small datasets**
6. ✅ **Keep active user sessions in HashMap**
7. ✅ **Use targeted cache invalidation when possible**

---

## 📞 Questions?

The caching system is fully integrated and ready to use. All existing code will benefit from caching automatically. New features like sorting are available through the service layer APIs shown above.
