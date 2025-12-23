package org.commerce.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.commerce.entities.User;
import org.commerce.enums.UserRole;

public class UserRepository {

    public User createUser(User user,Connection connection){
        String SQL = "INSERT INTO users (firstname,lastname,phone,userRole, email, password) VALUES (?, ?, ?, ?, ?, ?) RETURNING *";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastname());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getUserRole().toString());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPassword());
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                user.setId(rs.getInt("id"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                System.out.println("User created successfully.");
                return user;
            }
        }catch(SQLException e){
            System.err.println("Failed to create user: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteUser(int userId, Connection connection){
        String SQL = "DELETE FROM users WHERE id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("User deleted successfully.");
                return true;
            }
        }catch(SQLException e){
            System.err.println("Failed to delete user: " + e.getMessage());
        }
        return false;
    }

    public User updateUser(User user, Connection connection){
        String SQL = "UPDATE users SET firstname = ?, lastname = ?, phone = ?, userRole = ?, email = ?, password = ? WHERE id = ? RETURNING *";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastname());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getUserRole().toString());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPassword());
            pstmt.setInt(7, user.getId());
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                System.out.println("User updated successfully.");
                return user;
            }
        }catch(SQLException e){
            System.err.println("Failed to update user: " + e.getMessage());
        }
        return null;
    }

    public User getUserById(int userId, Connection connection){
        String SQL = "SELECT * FROM users WHERE id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
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
        }catch(SQLException e){
            System.err.println("Failed to retrieve user: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers(Connection connection){
        String SQL = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setUserRole(UserRole.valueOf(rs.getString("userRole")));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                users.add(user);
            }
        }catch(SQLException e){
            System.err.println("Failed to retrieve users: " + e.getMessage());
        }
        return users;
    }
}
