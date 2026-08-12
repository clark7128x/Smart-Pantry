// src/main/java/com/smartpantry/models/Recipe.java
package com.smartpantry.models;

import javafx.beans.property.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Model class representing a recipe.
 * Contains ingredients list and cooking instructions.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class Recipe {

    /** Unique identifier for the recipe */
    private final IntegerProperty id;

    /** Name of the recipe */
    private final StringProperty name;

    /** Comma-separated list of ingredients */
    private final StringProperty ingredientsList;

    /** Cooking instructions */
    private final StringProperty instructions;

    /** Match percentage with available pantry items */
    private final DoubleProperty matchPercentage;

    /**
     * Default constructor initializing all properties.
     */
    public Recipe() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.ingredientsList = new SimpleStringProperty();
        this.instructions = new SimpleStringProperty();
        this.matchPercentage = new SimpleDoubleProperty(0);
    }

    /**
     * Constructor with all parameters.
     *
     * @param id unique identifier
     * @param name recipe name
     * @param ingredientsList comma-separated ingredients
     * @param instructions cooking instructions
     */
    public Recipe(int id, String name, String ingredientsList, String instructions) {
        this();
        setId(id);
        setName(name);
        setIngredientsList(ingredientsList);
        setInstructions(instructions);
    }

    /**
     * Constructor without ID (for new recipes).
     *
     * @param name recipe name
     * @param ingredientsList comma-separated ingredients
     * @param instructions cooking instructions
     */
    public Recipe(String name, String ingredientsList, String instructions) {
        this(0, name, ingredientsList, instructions);
    }

    // ============ ID Property ============
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // ============ Name Property ============
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    // ============ Ingredients List Property ============
    public String getIngredientsList() {
        return ingredientsList.get();
    }

    public void setIngredientsList(String ingredientsList) {
        this.ingredientsList.set(ingredientsList);
    }

    public StringProperty ingredientsListProperty() {
        return ingredientsList;
    }

    // ============ Instructions Property ============
    public String getInstructions() {
        return instructions.get();
    }

    public void setInstructions(String instructions) {
        this.instructions.set(instructions);
    }

    public StringProperty instructionsProperty() {
        return instructions;
    }

    // ============ Match Percentage Property ============
    public double getMatchPercentage() {
        return matchPercentage.get();
    }

    public void setMatchPercentage(double percentage) {
        this.matchPercentage.set(percentage);
    }

    public DoubleProperty matchPercentageProperty() {
        return matchPercentage;
    }

    // ============ Helper Methods for Ingredients ============

    /**
     * Parses the ingredients list and returns as a List of strings.
     * Each ingredient is trimmed and converted to lowercase.
     *
     * @return List of individual ingredients
     */
    public List<String> getIngredientsAsList() {
        if (ingredientsList.get() == null || ingredientsList.get().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(ingredientsList.get().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Parses the ingredients list and returns as a Set of strings.
     * Each ingredient is trimmed and converted to lowercase.
     * Using a Set eliminates duplicates and allows for faster lookups.
     *
     * @return Set of individual ingredients
     */
    public Set<String> getIngredientsSet() {
        if (ingredientsList.get() == null || ingredientsList.get().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(ingredientsList.get().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Gets the count of ingredients in this recipe.
     *
     * @return number of ingredients
     */
    public int getIngredientCount() {
        return getIngredientsAsList().size();
    }

    /**
     * Checks if this recipe contains a specific ingredient.
     * Uses case-insensitive matching.
     *
     * @param ingredient the ingredient to check
     * @return true if the recipe contains the ingredient
     */
    public boolean hasIngredient(String ingredient) {
        if (ingredient == null || ingredient.isEmpty()) {
            return false;
        }
        return getIngredientsSet().contains(ingredient.toLowerCase().trim());
    }

    @Override
    public String toString() {
        return String.format("%s (%d ingredients)", getName(), getIngredientCount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return getId() == recipe.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}