// src/main/java/com/smartpantry/dao/ProductDAO.java
package com.smartpantry.dao;

import com.smartpantry.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations.
 * Handles all CRUD operations for the Products table.
 */
public class ProductDAO {

    private static ProductDAO instance;
    private final DatabaseManager dbManager;

    private ProductDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public static synchronized ProductDAO getInstance() {
        if (instance == null) {
            instance = new ProductDAO();
        }
        return instance;
    }

    /**
     * Retrieves all products from the database.
     */
    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Products ORDER BY expiration_date ASC";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    /**
     * Retrieves a product by its ID.
     */
    public Product getProductById(int id) {
        String sql = "SELECT * FROM Products WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching product by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Adds a new product to the database.
     * FIXED: Better handling of SQLite generated keys
     */
    public int addProduct(Product product) {
        String insertSql = """
            INSERT INTO Products (name, quantity, unit, expiration_date) 
            VALUES (?, ?, ?, ?)
        """;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(insertSql);

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getQuantity());
            pstmt.setString(3, product.getUnit());
            pstmt.setString(4, product.getExpirationDate() != null ?
                    product.getExpirationDate().toString() : null);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Use SQLite's last_insert_rowid() to get the generated ID
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        product.setId(generatedId);
                        System.out.println("Product added successfully with ID: " + generatedId);
                        return generatedId;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * Updates an existing product in the database.
     */
    public boolean updateProduct(Product product) {
        String sql = """
            UPDATE Products 
            SET name = ?, quantity = ?, unit = ?, expiration_date = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getQuantity());
            pstmt.setString(3, product.getUnit());
            pstmt.setString(4, product.getExpirationDate() != null ?
                    product.getExpirationDate().toString() : null);
            pstmt.setInt(5, product.getId());

            int result = pstmt.executeUpdate();
            System.out.println("Product updated. Rows affected: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a product from the database.
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            System.out.println("Product deleted. Rows affected: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Searches products by name.
     */
    public ObservableList<Product> searchProducts(String searchTerm) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Products WHERE name LIKE ? ORDER BY name";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }

        return products;
    }

    /**
     * Gets products that are expiring soon.
     */
    public ObservableList<Product> getExpiringProducts(int days) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = """
            SELECT * FROM Products 
            WHERE expiration_date IS NOT NULL 
            AND date(expiration_date) <= date('now', '+' || ? || ' days')
            AND date(expiration_date) >= date('now')
            ORDER BY expiration_date ASC
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching expiring products: " + e.getMessage());
        }

        return products;
    }

    /**
     * Gets all product names for recipe matching.
     */
    public List<String> getAllProductNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT LOWER(name) as name FROM Products WHERE quantity > 0";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                names.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching product names: " + e.getMessage());
        }

        return names;
    }

    /**
     * Maps a ResultSet row to a Product object.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setQuantity(rs.getDouble("quantity"));
        product.setUnit(rs.getString("unit"));

        String dateStr = rs.getString("expiration_date");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                product.setExpirationDate(LocalDate.parse(dateStr));
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dateStr);
            }
        }

        return product;
    }
}