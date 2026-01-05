package org.commerce.services;

import org.commerce.common.CacheManager;
import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.entities.Categories;
import org.commerce.exceptions.EntityNotFoundException;
import org.commerce.repositories.CategoryRepository;
import org.commerce.repositories.interfaces.ICategoryRepository;
import org.commerce.validators.CategoryValidator;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for Category business logic.
 * Handles validation, business rules, and delegates to repository.
 * Implements in-memory caching for improved performance.
 */
public class CategoryService {
    private final Connection connection;
    private final ICategoryRepository categoryRepository;
    
    // Cache for individual categories (by ID) - 10 minute TTL, max 100 entries
    private final CacheManager<Integer, Categories> categoryCache;
    
    // Cache for all categories list - 5 minute TTL
    private final CacheManager<String, List<Categories>> allCategoriesCache;
    
    // In-memory Map for quick category lookups by name
    private final Map<String, Categories> categoryNameIndex;

    public CategoryService(Connection connection) {
        this.connection = connection;
        this.categoryRepository = new CategoryRepository();
        this.categoryCache = new CacheManager<>(600000, 100); // 10 min, 100 entries
        this.allCategoriesCache = new CacheManager<>(300000, 1); // 5 min, 1 entry
        this.categoryNameIndex = new HashMap<>();
    }

    /**
     * Creates a new category.
     * 
     * @param category The category to create
     * @return Result containing the created category or error message
     */
    public Result<Categories> createCategory(Categories category) {
        // Field validation
        ValidationResult validation = CategoryValidator.validate(category);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Create category
        Categories created = categoryRepository.createCategory(category, connection);
        
        // Invalidate caches after creation
        invalidateAllCaches();
        
        return Result.success(created, "Category created successfully");
    }

    /**
     * Retrieves a category by ID (with caching).
     * 
     * @param categoryId The category ID
     * @return Result containing the category or error message
     */
    public Result<Categories> getCategoryById(int categoryId) {
        if (categoryId <= 0) {
            return Result.failure("Invalid category ID");
        }

        // Try to get from cache first
        Categories category = categoryCache.get(categoryId, () -> {
            Categories cat = categoryRepository.getCategoryById(categoryId, connection);
            if (cat != null) {
                // Update name index
                categoryNameIndex.put(cat.getCategoryName(), cat);
            }
            return cat;
        });
        
        if (category == null) {
            throw new EntityNotFoundException("Category", categoryId);
        }

        return Result.success(category);
    }

    /**
     * Retrieves all categories (with caching).
     * 
     * @return Result containing list of all categories
     */
    public Result<List<Categories>> getAllCategories() {
        // Use cache with "ALL" key
        List<Categories> categories = allCategoriesCache.get("ALL", () -> {
            List<Categories> cats = categoryRepository.getAllCategories(connection);
            // Populate name index
            for (Categories cat : cats) {
                categoryNameIndex.put(cat.getCategoryName(), cat);
            }
            return cats;
        });
        
        return Result.success(categories);
    }

    /**
     * Updates an existing category.
     * 
     * @param category The category with updated information
     * @return Result containing the updated category or error message
     */
    public Result<Categories> updateCategory(Categories category) {
        // Field validation
        ValidationResult validation = CategoryValidator.validateForUpdate(category);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Business rule: Category must exist
        Categories existingCategory = categoryRepository.getCategoryById(category.getId(), connection);
        if (existingCategory == null) {
            throw new EntityNotFoundException("Category", category.getId());
        }

        // Merge: use new values if provided, otherwise keep existing
        if (category.getCategoryName() != null && !category.getCategoryName().isEmpty()) {
            existingCategory.setCategoryName(category.getCategoryName());
        }

        if (category.getDescription() != null) {
            existingCategory.setDescription(category.getDescription());
        }

        Categories updated = categoryRepository.updateCategory(existingCategory, connection);
        
        // Invalidate caches after update
        invalidateAllCaches();
        
        return Result.success(updated, "Category updated successfully");
    }

    /**
     * Deletes a category by ID.
     * 
     * @param categoryId The category ID
     * @return Result containing success status or error message
     */
    public Result<Boolean> deleteCategory(int categoryId) {
        if (categoryId <= 0) {
            return Result.failure("Invalid category ID");
        }

        // Business rule: Category must exist
        Categories categoryExists = categoryRepository.getCategoryById(categoryId, connection);
        if (categoryExists == null) {
            throw new EntityNotFoundException("Category", categoryId);
        }

        // Note: In a real application, you might want to check if products exist
        // in this category before allowing deletion
        
        boolean deleted = categoryRepository.deleteCategory(categoryId, connection);
        
        // Invalidate caches after deletion
        invalidateAllCaches();
        
        return Result.success(deleted, "Category deleted successfully");
    }

    /**
     * Checks if a category exists.
     * 
     * @param categoryId The category ID
     * @return Result containing existence status
     */
    public Result<Boolean> categoryExists(int categoryId) {
        if (categoryId <= 0) {
            return Result.failure("Invalid category ID");
        }

        boolean exists = categoryRepository.exists(categoryId, connection);
        return Result.success(exists);
    }
    
    /**
     * Gets a category by name from in-memory index.
     * 
     * @param categoryName The category name
     * @return Result containing the category or error message
     */
    public Result<Categories> getCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return Result.failure("Category name cannot be empty");
        }
        
        // Ensure cache is populated
        if (categoryNameIndex.isEmpty()) {
            getAllCategories();
        }
        
        Categories category = categoryNameIndex.get(categoryName);
        if (category == null) {
            return Result.failure("Category '" + categoryName + "' not found");
        }
        
        return Result.success(category);
    }
    
    /**
     * Invalidates all category caches.
     * Should be called after any create, update, or delete operation.
     */
    public void invalidateAllCaches() {
        categoryCache.invalidateAll();
        allCategoriesCache.invalidateAll();
        categoryNameIndex.clear();
    }
    
    /**
     * Gets cache statistics.
     */
    public String getCacheStats() {
        return String.format("Category Cache: %d entries, All Categories Cache: %d entries, Name Index: %d entries",
                categoryCache.size(), allCategoriesCache.size(), categoryNameIndex.size());
    }
}
