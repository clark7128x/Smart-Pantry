package com.smartpantry.utils;

import com.smartpantry.models.Product;
import com.smartpantry.models.Recipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Algorithm for matching recipes with available pantry ingredients.
 * Uses fuzzy matching and ingredient normalization for better results.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class RecipeMatchingAlgorithm {

    /** Minimum match percentage to consider a recipe as "cookable" */
    public static final double MIN_MATCH_THRESHOLD = 50.0;

    /** Common ingredient synonyms for better matching */
    private static final Map<String, Set<String>> INGREDIENT_SYNONYMS = new HashMap<>();

    static {
        // Initialize common synonyms
        INGREDIENT_SYNONYMS.put("chicken", Set.of("chicken breast", "chicken thigh", "poultry"));
        INGREDIENT_SYNONYMS.put("oil", Set.of("olive oil", "vegetable oil", "cooking oil", "sunflower oil"));
        INGREDIENT_SYNONYMS.put("cheese", Set.of("parmesan", "cheddar", "mozzarella", "feta"));
        INGREDIENT_SYNONYMS.put("tomato", Set.of("tomatoes", "cherry tomatoes", "roma tomatoes"));
        INGREDIENT_SYNONYMS.put("onion", Set.of("onions", "red onion", "white onion", "spring onion"));
        INGREDIENT_SYNONYMS.put("garlic", Set.of("garlic cloves", "minced garlic", "garlic powder"));
        INGREDIENT_SYNONYMS.put("egg", Set.of("eggs"));
        INGREDIENT_SYNONYMS.put("milk", Set.of("whole milk", "skim milk", "2% milk"));
    }

    private RecipeMatchingAlgorithm() {
        // Private constructor to prevent instantiation
    }

    /**
     * Finds recipes that can be made with available pantry ingredients.
     * Returns recipes sorted by match percentage (highest first).
     *
     * @param recipes  List of all recipes
     * @param products List of available products in the pantry
     * @return List of recipes with calculated match percentages, sorted by match
     */
    public static List<Recipe> findMatchingRecipes(List<Recipe> recipes, List<Product> products) {
        // Get normalized names of available ingredients
        Set<String> availableIngredients = products.stream()
                .filter(p -> p.getQuantity() > 0) // Only include products with quantity > 0
                .map(p -> normalizeIngredientName(p.getName()))
                .collect(Collectors.toSet());

        // Calculate match percentage for each recipe
        List<Recipe> matchedRecipes = new ArrayList<>();

        for (Recipe recipe : recipes) {
            double matchPercentage = calculateMatchPercentage(recipe, availableIngredients);
            recipe.setMatchPercentage(matchPercentage);
            matchedRecipes.add(recipe);
        }

        // Sort by match percentage (descending) and filter by threshold
        return matchedRecipes.stream()
                .sorted((r1, r2) -> Double.compare(r2.getMatchPercentage(), r1.getMatchPercentage()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the percentage of recipe ingredients available in the pantry.
     *
     * @param recipe               The recipe to check
     * @param availableIngredients Set of normalized available ingredient names
     * @return Percentage of ingredients available (0-100)
     */
    public static double calculateMatchPercentage(Recipe recipe, Set<String> availableIngredients) {
        Set<String> recipeIngredients = recipe.getIngredientsSet();

        if (recipeIngredients.isEmpty()) {
            return 0.0;
        }

        int matchedCount = 0;

        for (String ingredient : recipeIngredients) {
            String normalizedIngredient = normalizeIngredientName(ingredient);

            if (isIngredientAvailable(normalizedIngredient, availableIngredients)) {
                matchedCount++;
            }
        }

        return (matchedCount * 100.0) / recipeIngredients.size();
    }

    /**
     * Checks if an ingredient is available, using fuzzy matching and synonyms.
     *
     * @param ingredient           The ingredient to check
     * @param availableIngredients Set of available ingredient names
     * @return true if the ingredient is available
     */
    private static boolean isIngredientAvailable(String ingredient, Set<String> availableIngredients) {
        // Direct match
        if (availableIngredients.contains(ingredient)) {
            return true;
        }

        // Check if any available ingredient contains the target ingredient
        for (String available : availableIngredients) {
            if (available.contains(ingredient) || ingredient.contains(available)) {
                return true;
            }

            // Check synonyms
            if (areSynonyms(ingredient, available)) {
                return true;
            }

            // Fuzzy match using Levenshtein distance
            if (fuzzyMatch(ingredient, available)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if two ingredients are synonyms.
     *
     * @param ing1 First ingredient
     * @param ing2 Second ingredient
     * @return true if they are synonyms
     */
    private static boolean areSynonyms(String ing1, String ing2) {
        for (Map.Entry<String, Set<String>> entry : INGREDIENT_SYNONYMS.entrySet()) {
            String base = entry.getKey();
            Set<String> synonyms = entry.getValue();

            boolean ing1Matches = ing1.equals(base) || ing1.contains(base) ||
                    synonyms.stream().anyMatch(s -> ing1.contains(s) || s.contains(ing1));
            boolean ing2Matches = ing2.equals(base) || ing2.contains(base) ||
                    synonyms.stream().anyMatch(s -> ing2.contains(s) || s.contains(ing2));

            if (ing1Matches && ing2Matches) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs fuzzy matching between two strings.
     * Uses a simplified Levenshtein distance check.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return true if strings are similar enough
     */
    private static boolean fuzzyMatch(String s1, String s2) {
        // If one string is much longer than the other, they're probably not the same
        if (Math.abs(s1.length() - s2.length()) > 3) {
            return false;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        // Allow up to 2 character differences for short strings, or 20% for longer ones
        return distance <= Math.max(2, maxLength * 0.2);
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return The edit distance between the strings
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Normalizes an ingredient name for comparison.
     * Converts to lowercase, removes extra spaces, and handles plurals.
     *
     * @param name The ingredient name to normalize
     * @return Normalized ingredient name
     */
    public static String normalizeIngredientName(String name) {
        if (name == null) {
            return "";
        }

        String normalized = name.toLowerCase().trim();

        // Remove common suffixes
        normalized = normalized.replaceAll("\\s+", " ");

        // Handle simple plurals (not perfect, but works for most cases)
        if (normalized.endsWith("ies")) {
            normalized = normalized.substring(0, normalized.length() - 3) + "y";
        } else if (normalized.endsWith("es") && !normalized.endsWith("cheese")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("s") && normalized.length() > 3
                && !normalized.endsWith("ss")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    /**
     * Gets a list of missing ingredients for a recipe.
     *
     * @param recipe   The recipe to check
     * @param products Available products
     * @return List of missing ingredient names
     */
    public static List<String> getMissingIngredients(Recipe recipe, List<Product> products) {
        Set<String> availableIngredients = products.stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> normalizeIngredientName(p.getName()))
                .collect(Collectors.toSet());

        List<String> missing = new ArrayList<>();

        for (String ingredient : recipe.getIngredientsSet()) {
            String normalized = normalizeIngredientName(ingredient);
            if (!isIngredientAvailable(normalized, availableIngredients)) {
                missing.add(ingredient);
            }
        }

        return missing;
    }

    /**
     * Gets a list of matched ingredients for a recipe.
     *
     * @param recipe   The recipe to check
     * @param products Available products
     * @return List of matched ingredient names
     */
    public static List<String> getMatchedIngredients(Recipe recipe, List<Product> products) {
        Set<String> availableIngredients = products.stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> normalizeIngredientName(p.getName()))
                .collect(Collectors.toSet());

        List<String> matched = new ArrayList<>();

        for (String ingredient : recipe.getIngredientsSet()) {
            String normalized = normalizeIngredientName(ingredient);
            if (isIngredientAvailable(normalized, availableIngredients)) {
                matched.add(ingredient);
            }
        }

        return matched;
    }
}