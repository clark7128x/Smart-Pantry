// src/main/java/com/smartpantry/dao/RecipeDAO.java
package com.smartpantry.dao;

import com.smartpantry.models.Recipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.List;

/**
 * Data Access Object for Recipe operations.
 * Handles all CRUD operations for the Recipes table.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class RecipeDAO {

    /** Singleton instance */
    private static RecipeDAO instance;

    /** Database manager reference */
    private final DatabaseManager dbManager;

    /**
     * Private constructor for singleton pattern.
     */
    private RecipeDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Gets the singleton instance of RecipeDAO.
     *
     * @return the singleton instance
     */
    public static synchronized RecipeDAO getInstance() {
        if (instance == null) {
            instance = new RecipeDAO();
        }
        return instance;
    }

    /**
     * Retrieves all recipes from the database.
     *
     * @return ObservableList of all recipes
     */
    public ObservableList<Recipe> getAllRecipes() {
        ObservableList<Recipe> recipes = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Recipes ORDER BY name";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                recipes.add(mapResultSetToRecipe(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching recipes: " + e.getMessage());
            e.printStackTrace();
        }

        return recipes;
    }

    /**
     * Retrieves a recipe by its ID.
     *
     * @param id the recipe ID
     * @return the Recipe object, or null if not found
     */
    public Recipe getRecipeById(int id) {
        String sql = "SELECT * FROM Recipes WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRecipe(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching recipe by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Adds a new recipe to the database.
     *
     * @param recipe the recipe to add
     * @return the generated ID of the new recipe, or -1 if failed
     */
    public int addRecipe(Recipe recipe) {
        String sql = """
            INSERT INTO Recipes (name, ingredients_list, instructions) 
            VALUES (?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, recipe.getName());
            pstmt.setString(2, recipe.getIngredientsList());
            pstmt.setString(3, recipe.getInstructions());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    recipe.setId(generatedId);
                    return generatedId;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding recipe: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Updates an existing recipe in the database.
     *
     * @param recipe the recipe with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updateRecipe(Recipe recipe) {
        String sql = """
            UPDATE Recipes 
            SET name = ?, ingredients_list = ?, instructions = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, recipe.getName());
            pstmt.setString(2, recipe.getIngredientsList());
            pstmt.setString(3, recipe.getInstructions());
            pstmt.setInt(4, recipe.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating recipe: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a recipe from the database.
     *
     * @param id the ID of the recipe to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteRecipe(int id) {
        String sql = "DELETE FROM Recipes WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting recipe: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Searches recipes by name (case-insensitive partial match).
     *
     * @param searchTerm the search term
     * @return ObservableList of matching recipes
     */
    public ObservableList<Recipe> searchRecipes(String searchTerm) {
        ObservableList<Recipe> recipes = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Recipes WHERE name LIKE ? ORDER BY name";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                recipes.add(mapResultSetToRecipe(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching recipes: " + e.getMessage());
        }

        return recipes;
    }

    /**
     * Finds recipes that can be made with available products.
     * Uses a matching algorithm to calculate compatibility percentage.
     *
     * @param availableProducts list of available product names (lowercase)
     * @param minMatchPercentage minimum percentage of ingredients that must be available
     * @return ObservableList of matching recipes sorted by match percentage
     */
    public ObservableList<Recipe> findRecipesByIngredients(
            List<String> availableProducts, double minMatchPercentage) {

        ObservableList<Recipe> allRecipes = getAllRecipes();
        ObservableList<Recipe> matchingRecipes = FXCollections.observableArrayList();

        for (Recipe recipe : allRecipes) {
            double matchPercentage = calculateMatchPercentage(recipe, availableProducts);

            if (matchPercentage >= minMatchPercentage) {
                recipe.setMatchPercentage(matchPercentage);
                matchingRecipes.add(recipe);
            }
        }

        // Sort by match percentage (descending)
        matchingRecipes.sort((r1, r2) ->
                Double.compare(r2.getMatchPercentage(), r1.getMatchPercentage()));

        return matchingRecipes;
    }

    /**
     * Calculates the match percentage between a recipe and available products.
     * Uses fuzzy matching to account for variations in ingredient names.
     *
     * @param recipe the recipe to check
     * @param availableProducts list of available product names
     * @return percentage of ingredients available (0-100)
     */
    private double calculateMatchPercentage(Recipe recipe, List<String> availableProducts) {
        List<String> recipeIngredients = recipe.getIngredientsAsList();

        if (recipeIngredients.isEmpty()) {
            return 0.0;
        }

        int matchedCount = 0;

        for (String ingredient : recipeIngredients) {
            if (isIngredientAvailable(ingredient, availableProducts)) {
                matchedCount++;
            }
        }

        return (matchedCount * 100.0) / recipeIngredients.size();
    }

    /**
     * Checks if an ingredient is available in the pantry.
     * Uses fuzzy matching to handle variations.
     *
     * @param ingredient the ingredient to check
     * @param availableProducts list of available products
     * @return true if ingredient is available
     */
    private boolean isIngredientAvailable(String ingredient, List<String> availableProducts) {
        String normalizedIngredient = ingredient.toLowerCase().trim();

        for (String product : availableProducts) {
            String normalizedProduct = product.toLowerCase().trim();

            // Exact match
            if (normalizedProduct.equals(normalizedIngredient)) {
                return true;
            }

            // Partial match (ingredient contains product name or vice versa)
            if (normalizedProduct.contains(normalizedIngredient) ||
                    normalizedIngredient.contains(normalizedProduct)) {
                return true;
            }

            // Handle plurals (simple check)
            if (normalizedProduct.equals(normalizedIngredient + "s") ||
                    normalizedIngredient.equals(normalizedProduct + "s")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Maps a ResultSet row to a Recipe object.
     *
     * @param rs the ResultSet positioned at the current row
     * @return the mapped Recipe object
     * @throws SQLException if reading from ResultSet fails
     */
    private Recipe mapResultSetToRecipe(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe();
        recipe.setId(rs.getInt("id"));
        recipe.setName(rs.getString("name"));
        recipe.setIngredientsList(rs.getString("ingredients_list"));
        recipe.setInstructions(rs.getString("instructions"));
        return recipe;
    }
}