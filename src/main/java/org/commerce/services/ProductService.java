package org.commerce.services;

import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.entities.Product;
import org.commerce.exceptions.EntityNotFoundException;
import org.commerce.exceptions.ServiceException;
import org.commerce.repositories.CategoryRepository;
import org.commerce.repositories.ProductRepository;
import org.commerce.repositories.interfaces.ICategoryRepository;
import org.commerce.repositories.interfaces.IProductRepository;
import org.commerce.validators.ProductValidator;

import java.sql.Connection;
import java.util.List;

/**
 * Service layer for Product business logic.
 * Handles validation, business rules, and delegates to repository.
 */
public class ProductService {
    private final Connection connection;
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;

    public ProductService(Connection connection) {
        this.connection = connection;
        this.productRepository = new ProductRepository();
        this.categoryRepository = new CategoryRepository();
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
        return Result.success(updated, "Product updated successfully");
    }

    /**
     * Retrieves a product by ID.
     * 
     * @param productId The product ID
     * @return Result containing the product or error message
     */
    public Result<Product> getProductById(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }

        Product product = productRepository.getProductById(productId, connection);
        if (product == null) {
            throw new EntityNotFoundException("Product", productId);
        }

        return Result.success(product);
    }

    /**
     * Retrieves all products.
     * 
     * @return Result containing list of all products
     */
    public Result<List<Product>> getAllProducts() {
        List<Product> products = productRepository.getAllProducts(connection);
        return Result.success(products);
    }

    /**
     * Gets the total stock for a product.
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

        int totalStock = productRepository.getTotalStock(productId, connection);
        return Result.success(totalStock);
    }
    
    /**
     * Searches products by name or description (optimized with database indexes).
     * 
     * @param searchTerm The search term (case-insensitive)
     * @return Result containing list of matching products
     */
    public Result<List<Product>> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        
        List<Product> products = productRepository.searchProducts(searchTerm.trim(), connection);
        return Result.success(products, "Found " + products.size() + " product(s)");
    }
    
    /**
     * Searches products with optional category filter and search term (optimized).
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
        
        List<Product> products = productRepository.searchProductsByCategory(
            categoryId, 
            searchTerm != null ? searchTerm.trim() : null, 
            connection
        );
        
        return Result.success(products, "Found " + products.size() + " product(s)");
    }
}
