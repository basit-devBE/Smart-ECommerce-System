package org.commerce;

import javafx.application.Application;

/**
 * Main entry point for the Smart E-Commerce Application
 * Launches the JavaFX UI
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Starting Smart E-Commerce Application...");
        
        // Launch JavaFX Application
        Application.launch(ECommerceApp.class, args);
    }
}
