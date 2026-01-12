# Smart E-Commerce System

A comprehensive e-commerce management system built with JavaFX, PostgreSQL, and MongoDB, featuring advanced caching, product management, inventory tracking, user authentication, and customer review capabilities.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Key Components](#key-components)
- [API Documentation](#api-documentation)
- [Caching System](#caching-system)
- [Review System](#review-system)
- [Activity Logging](#activity-logging)
- [Contributing](#contributing)
- [License](#license)

---

## 🌟 Overview

The Smart E-Commerce System is a desktop application designed for managing modern e-commerce operations. It provides two distinct interfaces:
- **Admin Dashboard**: For managing products, categories, inventory, and viewing system analytics
- **Customer Portal**: For browsing products, viewing details, adding items to cart, and leaving reviews

The system leverages a hybrid database architecture using PostgreSQL for transactional data and MongoDB for flexible schema requirements like reviews and activity logs.

---

## ✨ Features

### Core Functionality
- **User Authentication & Authorization**
  - Role-based access control (Admin/Customer)
  - BCrypt password hashing (12 rounds)
  - Session management

- **Product Management**
  - CRUD operations for products
  - Category-based organization
  - Advanced search with PostgreSQL full-text search
  - Product sorting (by name, price, date, category)
  - Image support (future enhancement)

- **Inventory Management**
  - Multi-warehouse support
  - Real-time stock tracking
  - Inventory level monitoring
  - Automatic cache invalidation

- **Category Management**
  - Hierarchical category structure
  - Category-based filtering
  - Dynamic category assignment

- **Customer Reviews (MongoDB)**
  - 5-star rating system
  - Title and comment fields
  - Verified purchase badges
  - Helpful vote tracking
  - Average rating calculation
  - Rating distribution analytics

- **Activity Logging (MongoDB)**
  - User action tracking
  - Login/logout monitoring
  - Review submission logging
  - Application lifecycle events
  - Metadata support for contextual information

### Advanced Features
- **High-Performance Caching**
  - LRU (Least Recently Used) eviction policy
  - TTL (Time To Live) support
  - Concurrent access handling
  - Cache statistics and monitoring
  - Automatic invalidation on updates

- **Search Optimization**
  - PostgreSQL GIN indexes
  - Full-text search capabilities
  - Category-based filtering
  - Optimized query performance

- **Performance Monitoring**
  - Operation timing
  - Cache hit/miss tracking
  - Query performance metrics

---

## 🏗️ Architecture

### Application Architecture (MVC + DAO Pattern)

```
┌─────────────────────────────────────────────────────────┐
│              Controllers (Presentation Layer)            │
│  LoginController, DashboardController, ProductListing    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                Services (Business Logic)                 │
│  UserService, ProductService, CategoryService,           │
│  InventoryService, ReviewService, ActivityLogService     │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                   DAOs (Data Access)                     │
│  ├─ Entities (Domain Models)                             │
│  ├─ Repositories (Data Operations)                       │
│  └─ Models (Table Initializers)                          │
└─────────────────────┬───────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │                           │
┌───────▼──────────┐      ┌────────▼─────────┐
│  PostgreSQL Repos │      │  MongoDB Repos    │
│   - Users         │      │   - Reviews       │
│   - Products      │      │   - ActivityLogs  │
│   - Categories    │      │                   │
│   - Inventory     │      │                   │
│   - Orders        │      │                   │
└───────┬──────────┘      └────────┬─────────┘
        │                           │
┌───────▼──────────┐      ┌────────▼─────────┐
│   PostgreSQL DB   │      │    MongoDB Atlas  │
│  (Transactional)  │      │  (Document Store) │
└───────────────────┘      └───────────────────┘
```

### Layer Responsibilities

**Controllers → Services → DAOs**

1. **Controllers**: Handle UI events, user input, and view updates
2. **Services**: Business logic, validation, caching, transactions
3. **DAOs**: Database operations, query execution, data mapping

### Hybrid Database Strategy

**PostgreSQL** - Used for:
- User accounts and authentication
- Product catalog
- Categories
- Inventory tracking
- Orders and order items
- Data requiring ACID transactions
- Complex relational queries

**MongoDB** - Used for:
- Product reviews (flexible schema)
- Activity logs (high-write volume)
- User-generated content
- Analytics and metrics
- Non-critical data

---

## 💻 Technology Stack

### Backend
- **Java 25**: Core application language
- **JavaFX 21.0.1**: Desktop UI framework
- **PostgreSQL 42.7.8**: Relational database
- **MongoDB Driver 5.7.0**: NoSQL database connectivity
- **BCrypt (jbcrypt 0.4)**: Password hashing and security
- **JDBC**: Database connection management

### Frontend
- **JavaFX FXML**: Declarative UI markup
- **CSS**: Custom styling
- **Scene Builder**: UI design tool

### Build & Dependencies
- **Maven**: Build automation and dependency management
- **Maven Compiler Plugin 3.1**: Java compilation
- **JavaFX Maven Plugin 0.0.8**: JavaFX integration

### Development Tools
- **Git**: Version control
- **VS Code**: Development environment
- **DBeaver/pgAdmin**: Database management

---

## 📦 Prerequisites

### Required Software
- **Java Development Kit (JDK) 25** or higher
- **Apache Maven 3.6+**
- **PostgreSQL 12+**
- **MongoDB Atlas Account** (or local MongoDB 4.4+)

### System Requirements
- **OS**: Linux, macOS, or Windows
- **RAM**: Minimum 4GB (8GB recommended)
- **Disk Space**: 500MB for application and dependencies
- **Display**: 1400x900 resolution or higher

---

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/basit-devBE/Smart-ECommerce-System.git
cd Smart-ECommerce-System
```

### 2. Install PostgreSQL

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### macOS (Homebrew)
```bash
brew install postgresql@14
brew services start postgresql@14
```

#### Windows
Download and install from [PostgreSQL Official Website](https://www.postgresql.org/download/windows/)

### 3. Set Up MongoDB Atlas

1. Create a free account at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a new cluster
3. Set up database user credentials
4. Whitelist your IP address (or use 0.0.0.0/0 for development)
5. Get your connection string

### 4. Install Maven Dependencies

```bash
mvn clean install
```

---

## ⚙️ Configuration

### Database Configuration

#### PostgreSQL Setup

1. **Create Database and User**

```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE SmartEcommerce;
CREATE USER basit WITH ENCRYPTED PASSWORD 'bece2018';
GRANT ALL PRIVILEGES ON DATABASE SmartEcommerce TO basit;
\q
```

2. **Update Connection Details**

Edit `src/main/java/org/commerce/config/DBConfig.java`:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/SmartEcommerce";
private static final String USER = "basit";
private static final String PASSWORD = "bece2018";
```

Or set environment variables in `run.sh`:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=SmartEcommerce
export DB_USER=basit
export DB_PASSWORD=bece2018
```

#### MongoDB Configuration

Edit `src/main/java/org/commerce/config/MongoDBConfig.java` or set environment variables:

```bash
export MONGO_URI="mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/?appName=Cluster0"
export MONGO_DB="ecommerce_reviews"
```

### JVM Memory Configuration

The `run.sh` script includes optimized memory settings:

```bash
export MAVEN_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"
```

Adjust based on your system:
- **-Xms**: Initial heap size (512MB)
- **-Xmx**: Maximum heap size (2GB)
- **-XX:+UseG1GC**: Garbage collector (G1 recommended)

---

## 🗄️ Database Setup

### Automatic Initialization

The application automatically creates all required tables on first run:

- `users` - User accounts
- `categories` - Product categories
- `products` - Product catalog
- `inventory` - Stock management
- `orders` - Order tracking
- `order_items` - Order line items
- `reviews` - Review metadata (PostgreSQL)

MongoDB collections are created automatically:
- `reviews` - Product reviews
- `activity_logs` - User activity tracking

### Seed Data

Uncomment the following line in `ECommerceApp.java` to populate sample data:

```java
// seedData();  // Remove comment to enable
```

This will create:
- **56 categories** across multiple domains
- **92 products** with realistic data
- **166+ inventory records** across warehouses
- **1 admin user** (pre-seeded)

### Manual Database Reset

To clear and reset the database:

```sql
-- PostgreSQL
DROP DATABASE SmartEcommerce;
CREATE DATABASE SmartEcommerce;
```

```javascript
// MongoDB (using MongoDB Shell)
use ecommerce_reviews
db.reviews.deleteMany({})
db.activity_logs.deleteMany({})
```

---

## 🎯 Running the Application

### Using the Launch Script (Recommended)

```bash
chmod +x run.sh
./run.sh
```

### Using Maven Directly

```bash
mvn javafx:run
```

### Default Login Credentials

**Admin Account:**
- Email: `mohammedbasit362@gmail.com`
- Password: `bece2018`
- Role: Administrator

### Creating Additional Users

Users can be created through the admin dashboard or by direct database insertion.

---

## 📁 Project Structure

```
Smart-ECommerce-System/
├── src/
│   ├── main/
│   │   ├── java/org/commerce/
│   │   │   ├── common/                 # Shared utilities
│   │   │   │   ├── CacheManager.java   # Generic LRU cache with TTL
│   │   │   │   ├── PasswordHasher.java # BCrypt password hashing
│   │   │   │   ├── PerformanceMonitor.java  # Timing and metrics
│   │   │   │   ├── ProductComparator.java   # Sorting strategies
│   │   │   │   ├── Result.java         # Service result wrapper
│   │   │   │   └── ValidationResult.java    # Validation responses
│   │   │   │
│   │   │   ├── config/                 # Configuration
│   │   │   │   ├── DBConfig.java       # PostgreSQL configuration
│   │   │   │   └── MongoDBConfig.java  # MongoDB Atlas configuration
│   │   │   │
│   │   │   ├── controllers/            # JavaFX controllers (Presentation Layer)
│   │   │   │   ├── LoginController.java      # Authentication UI
│   │   │   │   ├── DashboardController.java  # Admin dashboard
│   │   │   │   └── ProductListingController.java # Customer portal
│   │   │   │
│   │   │   ├── daos/                   # Data Access Objects (DAO Layer)
│   │   │   │   ├── entities/           # Domain models
│   │   │   │   │   ├── User.java           # User entity
│   │   │   │   │   ├── Product.java        # Product entity
│   │   │   │   │   ├── Categories.java     # Category entity
│   │   │   │   │   ├── Inventory.java      # Inventory entity
│   │   │   │   │   ├── Orders.java         # Order entity
│   │   │   │   │   ├── OrderItems.java     # Order line items
│   │   │   │   │   ├── Review.java         # Review entity (MongoDB)
│   │   │   │   │   └── ActivityLog.java    # Activity log (MongoDB)
│   │   │   │   │
│   │   │   │   ├── models/             # Database table initializers
│   │   │   │   │   ├── UsersModel.java
│   │   │   │   │   ├── CategoriesModel.java
│   │   │   │   │   ├── ProductsModel.java
│   │   │   │   │   ├── InventoryModel.java
│   │   │   │   │   ├── OrdersModel.java
│   │   │   │   │   ├── OrderItemsModel.java
│   │   │   │   │   └── ReviewsModel.java
│   │   │   │   │
│   │   │   │   └── repositories/       # Data access layer
│   │   │   │       ├── interfaces/     # Repository contracts
│   │   │   │       │   ├── IUserRepository.java
│   │   │   │       │   ├── IProductRepository.java
│   │   │   │       │   ├── ICategoryRepository.java
│   │   │   │       │   └── IInventoryRepository.java
│   │   │   │       ├── BaseRepository.java       # Base CRUD operations
│   │   │   │       ├── UserRepository.java
│   │   │   │       ├── ProductRepository.java
│   │   │   │       ├── CategoryRepository.java
│   │   │   │       ├── InventoryRepository.java
│   │   │   │       ├── ReviewRepository.java     # MongoDB reviews
│   │   │   │       └── ActivityLogRepository.java # MongoDB logs
│   │   │   │
│   │   │   ├── enums/                  # Enumerations
│   │   │   │   └── UserRole.java       # User roles (ADMIN/CUSTOMER)
│   │   │   │
│   │   │   ├── exceptions/             # Custom exceptions
│   │   │   │   ├── CommerceException.java
│   │   │   │   ├── DatabaseConnectionException.java
│   │   │   │   ├── DuplicateEntityException.java
│   │   │   │   ├── EntityNotFoundException.java
│   │   │   │   ├── RepositoryException.java
│   │   │   │   ├── ServiceException.java
│   │   │   │   └── ValidationException.java
│   │   │   │
│   │   │   ├── services/               # Business logic layer
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ProductService.java       # With caching
│   │   │   │   ├── CategoryService.java      # With caching
│   │   │   │   ├── InventoryService.java
│   │   │   │   ├── ReviewService.java        # MongoDB reviews
│   │   │   │   └── ActivityLogService.java   # MongoDB logging
│   │   │   │
│   │   │   ├── validators/             # Input validation
│   │   │   │   ├── UserValidator.java
│   │   │   │   ├── ProductValidator.java
│   │   │   │   ├── CategoryValidator.java
│   │   │   │   └── InventoryValidator.java
│   │   │   │
│   │   │   ├── ECommerceApp.java       # Main JavaFX application
│   │   │   ├── Main.java               # Entry point
│   │   │   ├── ConsoleApp.java         # CLI interface
│   │   │   ├── SeedData.java           # Database seeding
│   │   │   ├── MongoDBTest.java        # MongoDB connection test
│   │   │   └── CacheDemo.java          # Cache demonstration
│   │   │
│   │   └── resources/
│   │       ├── views/                  # FXML view files
│   │       │   ├── login.fxml
│   │       │   ├── dashboard.fxml
│   │       │   └── product-listing.fxml
│   │       ├── styles/                 # CSS stylesheets
│   │       │   └── main.css
│   │       └── db/                     # SQL scripts
│   │           └── search_optimization.sql
├── pom.xml                             # Maven configuration
├── run.sh                              # Launch script
└── README.md                           # This file
```

---

## 🔑 Key Components

### 1. CacheManager

Generic LRU cache implementation with TTL support.

**Features:**
- Concurrent access handling (`ConcurrentHashMap`)
- Time-To-Live (TTL) expiration
- Least Recently Used (LRU) eviction
- Cache statistics (hits, misses, evictions)
- Thread-safe operations

**Usage:**
```java
CacheManager<Integer, Product> productCache = new CacheManager<>(100, 300_000); // 100 items, 5 min TTL
productCache.put(1, product);
Product cached = productCache.get(1);
```

**Integration:**
- `ProductService`: Caches products by ID and search results
- `CategoryService`: Caches categories by ID and all categories

### 2. PasswordHasher

BCrypt-based password hashing utility for secure authentication.

**Features:**
- BCrypt algorithm with configurable work factor
- Automatic salt generation
- Secure password verification
- Rehashing detection for algorithm upgrades
- Exception handling for invalid hashes

**Usage:**
```java
// Hash a password
String hashedPassword = PasswordHasher.hashPassword("myPassword123");
// Returns: $2a$12$N9qo8uLOickgx2ZMRZoMye...

// Verify a password
boolean isValid = PasswordHasher.verifyPassword("myPassword123", hashedPassword);
// Returns: true

// Check if rehashing is needed
boolean needsRehash = PasswordHasher.needsRehash(hashedPassword);
```

**Security Configuration:**
- Work factor: 12 (2^12 = 4096 rounds)
- Resistant to brute-force attacks
- Computation time: ~250-300ms per hash
- Hash length: 60 characters
- Database field: VARCHAR(255)

**Integration:**
- `UserService`: Automatically hashes passwords on user creation and updates
- `UserService.login()`: Verifies passwords using BCrypt comparison
- All password operations are transparent to controllers and UI

### 3. ProductComparator

Flexible sorting implementation with 7 strategies.

**Sorting Options:**
- `BY_NAME`: Alphabetical A-Z
- `BY_NAME_DESC`: Alphabetical Z-A
- `BY_PRICE_ASC`: Price low to high
- `BY_PRICE_DESC`: Price high to low
- `BY_NEWEST`: Most recent first
- `BY_OLDEST`: Oldest first
- `BY_CATEGORY`: Group by category

**Usage:**
```java
Collections.sort(products, ProductComparator.BY_PRICE_ASC);
```

### 4. Review System (MongoDB)

Complete review management with ratings and comments.

**Review Entity:**
```java
{
  "_id": ObjectId,
  "productId": 123,
  "userId": 456,
  "userName": "John Doe",
  "rating": 5,                    // 1-5 stars
  "title": "Great product!",
  "comment": "Highly recommend...",
  "verified": true,               // Verified purchase flag
  "helpfulCount": 10,             // Helpful votes
  "images": [],                   // Image URLs (future)
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

**Key Operations:**
- Create review with validation
- Get reviews by product/user
- Calculate average rating
- Generate rating distribution (1-5 star counts)
- Mark reviews as helpful
- Update/delete reviews

**Validation Rules:**
- Rating: 1-5 (required)
- Title: Required, non-empty
- Comment: Minimum 10 characters
- User: Must be logged in

### 4. Activity Logging (MongoDB)

Tracks user actions and system events.

**ActivityLog Entity:**
```java
{
  "_id": ObjectId,
  "userId": 123,
  "userName": "John Doe",
  "action": "LOGIN",              // Action type
  "entityType": "User",           // Entity involved
  "entityId": 123,                // Entity ID
  "metadata": {                   // Additional context
    "ipAddress": "removed",       // Removed for privacy
    "details": "..."
  },
  "timestamp": ISODate
}
```

**Tracked Actions:**
- `APP_START`: Application startup
- `APP_STOP`: Application shutdown
- `LOGIN`: User login
- `LOGIN_FAILED`: Failed login attempt
- `LOGOUT`: User logout
- `WRITE_REVIEW`: Review submission
- `VIEW_PRODUCT`: Product view (future)
- `ADD_TO_CART`: Cart addition (future)
- `PURCHASE`: Order placement (future)

**Features:**
- Silent fail logging (doesn't break app)
- Metadata support for context
- Time-based queries
- User activity history
- Action filtering

### 6. PerformanceMonitor

Tracks operation timing and performance metrics.

**Usage:**
```java
long startTime = PerformanceMonitor.startTiming("Product Search");
// ... perform operation ...
long duration = PerformanceMonitor.endTiming("Product Search", startTime);
```

**Metrics Tracked:**
- Operation duration (milliseconds)
- Cache hit/miss ratios
- Query execution time
- Search performance

---

## 📚 API Documentation

### UserService

```java
// Create new user
Result<User> createUser(User user)

// Authenticate user
Result<User> authenticateUser(String email, String password)

// Get user by ID
Result<User> getUserById(int id)

// Get user by email
Result<User> getUserByEmail(String email)

// Update user
Result<User> updateUser(User user)

// Delete user
Result<Void> deleteUser(int id)

// Get all users (admin only)
Result<List<User>> getAllUsers()
```

### ProductService (With Caching)

```java
// Create product
Result<Product> createProduct(Product product)

// Get product by ID (cached)
Result<Product> getProductById(int id)

// Get all products (cached)
Result<List<Product>> getAllProducts()

// Search products with caching
Result<List<Product>> searchProductsByCategory(Integer categoryId, String searchTerm)

// Update product (invalidates cache)
Result<Product> updateProduct(Product product)

// Delete product (invalidates cache)
Result<Void> deleteProduct(int id)

// Get total stock for product
Result<Integer> getTotalStock(int productId)

// Sort products
void sortProducts(List<Product> products, ProductComparator.SortStrategy strategy)

// Get cache statistics
String getCacheStats()
```

### CategoryService (With Caching)

```java
// Create category
Result<Categories> createCategory(Categories category)

// Get category by ID (cached)
Result<Categories> getCategoryById(int id)

// Get all categories (cached)
Result<List<Categories>> getAllCategories()

// Update category (invalidates cache)
Result<Categories> updateCategory(Categories category)

// Delete category (invalidates cache)
Result<Void> deleteCategory(int id)

// Search categories
Result<List<Categories>> searchCategories(String keyword)
```

### ReviewService (MongoDB)

```java
// Create review
Result<Review> createReview(Review review)

// Get product reviews
Result<List<Review>> getProductReviews(int productId)

// Get user reviews
Result<List<Review>> getUserReviews(int userId)

// Get average rating
Result<Double> getAverageRating(int productId)

// Get rating distribution
Result<long[]> getRatingDistribution(int productId)

// Update review
Result<Review> updateReview(Review review)

// Mark review as helpful
Result<Review> markHelpful(String reviewId)

// Delete review
Result<Void> deleteReview(String reviewId)
```

### ActivityLogService (MongoDB)

```java
// Log activity (basic)
void logActivity(int userId, String userName, String action)

// Log activity with entity
void logActivity(int userId, String userName, String action, String entityType, Integer entityId)

// Get user logs
List<ActivityLog> getUserLogs(int userId)

// Get logs by action
List<ActivityLog> getLogsByAction(String action)

// Get recent logs
List<ActivityLog> getRecentLogs(int limit)

// Get logs by date range
List<ActivityLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end)

// Get activity count
long getActivityCount(int userId, String action)
```

---

## ⚡ Caching System

### Overview

The application implements a sophisticated multi-layer caching system to optimize database queries and improve response times.

### Cache Configuration

**Product Cache:**
- Capacity: 500 items
- TTL: 5 minutes (300,000 ms)
- Cached: Individual products, search results

**Category Cache:**
- Capacity: 200 items
- TTL: 10 minutes (600,000 ms)
- Cached: Individual categories, all categories list

### Cache Invalidation

**Automatic Invalidation:**
- Product update → Invalidates product cache
- Product delete → Invalidates product cache
- Category update → Invalidates category cache
- Category delete → Invalidates category cache
- Inventory change → Invalidates related product cache

**Manual Invalidation:**
```java
productService.clearCache();      // Clear product cache
categoryService.clearCache();     // Clear category cache
```

### Cache Statistics

View real-time cache performance:

```java
String stats = productService.getCacheStats();
// Output: "Cache Stats - Hits: 1234, Misses: 56, Size: 450, Hit Rate: 95.7%"
```

### Performance Impact

**Without Cache:**
- Database query: ~50-100ms per request
- 100 requests = 5-10 seconds

**With Cache:**
- Cache hit: ~0.1-1ms per request
- 100 requests = 0.01-0.1 seconds
- **50-100x performance improvement**

---

## ⭐ Review System

### Writing Reviews

1. User clicks "View Details & Reviews" on a product
2. Dialog displays product information, existing reviews, and review form
3. User selects rating (1-5 stars)
4. User enters title and comment (min 10 characters)
5. User clicks "Submit Review"
6. System validates input and saves to MongoDB
7. Activity log records "WRITE_REVIEW" action

### Review Display

**Product Card:**
- No rating shown (performance optimization)
- Click "View Details & Reviews" to see ratings

**Review Dialog:**
- Average rating with star visualization
- Rating distribution histogram (future)
- List of recent reviews (limit 20)
- Review submission form (inline)

**Review Card:**
- User name
- Star rating (★★★★☆)
- Review date
- Review title
- Comment text
- Verified purchase badge (if applicable)

### Review Validation

```java
// Rating: 1-5 (required)
if (rating < 1 || rating > 5) {
    return Result.failure("Rating must be between 1 and 5");
}

// Title: Required
if (title == null || title.trim().isEmpty()) {
    return Result.failure("Title is required");
}

// Comment: Minimum 10 characters
if (comment == null || comment.length() < 10) {
    return Result.failure("Comment must be at least 10 characters");
}
```

### Average Rating Calculation

```java
// MongoDB aggregation pipeline
[
  { $match: { productId: 123 } },
  { $group: { _id: null, avgRating: { $avg: "$rating" } } }
]
```

---

## 📊 Activity Logging

### Logged Events

**Application Lifecycle:**
- `APP_START`: When application launches
- `APP_STOP`: When application closes

**Authentication:**
- `LOGIN`: Successful login with user details
- `LOGIN_FAILED`: Failed login attempt
- `LOGOUT`: User logout (future)

**User Actions:**
- `WRITE_REVIEW`: Review submission
- `VIEW_PRODUCT`: Product view (future)
- `ADD_TO_CART`: Cart addition (future)
- `PURCHASE`: Order placement (future)

### Privacy Considerations

**Removed Fields:**
- IP addresses (privacy concern)
- User agent strings
- Session tokens

**Retained Fields:**
- User ID and name
- Action type
- Entity references
- Timestamp
- Custom metadata

### Querying Logs

```java
// Get user activity
List<ActivityLog> userLogs = activityLogService.getUserLogs(userId);

// Get login events
List<ActivityLog> logins = activityLogService.getLogsByAction("LOGIN");

// Get recent activity
List<ActivityLog> recent = activityLogService.getRecentLogs(50);

// Get activity in date range
List<ActivityLog> range = activityLogService.getLogsByDateRange(start, end);
```

---

## 🔍 Search Optimization

### PostgreSQL Full-Text Search

**GIN Index Setup:**
```sql
CREATE INDEX idx_products_search 
ON products USING gin(to_tsvector('english', product_name || ' ' || description));
```

**Search Query:**
```sql
SELECT * FROM products 
WHERE to_tsvector('english', product_name || ' ' || description) 
      @@ plainto_tsquery('english', ?)
  AND (category_id = ? OR ? IS NULL)
ORDER BY ts_rank(to_tsvector('english', product_name || ' ' || description), 
                 plainto_tsquery('english', ?)) DESC;
```

### Search Features

- **Full-text search**: Searches product names and descriptions
- **Category filtering**: Optional category constraint
- **Relevance ranking**: Results ordered by match quality
- **Case-insensitive**: Matches regardless of case
- **Word stemming**: Finds related word forms
- **Cache integration**: Results cached for performance

---

## 🛠️ Development

### Building from Source

```bash
# Clean build
mvn clean install

# Compile only
mvn compile

# Run tests (when implemented)
mvn test

# Package JAR
mvn package

# Skip tests
mvn install -DskipTests
```

### Running Tests

```bash
# MongoDB connection test
java -cp target/classes org.commerce.MongoDBTest

# Cache demonstration
java -cp target/classes org.commerce.CacheDemo

# Console interface
java -cp target/classes org.commerce.ConsoleApp
```

### Code Style

- **Naming**: camelCase for variables/methods, PascalCase for classes
- **Indentation**: 4 spaces
- **Line length**: 120 characters max
- **Comments**: JavaDoc for public APIs
- **Packages**: Organized by layer (entities, repositories, services, etc.)

### Best Practices

1. **Always use Result<T>** for service methods
2. **Validate input** before database operations
3. **Invalidate caches** when data changes
4. **Log errors** appropriately
5. **Use transactions** for multi-step operations
6. **Close resources** in finally blocks or try-with-resources
7. **Test MongoDB connection** before critical operations

---

## 📝 Configuration Files

### pom.xml

Maven project configuration with dependencies:
- JavaFX 21.0.1
- PostgreSQL JDBC 42.7.8
- MongoDB Driver 5.7.0-alpha0

### run.sh

Launch script with environment variables and JVM options:
```bash
#!/bin/bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=SmartEcommerce
export DB_USER=basit
export DB_PASSWORD=bece2018
export MONGO_URI="mongodb+srv://..."
export MONGO_DB="ecommerce_reviews"
export MAVEN_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"
mvn javafx:run
```

### main.css

Custom stylesheet for JavaFX components:
- Button styles (primary, secondary, danger)
- Form layouts
- Product cards
- Dashboard styling

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Symptom**: Application crashes on launch with exit code 137

**Cause**: Out of memory (OOM) - too many products or reviews loaded at once

**Solution**:
```bash
# Increase heap size in run.sh
export MAVEN_OPTS="-Xms1024m -Xmx4096m -XX:+UseG1GC"
```

#### 2. PostgreSQL Connection Failed

**Symptom**: `DatabaseConnectionException: Connection refused`

**Solution**:
- Verify PostgreSQL is running: `sudo systemctl status postgresql`
- Check connection details in `DBConfig.java`
- Ensure database exists: `psql -U postgres -l`
- Test connection: `psql -U basit -d SmartEcommerce`

#### 3. MongoDB Connection Failed

**Symptom**: `MongoDB not connected`

**Solution**:
- Verify connection string in `MongoDBConfig.java`
- Check IP whitelist in MongoDB Atlas
- Test connection: Run `MongoDBTest.java`
- Ensure internet connectivity

#### 4. Blank Review Dialog

**Symptom**: "Write a Review" dialog shows white screen

**Solution**:
- This was fixed by integrating review form into product details dialog
- Update to latest code from `develop` branch
- Clear Maven cache: `mvn clean`

#### 5. Products Not Loading

**Symptom**: Product listing page is empty

**Solution**:
- Check if seed data was loaded
- Uncomment `seedData()` in `ECommerceApp.java`
- Restart application
- Verify database has products: `SELECT COUNT(*) FROM products;`

#### 6. Cache Not Working

**Symptom**: Slow performance despite caching

**Solution**:
- Check cache statistics: `productService.getCacheStats()`
- Verify TTL isn't too short
- Ensure cache isn't being cleared unnecessarily
- Check cache hit rate (should be >80% for frequent queries)

### Debug Mode

Enable verbose logging by adding to JVM arguments:
```bash
export MAVEN_OPTS="$MAVEN_OPTS -Djavafx.verbose=true"
```

---

## 🔐 Security Considerations

### Current Implementation

- **Password Storage**: ✅ **BCrypt hashed** with salt (12 rounds, production-ready)
- **SQL Injection**: Protected via prepared statements
- **Authentication**: Email/password with BCrypt verification
- **Authorization**: Role-based (ADMIN/CUSTOMER)
- **Session Management**: In-memory current user

### Password Hashing Details

The system uses BCrypt for secure password hashing:

```java
// Hashing passwords (automatic on user creation/update)
String hashedPassword = PasswordHasher.hashPassword(plainPassword);
// Result: $2a$12$N9qo8uLOickgx2ZMRZoMye...

// Verifying passwords (automatic on login)
boolean isValid = PasswordHasher.verifyPassword(plainPassword, hashedPassword);
```

**Security Features:**
- 12 work factor (4096 rounds) - resistant to brute force
- Automatic salt generation per password
- Passwords stored as 60-character hashes (VARCHAR 255)
- Even with database access, passwords remain secure
- Rehashing support for algorithm upgrades

### Recommended Additional Improvements

1. **JWT Tokens** for session management
2. **HTTPS** for data transmission
3. **Input Sanitization** for all user inputs
4. **Rate Limiting** for login attempts
5. **CSRF Protection** for web version
6. **Two-Factor Authentication** (2FA)
7. **Password complexity requirements**
8. **Account lockout after failed attempts**

---

## 🚀 Future Enhancements

### Planned Features

- [ ] Shopping cart functionality
- [ ] Order management system
- [ ] Payment gateway integration
- [ ] Email notifications
- [ ] Product image uploads
- [ ] Advanced search filters
- [ ] Product recommendations
- [ ] User wishlists
- [ ] Review moderation
- [ ] Admin analytics dashboard
- [ ] Export reports (PDF/Excel)
- [ ] Multi-language support
- [ ] Mobile responsive design
- [ ] RESTful API backend
- [ ] Real-time inventory updates

### Technical Improvements

- [ ] Unit tests (JUnit 5)
- [ ] Integration tests
- [ ] CI/CD pipeline
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Load balancing
- [ ] Database migrations (Flyway)
- [ ] API documentation (Swagger)
- [ ] Monitoring (Prometheus/Grafana)
- [ ] Logging framework (SLF4J/Logback)

---

## 👥 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Code Review Process

- All PRs require review before merging
- Must pass all tests
- Follow existing code style
- Update documentation for new features
- Add tests for bug fixes

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Mohammed Basit**
- Email: mohammedbasit362@gmail.com
- GitHub: [@basit-devBE](https://github.com/basit-devBE)

---

## 🙏 Acknowledgments

- JavaFX community for excellent UI framework
- MongoDB Atlas for free cloud database
- PostgreSQL team for robust RDBMS
- Maven for dependency management
- Open source community for inspiration

---

## 📞 Support

For issues, questions, or suggestions:
- **GitHub Issues**: [Create an issue](https://github.com/basit-devBE/Smart-ECommerce-System/issues)
- **Email**: mohammedbasit362@gmail.com
- **Documentation**: This README

---

## 📊 Project Statistics

- **Total Lines of Code**: ~8,500+
- **Java Classes**: 60+
- **Database Tables**: 7 (PostgreSQL)
- **MongoDB Collections**: 2
- **FXML Views**: 3
- **Dependencies**: 3 core libraries
- **Development Time**: 4+ weeks

---

## 🎯 Project Goals

1. **Learning**: Demonstrate full-stack Java development skills
2. **Architecture**: Showcase clean architecture and design patterns
3. **Performance**: Implement advanced caching and optimization
4. **Modern Tech**: Integrate hybrid database architecture (SQL + NoSQL)
5. **Real-World**: Build production-ready e-commerce features

---

**Built with ❤️ using Java, JavaFX, PostgreSQL, and MongoDB**

*Last Updated: January 7, 2026*
