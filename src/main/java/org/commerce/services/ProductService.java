package org.commerce.services;

import org.commerce.common.CacheManager;
import org.commerce.common.ProductComparator;
import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.daos.entities.Product;
import org.commerce.exceptions.EntityNotFoundException;
import org.commerce.exceptions.ServiceException;
import org.commerce.daos.repositories.CategoryRepository;
import org.commerce.daos.repositories.ProductRepository;
import org.commerce.daos.repositories.interfaces.ICategoryRepository;
import org.commerce.daos.repositories.interfaces.IProductRepository;
import org.commerce.validators.ProductValidator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Product business logic.
 * Handles validation, business rules, and delegates to repository.
 * Implements in-memory caching and sorting for improved performance.
 */
public class ProductService {
    private final Connection connection;
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    
    // Cache for individual products (by ID) - 5 minute TTL, max 200 entries
    private final CacheManager<Integer, Product> productCache;
    
    // Cache for all products list - 3 minute TTL
    private final CacheManager<String, List<Product>> allProductsCache;
    
    // Cache for search results - 2 minute TTL, max 50 searches
    private final CacheManager<String, List<Product>> searchCache;
    
    // Cache for product stock - 1 minute TTL
    private final CacheManager<Integer, Integer> stockCache;

    public ProductService(Connection connection) {
        this.connection = connection;
        this.productRepository = new ProductRepository();
        this.categoryRepository = new CategoryRepository();
        this.productCache = new CacheManager<>(300000, 200); // 5 min, 200 entries
        this.allProductsCache = new CacheManager<>(180000, 1); // 3 min, 1 entry
        this.searchCache = new CacheManager<>(120000, 50); // 2 min, 50 searches
        this.stockCache = new CacheManager<>(60000, 200); // 1 min, 200 entries
    }

    /**
     * Creates a new product.
     * 
     * @param product The product to create
     * @return Result containing the created product or error message
     */
    public Result<Product> createProduct(Product product) {
        // Field validation
        ValidationResult validation = ProductValidator.validate(product);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Business rule: Category must exist
        if (!categoryRepository.exists(product.getCategoryId(), connection)) {
            throw new ServiceException("Category with ID " + product.getCategoryId() + " does not exist");
        }

        // Create product
        Product created = productRepository.createProduct(product, connection);
        
        // Invalidate caches after creation
        invalidateAllCaches();
        
        return Result.success(created, "Product created successfully");
    }

    /**
     * Deletes a product by ID.
     * 
     * @param productId The product ID
     * @return Result containing success status or error message
     */
    public Result<Boolean> deleteProduct(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }

        // Business rule: Product must exist
        Product productExists = productRepository.getProductById(productId, connection);
        if (productExists == null) {
            throw new EntityNotFoundException("Product", productId);
        }

        boolean deleted = productRepository.deleteProduct(productId, connection);
        
        // Invalidate caches after deletion
        invalidateAllCaches();
        
