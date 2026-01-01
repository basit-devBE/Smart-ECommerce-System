package org.commerce.repositories;

import org.commerce.exceptions.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base repository class that provides common database operations.
 * All repository classes should extend this to eliminate code duplication.
 */
public abstract class BaseRepository {
    
    /**
     * Executes a query that returns a single result.
     * 
     * @param connection The database connection
     * @param sql The SQL query to execute
     * @param mapper Function to map ResultSet to entity
     * @param params Query parameters
     * @return The mapped entity or null if not found
     * @throws RepositoryException if query execution fails
     */
    protected <T> T executeQuerySingle(Connection connection, String sql, 
                                       ResultSetMapper<T> mapper, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RepositoryException("Query execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes a query that returns a list of results.
     * 
     * @param connection The database connection
     * @param sql The SQL query to execute
     * @param mapper Function to map ResultSet to entity
     * @param params Query parameters
     * @return List of mapped entities
     * @throws RepositoryException if query execution fails
     */
    protected <T> List<T> executeQueryList(Connection connection, String sql, 
                                           ResultSetMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RepositoryException("Query execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     * 
     * @param connection The database connection
     * @param sql The SQL query to execute
     * @param params Query parameters
     * @return Number of rows affected
     * @throws RepositoryException if update execution fails
     */
    protected int executeUpdate(Connection connection, String sql, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Update execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes an insert query with RETURNING clause (PostgreSQL).
     * 
     * @param connection The database connection
     * @param sql The SQL query to execute (should include RETURNING clause)
     * @param mapper Function to map ResultSet to entity
     * @param params Query parameters
     * @return The inserted entity with generated fields
     * @throws RepositoryException if insert execution fails
     */
    protected <T> T executeInsertReturning(Connection connection, String sql, 
                                           ResultSetMapper<T> mapper, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RepositoryException("Insert execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes an insert query and returns the generated key.
     * 
     * @param connection The database connection
     * @param sql The SQL query to execute
     * @param params Query parameters
     * @return The generated key
     * @throws RepositoryException if insert execution fails
     */
    protected int executeInsertWithGeneratedKey(Connection connection, String sql, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(pstmt, params);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new RepositoryException("Failed to retrieve generated key");
        } catch (SQLException e) {
            throw new RepositoryException("Insert execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if a record exists based on a query.
     * 
     * @param connection The database connection
     * @param sql The SQL query (should return count or id)
     * @param params Query parameters
     * @return true if record exists, false otherwise
     */
    protected boolean exists(Connection connection, String sql, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Existence check failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets parameters on a PreparedStatement.
     * 
     * @param pstmt The PreparedStatement
     * @param params The parameters to set
     * @throws SQLException if parameter setting fails
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
    
    /**
     * Functional interface for mapping ResultSet to entity.
     */
    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
