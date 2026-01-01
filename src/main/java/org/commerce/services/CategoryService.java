package org.commerce.services;

import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.entities.Categories;
import org.commerce.exceptions.EntityNotFoundException;
import org.commerce.repositories.CategoryRepository;
import org.commerce.repositories.interfaces.ICategoryRepository;
import org.commerce.validators.CategoryValidator;

import java.sql.Connection;
import java.util.List;

/**
 * Service layer for Category business logic.
 * Handles validation, business rules, and delegates to repository.
 */
public class CategoryService {
    private final Connection connection;
    private final ICategoryRepository categoryRepository;

    public CategoryService(Connection connection) {
        this.connection = connection;
        this.categoryRepository = new CategoryRepository();
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
        return Result.success(created, "Category created successfully");
    }

    /**
     * Retrieves a category by ID.
     * 
     * @param categoryId The category ID
     * @return Result containing the category or error message
     */
    public Result<Categories> getCategoryById(int categoryId) {
        if (categoryId <= 0) {
            return Result.failure("Invalid category ID");
        }

        Categories category = categoryRepository.getCategoryById(categoryId, connection);
        if (category == null) {
            throw new EntityNotFoundException("Category", categoryId);
        }

        return Result.success(category);
    }

    /**
     * Retrieves all categories.
     * 
     * @return Result containing list of all categories
     */
    public Result<List<Categories>> getAllCategories() {
        List<Categories> categories = categoryRepository.getAllCategories(connection);
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
}
