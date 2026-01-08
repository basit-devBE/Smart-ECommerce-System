package org.commerce.daos.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.commerce.daos.entities.User;
import org.commerce.enums.UserRole;
import org.commerce.daos.repositories.interfaces.IUserRepository;

/**
 * Repository implementation for User entity operations.
 * Uses BaseRepository to eliminate code duplication.
 */
public class UserRepository extends BaseRepository implements IUserRepository {

    @Override
    public User createUser(User user, Connection connection) {
        String SQL = "INSERT INTO users (firstname, lastname, phone, userRole, email, password) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapUser,
            user.getFirstname(),
            user.getLastname(),
            user.getPhone(),
            user.getUserRole().toString(),
            user.getEmail(),
            user.getPassword()
        );
    }

    @Override
    public User getUserById(int userId, Connection connection) {
        String SQL = "SELECT * FROM users WHERE id = ?";
        return executeQuerySingle(connection, SQL, this::mapUser, userId);
    }

    @Override
    public User getUserByEmail(String email, Connection connection) {
        String SQL = "SELECT * FROM users WHERE email = ?";
        return executeQuerySingle(connection, SQL, this::mapUser, email);
    }

    @Override
    public List<User> getAllUsers(Connection connection) {
        String SQL = "SELECT * FROM users";
        return executeQueryList(connection, SQL, this::mapUser);
    }

    @Override
    public User updateUser(User user, Connection connection) {
        String SQL = "UPDATE users SET firstname = ?, lastname = ?, phone = ?, userRole = ?, " +
                     "email = ?, password = ? WHERE id = ? RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapUser,
            user.getFirstname(),
            user.getLastname(),
            user.getPhone(),
            user.getUserRole().toString(),
            user.getEmail(),
            user.getPassword(),
            user.getId()
        );
    }

    @Override
    public boolean deleteUser(int userId, Connection connection) {
        String SQL = "DELETE FROM users WHERE id = ?";
        return executeUpdate(connection, SQL, userId) > 0;
    }

    @Override
    public boolean existsByEmail(String email, Connection connection) {
        String SQL = "SELECT COUNT(*) FROM users WHERE email = ?";
        return exists(connection, SQL, email);
    }

    /**
     * Maps a ResultSet row to a User entity.
     */
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstname(rs.getString("firstname"));
        user.setLastname(rs.getString("lastname"));
        user.setPhone(rs.getString("phone"));
        user.setUserRole(UserRole.valueOf(rs.getString("userRole")));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}
