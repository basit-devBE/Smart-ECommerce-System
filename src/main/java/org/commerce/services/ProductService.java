package org.commerce.services;

import org.commerce.entities.Product;
import org.commerce.repositories.ProductRepository;

import java.sql.Connection;
import java.math.BigDecimal;

public class ProductService {
    private final Connection connection;
    private final ProductRepository productRepository;

    public ProductService(Connection connection ) {
        this.connection = connection;
        this.productRepository = new ProductRepository();
    }

    public Product createProduct(Product product){
        if(product == null){
            System.err.println("Product cannot be null");
            return null;
        }

        if(product.getProductName() == null || product.getProductName().isEmpty()){
            System.err.println("Product name is required");
            return null;
        }

        if(product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0){
            System.err.println("Price must be greater than zero");
            return null;
        }

        if(product.getCategoryId() <= 0){
            System.err.println("Category ID is required");
            return null;
        }

        return productRepository.createProduct(product, connection);
    }

    public boolean deleteProduct(int productId){
        if(productId <= 0){
            System.err.println("Invalid product ID");
            return false;
        }

        Product productExists = productRepository.getProductById(productId, connection);
        if(productExists == null){
            System.err.println("Product does not exist");
            return false;
        }

        return productRepository.deleteProduct(productId, connection);
    }

    public Product editProduct(Product product){
        if(product.getId() <= 0){
            System.err.println("Invalid product ID");
            return null;
        }

        Product productExists = productRepository.getProductById(product.getId(), connection);
        if(productExists == null){
            System.err.println("Product does not exist");
            return null;
        }

        // Merge: use new values if provided, otherwise keep existing
        if(product.getProductName() != null && !product.getProductName().isEmpty()){
            productExists.setProductName(product.getProductName());
        }

        if(product.getDescription() != null){
            productExists.setDescription(product.getDescription());
        }

        if(product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) > 0){
            productExists.setPrice(product.getPrice());
        }

        if(product.getCategoryId() > 0){
            productExists.setCategoryId(product.getCategoryId());
        }

        return productRepository.updateProduct(productExists, connection);
    }
}
