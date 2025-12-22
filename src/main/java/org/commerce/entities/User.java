package org.commerce.entities;
import org.commerce.enums.UserRole;

import java.time.LocalDateTime;

public class User{
  private  int id;
  private String firstname;
  private String lastname;
  private String phone;
  private UserRole userRole;
  private String email;
  private String password;
  private LocalDateTime createdAt;

   public User() {
       this.userRole = UserRole.CUSTOMER; // Default role
   }

   public User(int id, String firstname, String lastname, String phone, UserRole userRole, String email, String password, LocalDateTime createdAt) {
       this.id = id;
       this.firstname = firstname;
       this.lastname = lastname;
       this.phone = phone;
       this.userRole = userRole;
       this.email = email;
       this.password = password;
       this.createdAt = createdAt;
   }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public UserRole getUserRole() {
        return userRole;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
