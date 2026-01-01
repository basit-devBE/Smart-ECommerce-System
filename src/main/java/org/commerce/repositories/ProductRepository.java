package org.commerce.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.commerce.entities.Product;
import org.commerce.repositories.interfaces.IProductRepository;

/**
 * Repository implementation for Product entity operations.
 * Uses BaseRepository to eliminate code duplication.
 */
public class ProductRepository extends BaseRepository implements IProductRepository {
    
    @Override
    public Product createProduct(Product product, Connection connection) {
        String SQL = "INSERT INTO products (product_name, description, price, category_id) " +
                     "VALUES (?, ?, ?, ?) RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapProduct,
            product.getProductName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategoryId()
        );
    }
    
    @Override
    public Product getProductById(int productId, Connection connection) {
        String SQL = "SELECT * FROM products WHERE id = ?";
        return executeQuerySingle(connection, SQL, this::mapProduct, productId);
    }
    
    @Override
    public List<Product> getAllProducts(Connection connection) {
        String SQL = "SELECT * FROM products";
        return executeQueryList(connection, SQL, this::mapProduct);
    }
    
    @Override
    public Product updateProduct(Product product, Connection connection) {
        String SQL = "UPDATE products SET product_name = ?, description = ?, price = ?, " +
                     "category_id = ? WHERE id = ? RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapProduct,
            product.getProductName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategoryId(),
            product.getId()
        );
    }
    
    @Override
    public boolean deleteProduct(int productId, Connection connection) {
        String SQL = "DELETE FROM products WHERE id = ?";
        return executeUpdate(connection, SQL, productId) > 0;
    }
    
    @Override
    public int getTotalStock(int productId, Connection connection) {
        String SQL = "SELECT COALESCE(SUM(quantity), 0) as total_stock FROM inventory WHERE product_id = ?";
        Integer stock = executeQuerySingle(connection, SQL, rs -> rs.getInt("total_stock"), productId);
        return stock != null ? stock : 0;
    }
    
    @Override
    public boolean existsByName(String productName, Connection connection) {
        String SQL = "SELECT COUNT(*) FROM products WHERE product_name = ?";
        return exists(connection, SQL, productName);
    }
    
    @Override
    public List<Product> searchProducts(String searchTerm, Connection connection) {
        // Uses GIN trigram indexes for optimized pattern matching
        String SQL = "SELECT * FROM products " +
                     "WHERE product_name ILIKE ? OR description ILIKE ? " +
                     "ORDER BY product_name";
        String pattern = "%" + searchTerm + "%";
        return executeQueryList(connection, SQL, this::mapProduct, pattern, pattern);
    }
    
    @Override
    public List<Product> searchProductsByCategory(Integer categoryId, String searchTerm, Connection connection) {
        // Uses composite index on category_id and product_name for optimization
        StringBuilder SQL = new StringBuilder("SELECT * FROM products WHERE 1=1");
        
        if (categoryId != null) {
            SQL.append(" AND category_id = ?");
        }
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            SQL.append(" AND (product_name ILIKE ? OR description ILIKE ?)");
        }
        
        SQL.append(" ORDER BY product_name");
        
        // Build parameters dynamically
        if (categoryId != null && searchTerm != null && !searchTerm.trim().isEmpty()) {
            String pattern = "%" + searchTerm + "%";
            return executeQueryList(connection, SQL.toString(), this::mapProduct, categoryId, pattern, pattern);
        } else if (categoryId != null) {
            return executeQueryList(connection, SQL.toString(), this::mapProduct, categoryId);
        } else if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String pattern = "%" + searchTerm + "%";
            return executeQueryList(connection, SQL.toString(), this::mapProduct, pattern, pattern);
        } else {
            return getAllProducts(connection);
        }
    }
    
    /**
     * Maps a ResultSet row to a Product entity.
     */
    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }
}