        return Result.success(deleted, "Product deleted successfully");
    }

    /**
     * Updates an existing product.
     * 
     * @param product The product with updated information
     * @return Result containing the updated product or error message
     */
    public Result<Product> updateProduct(Product product) {
        // Field validation
        ValidationResult validation = ProductValidator.validateForUpdate(product);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Business rule: Product must exist
        Product existingProduct = productRepository.getProductById(product.getId(), connection);
        if (existingProduct == null) {
            throw new EntityNotFoundException("Product", product.getId());
        }

        // Merge: use new values if provided, otherwise keep existing
        if (product.getProductName() != null && !product.getProductName().isEmpty()) {
            existingProduct.setProductName(product.getProductName());
        }

        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }

        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }

        if (product.getCategoryId() > 0) {
            // Business rule: New category must exist
            if (!categoryRepository.exists(product.getCategoryId(), connection)) {
                throw new ServiceException("Category with ID " + product.getCategoryId() + " does not exist");
            }
            existingProduct.setCategoryId(product.getCategoryId());
        }

        Product updated = productRepository.updateProduct(existingProduct, connection);
        
        // Invalidate caches after update
        invalidateAllCaches();
        
        return Result.success(updated, "Product updated successfully");
    }

    /**
     * Retrieves a product by ID (with caching).
     * 
     * @param productId The product ID
     * @return Result containing the product or error message
     */
    public Result<Product> getProductById(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }

        // Try to get from cache first
        Product product = productCache.get(productId, () -> 
            productRepository.getProductById(productId, connection)
        );
        
        if (product == null) {
            throw new EntityNotFoundException("Product", productId);
        }

        return Result.success(product);
    }

    /**
     * Retrieves all products (with caching).
     * 
     * @return Result containing list of all products
     */
    public Result<List<Product>> getAllProducts() {
        List<Product> products = allProductsCache.get("ALL", () -> 
            productRepository.getAllProducts(connection)
        );
        return Result.success(products);
    }
    
    /**
     * Retrieves all products sorted by specified criteria.
     * 
     * @param sortBy Sort criteria: "name", "price_asc", "price_desc", "newest", etc.
     * @return Result containing sorted list of products
     */
    public Result<List<Product>> getAllProductsSorted(String sortBy) {
        String cacheKey = "ALL_SORTED_" + (sortBy != null ? sortBy : "name");
        
        List<Product> products = allProductsCache.get(cacheKey, () -> {
            List<Product> allProducts = productRepository.getAllProducts(connection);
            Comparator<Product> comparator = ProductComparator.getComparator(sortBy);
            
            // Sort in memory
            List<Product> sorted = new ArrayList<>(allProducts);
            sorted.sort(comparator);
            return sorted;
        });
        
        return Result.success(products);
    }

    /**
     * Gets the total stock for a product (with caching).
     * 
     * @param productId The product ID
     * @return Result containing the total stock quantity
     */
    public Result<Integer> getTotalStock(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }

        // Business rule: Product must exist
        Product product = productRepository.getProductById(productId, connection);
        if (product == null) {
            throw new EntityNotFoundException("Product", productId);
        }

        // Use stock cache with 1 minute TTL
        int totalStock = stockCache.get(productId, () -> 
            productRepository.getTotalStock(productId, connection)
        );
        
        return Result.success(totalStock);
    }
    
    /**
     * Searches products by name or description (cached and sortable).
     * 
     * @param searchTerm The search term (case-insensitive)
     * @return Result containing list of matching products
     */
    public Result<List<Product>> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String normalizedTerm = searchTerm.trim().toLowerCase();
        String cacheKey = "SEARCH_" + normalizedTerm;
        
        List<Product> products = searchCache.get(cacheKey, () -> 
            productRepository.searchProducts(normalizedTerm, connection)
        );
        
        return Result.success(products, "Found " + products.size() + " product(s)");
    }
    
    /**
     * Searches products with optional category filter and search term (cached).
     * 
     * @param categoryId The category ID (null for all categories)
     * @param searchTerm The search term (null or empty for no search filter)
     * @return Result containing list of matching products
     */
    public Result<List<Product>> searchProductsByCategory(Integer categoryId, String searchTerm) {
        // Validate category if provided
        if (categoryId != null && categoryId > 0) {
            if (!categoryRepository.exists(categoryId, connection)) {
                throw new EntityNotFoundException("Category", categoryId);
            }
        }
        
        String normalizedTerm = searchTerm != null ? searchTerm.trim() : null;
        String cacheKey = "SEARCH_CAT_" + categoryId + "_" + 
                         (normalizedTerm != null ? normalizedTerm.toLowerCase() : "all");
        
        List<Product> products = searchCache.get(cacheKey, () -> 
            productRepository.searchProductsByCategory(categoryId, normalizedTerm, connection)
        );
        
        return Result.success(products, "Found " + products.size() + " product(s)");
    }
    
    /**
     * Gets products by category with in-memory sorting.
     * 
     * @param categoryId The category ID
     * @param sortBy Sort criteria
     * @return Result containing sorted list of products
     */
    public Result<List<Product>> getProductsByCategorySorted(int categoryId, String sortBy) {
        if (categoryId <= 0) {
            return Result.failure("Invalid category ID");
        }
        
        // Get all products and filter by category in memory
        Result<List<Product>> allProducts = getAllProducts();
        if (!allProducts.isSuccess()) {
            return allProducts;
        }
        
        List<Product> filtered = allProducts.getData().stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
        
        // Sort in memory
        Comparator<Product> comparator = ProductComparator.getComparator(sortBy);
        filtered.sort(comparator);
        
        return Result.success(filtered, "Found " + filtered.size() + " product(s) in category");
    }
    
    /**
     * Invalidates all product caches.
     * Should be called after any create, update, or delete operation.
     */
    public void invalidateAllCaches() {
        productCache.invalidateAll();
        allProductsCache.invalidateAll();
        searchCache.invalidateAll();
        stockCache.invalidateAll();
    }
    
    /**
     * Invalidates stock cache only (useful after inventory changes).
     */
    public void invalidateStockCache() {
        stockCache.invalidateAll();
    }
    
    /**
     * Invalidates stock cache for specific product.
     */
    public void invalidateStockCache(int productId) {
        stockCache.invalidate(productId);
    }
    
    /**
     * Gets cache statistics.
     */
    public String getCacheStats() {
        return String.format(
            "Product Cache: %d entries, All Products Cache: %d entries, Search Cache: %d entries, Stock Cache: %d entries",
            productCache.size(), allProductsCache.size(), searchCache.size(), stockCache.size()
        );
    }
}
