package org.commerce.services;

import java.sql.Connection;
import org.commerce.entities.User;
import org.commerce.repositories.UserRepository;

public class UserService {
    private final Connection connection;
    private final UserRepository userRepository;

    public UserService(Connection connection) {
        this.connection = connection;
        this.userRepository = new UserRepository();
    }

    public User createUser(User user){
        if(user.getEmail() == null || user.getEmail().isEmpty()){
            System.err.println("Email is required");
            return null;
        }
        
        if(user.getPassword() == null || user.getPassword().length() < 6){
            System.err.println("Password must be at least 6 characters");
            return null;
        }
        
        return userRepository.createUser(user, connection);
    }

    public boolean deleteUser(int userId){
        if(userId <= 0){
            System.err.println("Invalid user ID");
            return false;
        }
        
        User userExists = userRepository.getUserById(userId, connection);
        if(userExists == null){
            System.err.println("User does not exist");
            return false;
        }
        
        return userRepository.deleteUser(userId, connection);
    }

    public User editUser(User user){
        if(user.getId() <= 0){
            System.err.println("Invalid user ID");
            return null;
        }

        User userExists = userRepository.getUserById(user.getId(), connection);
        if(userExists == null){
            System.err.println("User does not exist");
            return null;
        }

        // Merge: use new values if provided, otherwise keep existing
        if(user.getFirstname() != null && !user.getFirstname().isEmpty()){
            userExists.setFirstname(user.getFirstname());
        }
        if(user.getLastname() != null && !user.getLastname().isEmpty()){
            userExists.setLastname(user.getLastname());
        }
        if(user.getEmail() != null && !user.getEmail().isEmpty()){
            userExists.setEmail(user.getEmail());
        }
        if(user.getPassword() != null && user.getPassword().length() >= 6){
            userExists.setPassword(user.getPassword());
        }
        if(user.getPhone() != null && !user.getPhone().isEmpty()){
            userExists.setPhone(user.getPhone());
        }
        if(user.getUserRole() != null){
            userExists.setUserRole(user.getUserRole());
        }

        return userRepository.updateUser(userExists, connection);
    }
}
