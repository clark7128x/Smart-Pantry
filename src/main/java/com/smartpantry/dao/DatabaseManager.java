// src/main/java/com/smartpantry/dao/DatabaseManager.java
package com.smartpantry.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class managing the SQLite database connection.
 * Handles database initialization and connection pooling.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class DatabaseManager {

    /** Singleton instance */
    private static DatabaseManager instance;

    /** Database connection */
    private Connection connection;

    /** Database file path */
    private static final String DB_URL = "jdbc:sqlite:smart_pantry.db";

    /**
     * Private constructor for singleton pattern.
     */
    private DatabaseManager() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Gets the singleton instance of DatabaseManager.
     *
     * @return the singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Gets the database connection, creating one if necessary.
     *
     * @return the database connection
     * @throws SQLException if connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign keys
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /**
     * Initializes the database by creating necessary tables.
     * This method is called on application startup.
     */
    public void initializeDatabase() {
        try (Statement statement = getConnection().createStatement()) {

            // Create Products table
            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS Products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    quantity REAL NOT NULL DEFAULT 0,
                    unit TEXT NOT NULL,
                    expiration_date TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            statement.execute(createProductsTable);

            // Create Recipes table
            String createRecipesTable = """
                CREATE TABLE IF NOT EXISTS Recipes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    ingredients_list TEXT NOT NULL,
                    instructions TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            statement.execute(createRecipesTable);

            // Create index for faster ingredient searching
            String createIngredientIndex = """
                CREATE INDEX IF NOT EXISTS idx_products_name 
                ON Products(name COLLATE NOCASE)
            """;
            statement.execute(createIngredientIndex);

            // Insert sample data if tables are empty
            insertSampleData(statement);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts sample data for demonstration purposes.
     *
     * @param statement SQL statement object
     * @throws SQLException if insertion fails
     */
    private void insertSampleData(Statement statement) throws SQLException {
        // Check if products table is empty
        var rs = statement.executeQuery("SELECT COUNT(*) FROM Products");
        if (rs.next() && rs.getInt(1) == 0) {
            // Insert sample products
            String[] sampleProducts = {
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Milk', 2, 'liters', date('now', '+3 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Eggs', 12, 'pieces', date('now', '+7 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Bread', 1, 'loaf', date('now', '+1 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Chicken Breast', 500, 'grams', date('now', '+2 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Rice', 1000, 'grams', date('now', '+180 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Tomatoes', 6, 'pieces', date('now', '+5 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Onions', 5, 'pieces', date('now', '+14 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Garlic', 2, 'heads', date('now', '+21 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Olive Oil', 500, 'ml', date('now', '+365 days'))",
                    "INSERT INTO Products (name, quantity, unit, expiration_date) VALUES ('Pasta', 500, 'grams', date('now', '+365 days'))"
            };

            for (String sql : sampleProducts) {
                statement.execute(sql);
            }
            System.out.println("Sample products inserted.");
        }

        // Check if recipes table is empty
        rs = statement.executeQuery("SELECT COUNT(*) FROM Recipes");
        if (rs.next() && rs.getInt(1) == 0) {
            // Insert sample recipes
            String[] sampleRecipes = {
                    """
                INSERT INTO Recipes (name, ingredients_list, instructions) VALUES 
                ('Pasta Pomodoro', 'pasta, tomatoes, garlic, olive oil, onions', 
                '1. Boil pasta according to package directions.
                2. Dice tomatoes and mince garlic.
                3. Sauté onions and garlic in olive oil.
                4. Add tomatoes and simmer for 10 minutes.
                5. Toss with drained pasta and serve.')
                """,
                    """
                INSERT INTO Recipes (name, ingredients_list, instructions) VALUES 
                ('Scrambled Eggs', 'eggs, milk, butter', 
                '1. Whisk eggs with a splash of milk.
                2. Melt butter in a non-stick pan over medium heat.
                3. Pour in eggs and stir gently.
                4. Cook until just set. Season and serve.')
                """,
                    """
                INSERT INTO Recipes (name, ingredients_list, instructions) VALUES 
                ('Chicken Rice Bowl', 'chicken breast, rice, garlic, olive oil, onions', 
                '1. Cook rice according to package directions.
                2. Season and grill chicken breast until cooked through.
                3. Sauté garlic and onions in olive oil.
                4. Slice chicken and serve over rice with sautéed vegetables.')
                """,
                    """
                INSERT INTO Recipes (name, ingredients_list, instructions) VALUES 
                ('French Toast', 'bread, eggs, milk', 
                '1. Whisk eggs with milk.
                2. Dip bread slices in the mixture.
                3. Fry in butter until golden on both sides.
                4. Serve with maple syrup or powdered sugar.')
                """,
                    """
                INSERT INTO Recipes (name, ingredients_list, instructions) VALUES 
                ('Garlic Rice', 'rice, garlic, olive oil', 
                '1. Cook rice and let it cool slightly.
                2. Mince garlic and sauté in olive oil until golden.
                3. Add cooked rice and stir-fry for 2-3 minutes.
                4. Season with salt and serve.')
                """
            };

            for (String sql : sampleRecipes) {
                statement.execute(sql);
            }
            System.out.println("Sample recipes inserted.");
        }
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}