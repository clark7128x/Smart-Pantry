// src/main/java/com/smartpantry/models/Product.java
package com.smartpantry.models;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Model class representing a product in the pantry.
 * Uses JavaFX properties for easy binding with TableView.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class Product {

    /** Unique identifier for the product */
    private final IntegerProperty id;

    /** Name of the product */
    private final StringProperty name;

    /** Quantity of the product in stock */
    private final DoubleProperty quantity;

    /** Unit of measurement (e.g., kg, liters, pieces) */
    private final StringProperty unit;

    /** Expiration date of the product */
    private final ObjectProperty<LocalDate> expirationDate;

    /** Number of days before expiration to trigger warning */
    public static final int EXPIRATION_WARNING_DAYS = 2;

    /**
     * Default constructor initializing all properties.
     */
    public Product() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.quantity = new SimpleDoubleProperty();
        this.unit = new SimpleStringProperty();
        this.expirationDate = new SimpleObjectProperty<>();
    }

    /**
     * Constructor with all parameters.
     *
     * @param id unique identifier
     * @param name product name
     * @param quantity product quantity
     * @param unit unit of measurement
     * @param expirationDate expiration date
     */
    public Product(int id, String name, double quantity, String unit, LocalDate expirationDate) {
        this();
        setId(id);
        setName(name);
        setQuantity(quantity);
        setUnit(unit);
        setExpirationDate(expirationDate);
    }

    /**
     * Constructor without ID (for new products).
     *
     * @param name product name
     * @param quantity product quantity
     * @param unit unit of measurement
     * @param expirationDate expiration date
     */
    public Product(String name, double quantity, String unit, LocalDate expirationDate) {
        this(0, name, quantity, unit, expirationDate);
    }

    // ID Property
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Name Property
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    // Quantity Property
    public double getQuantity() { return quantity.get(); }
    public void setQuantity(double quantity) { this.quantity.set(quantity); }
    public DoubleProperty quantityProperty() { return quantity; }

    // Unit Property
    public String getUnit() { return unit.get(); }
    public void setUnit(String unit) { this.unit.set(unit); }
    public StringProperty unitProperty() { return unit; }

    // Expiration Date Property
    public LocalDate getExpirationDate() { return expirationDate.get(); }
    public void setExpirationDate(LocalDate date) { this.expirationDate.set(date); }
    public ObjectProperty<LocalDate> expirationDateProperty() { return expirationDate; }

    /**
     * Checks if the product is expiring soon (within warning days).
     *
     * @return true if expiring within EXPIRATION_WARNING_DAYS days
     */
    public boolean isExpiringSoon() {
        if (expirationDate.get() == null) return false;
        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate.get());
        return daysUntilExpiration >= 0 && daysUntilExpiration <= EXPIRATION_WARNING_DAYS;
    }

    /**
     * Checks if the product has already expired.
     *
     * @return true if the product is past its expiration date
     */
    public boolean isExpired() {
        if (expirationDate.get() == null) return false;
        return expirationDate.get().isBefore(LocalDate.now());
    }

    /**
     * Gets the number of days until expiration.
     *
     * @return days until expiration (negative if expired)
     */
    public long getDaysUntilExpiration() {
        if (expirationDate.get() == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), expirationDate.get());
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f %s)", getName(), getQuantity(), getUnit());
    }
}