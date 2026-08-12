// src/main/java/com/smartpantry/utils/ValidationUtils.java
package com.smartpantry.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Utility class for input validation.
 * Provides methods to validate various types of user input.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class ValidationUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtils() {}

    /**
     * Validates that a string is not null or empty.
     *
     * @param value the string to validate
     * @return true if the string is not null and not empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates that a string represents a valid positive number.
     *
     * @param value the string to validate
     * @return true if the string is a valid positive number
     */
    public static boolean isPositiveNumber(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            double num = Double.parseDouble(value.trim());
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates that a string represents a valid non-negative number.
     *
     * @param value the string to validate
     * @return true if the string is a valid non-negative number
     */
    public static boolean isNonNegativeNumber(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            double num = Double.parseDouble(value.trim());
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses a string to a double safely.
     *
     * @param value the string to parse
     * @param defaultValue the default value if parsing fails
     * @return the parsed double or the default value
     */
    public static double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    /**
     * Validates that a date is not in the past.
     *
     * @param date the date to validate
     * @return true if the date is today or in the future
     */
    public static boolean isNotPastDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Validates that a string is a valid date in ISO format (YYYY-MM-DD).
     *
     * @param value the string to validate
     * @return true if the string is a valid date
     */
    public static boolean isValidDate(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            LocalDate.parse(value.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates product input fields.
     *
     * @param name the product name
     * @param quantity the quantity string
     * @param unit the unit of measurement
     * @return an error message if validation fails, or null if all valid
     */
    public static String validateProductInput(String name, String quantity, String unit) {
        StringBuilder errors = new StringBuilder();

        if (!isNotEmpty(name)) {
            errors.append("• Product name is required.\n");
        } else if (name.trim().length() < 2) {
            errors.append("• Product name must be at least 2 characters.\n");
        }

        if (!isNotEmpty(quantity)) {
            errors.append("• Quantity is required.\n");
        } else if (!isNonNegativeNumber(quantity)) {
            errors.append("• Quantity must be a valid non-negative number.\n");
        }

        if (!isNotEmpty(unit)) {
            errors.append("• Unit is required.\n");
        }

        return errors.length() > 0 ? errors.toString() : null;
    }

    /**
     * Validates recipe input fields.
     *
     * @param name the recipe name
     * @param ingredients the ingredients list
     * @return an error message if validation fails, or null if all valid
     */
    public static String validateRecipeInput(String name, String ingredients) {
        StringBuilder errors = new StringBuilder();

        if (!isNotEmpty(name)) {
            errors.append("• Recipe name is required.\n");
        } else if (name.trim().length() < 3) {
            errors.append("• Recipe name must be at least 3 characters.\n");
        }

        if (!isNotEmpty(ingredients)) {
            errors.append("• At least one ingredient is required.\n");
        }

        return errors.length() > 0 ? errors.toString() : null;
    }
}