// src/main/java/com/smartpantry/controllers/RecipeController.java
package com.smartpantry.controllers;

import com.smartpantry.dao.ProductDAO;
import com.smartpantry.dao.RecipeDAO;
import com.smartpantry.models.Recipe;
import com.smartpantry.utils.AlertUtils;
import com.smartpantry.utils.ValidationUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Recipe view.
 * Handles recipe management and "What to Cook?" suggestions.
 */
public class RecipeController implements Initializable {

    public enum ViewMode { MANAGE, SUGGESTIONS }

    @FXML private VBox mainContainer;
    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private FlowPane recipeCardsContainer;
    @FXML private TextField txtSearch;
    @FXML private Slider sliderMatchPercentage;
    @FXML private Label lblMatchPercentage;
    @FXML private HBox filterContainer;
    @FXML private VBox addRecipeSection;
    @FXML private TextField txtRecipeName;
    @FXML private TextField txtIngredients;
    @FXML private TextArea txtInstructions;

    private RecipeDAO recipeDAO;
    private ProductDAO productDAO;
    private ObservableList<Recipe> recipeList;
    private ViewMode currentMode = ViewMode.MANAGE;

    // Food emojis for visual appeal
    private static final String[] FOOD_EMOJIS = {
            "🍝", "🍳", "🥗", "🍲", "🍜", "🥘", "🍛", "🍕",
            "🥙", "🌮", "🍱", "🥪", "🍰", "🥧", "🍪", "🧁"
    };

    private final Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        recipeDAO = RecipeDAO.getInstance();
        productDAO = ProductDAO.getInstance();

        setupSlider();
        setupSearch();
        loadRecipes();
    }

    public void setMode(ViewMode mode) {
        this.currentMode = mode;
        updateViewForMode();
    }

    private void updateViewForMode() {
        if (currentMode == ViewMode.SUGGESTIONS) {
            lblTitle.setText("🍳 What to Cook?");
            lblSubtitle.setText("Recipes you can make with your current pantry items");
            addRecipeSection.setVisible(false);
            addRecipeSection.setManaged(false);
            filterContainer.setVisible(true);
            filterContainer.setManaged(true);
            loadSuggestedRecipes();
        } else {
            lblTitle.setText("📖 Recipe Book");
            lblSubtitle.setText("Manage your favorite recipes");
            addRecipeSection.setVisible(true);
            addRecipeSection.setManaged(true);
            filterContainer.setVisible(false);
            filterContainer.setManaged(false);
            loadRecipes();
        }
    }

    private void setupSlider() {
        sliderMatchPercentage.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            lblMatchPercentage.setText(percentage + "%");
            if (currentMode == ViewMode.SUGGESTIONS) {
                loadSuggestedRecipes();
            }
        });
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentMode == ViewMode.MANAGE) {
                filterRecipes(newVal);
            } else {
                loadSuggestedRecipes();
            }
        });
    }

    private void loadRecipes() {
        recipeList = recipeDAO.getAllRecipes();
        displayRecipeCards(recipeList);
    }

    private void loadSuggestedRecipes() {
        List<String> availableProducts = productDAO.getAllProductNames();
        double minMatch = sliderMatchPercentage.getValue();

        ObservableList<Recipe> suggestions = recipeDAO.findRecipesByIngredients(
                availableProducts, minMatch
        );

        String searchTerm = txtSearch.getText();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            suggestions = suggestions.filtered(recipe ->
                    recipe.getName().toLowerCase().contains(searchTerm.toLowerCase())
            );
        }

        displayRecipeCards(suggestions);

        if (suggestions.isEmpty()) {
            showNoResultsMessage();
        }
    }

    private void displayRecipeCards(ObservableList<Recipe> recipes) {
        recipeCardsContainer.getChildren().clear();

        for (Recipe recipe : recipes) {
            recipeCardsContainer.getChildren().add(createRecipeCard(recipe));
        }
    }

    /**
     * Creates a visually appealing recipe card with emoji icon
     */
    private VBox createRecipeCard(Recipe recipe) {
        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setMinHeight(280);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 4);" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 6);" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 2;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 4);" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        ));

        // Header with emoji icon
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Create circular emoji icon
        StackPane iconContainer = createEmojiIcon(recipe.getName());

        // Recipe name
        VBox titleBox = new VBox(2);
        Label nameLabel = new Label(recipe.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#2c3e50"));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);

        // Ingredient count
        Label countLabel = new Label(recipe.getIngredientCount() + " ingredients");
        countLabel.setFont(Font.font("System", 12));
        countLabel.setTextFill(Color.web("#7f8c8d"));

        titleBox.getChildren().addAll(nameLabel, countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        header.getChildren().addAll(iconContainer, titleBox);

        // Match percentage badge (for suggestions mode)
        if (currentMode == ViewMode.SUGGESTIONS && recipe.getMatchPercentage() > 0) {
            HBox matchBox = createMatchBadge(recipe.getMatchPercentage());
            card.getChildren().add(matchBox);
        }

        // Ingredients section
        VBox ingredientsBox = new VBox(8);
        Label ingredientsTitle = new Label("🥗 Ingredients");
        ingredientsTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        ingredientsTitle.setTextFill(Color.web("#2c3e50"));

        // Show ingredients as tags
        FlowPane ingredientTags = createIngredientTags(recipe);

        ingredientsBox.getChildren().addAll(ingredientsTitle, ingredientTags);

        // Instructions preview
        VBox instructionsBox = new VBox(5);
        Label instructionsTitle = new Label("📝 Instructions");
        instructionsTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        instructionsTitle.setTextFill(Color.web("#2c3e50"));

        String instructions = recipe.getInstructions();
        if (instructions == null || instructions.isEmpty()) {
            instructions = "No instructions provided.";
        } else if (instructions.length() > 80) {
            instructions = instructions.substring(0, 80) + "...";
        }
        Label instructionsLabel = new Label(instructions);
        instructionsLabel.setFont(Font.font("System", 12));
        instructionsLabel.setTextFill(Color.web("#555555"));
        instructionsLabel.setWrapText(true);

        instructionsBox.getChildren().addAll(instructionsTitle, instructionsLabel);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn = createStyledButton("👁 View", "#3498db");
        viewBtn.setOnAction(e -> showRecipeDetails(recipe));

        buttonBox.getChildren().add(viewBtn);

        if (currentMode == ViewMode.MANAGE) {
            Button editBtn = createStyledButton("✏ Edit", "#95a5a6");
            editBtn.setOnAction(e -> handleEditRecipe(recipe));

            Button deleteBtn = createStyledButton("🗑 Delete", "#e74c3c");
            deleteBtn.setOnAction(e -> handleDeleteRecipe(recipe));

            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        } else {
            Button cookBtn = createStyledButton("🍳 Let's Cook!", "#27ae60");
            cookBtn.setOnAction(e -> showRecipeDetails(recipe));
            buttonBox.getChildren().add(cookBtn);
        }

        card.getChildren().addAll(header, ingredientsBox, instructionsBox, spacer, buttonBox);

        return card;
    }

    /**
     * Creates a circular emoji icon for the recipe
     */
    private StackPane createEmojiIcon(String recipeName) {
        StackPane container = new StackPane();
        container.setMinSize(60, 60);
        container.setMaxSize(60, 60);

        // Create colored circle background
        Circle circle = new Circle(30);
        String color = getColorForRecipe(recipeName);
        circle.setFill(Color.web(color));

        // Get appropriate emoji
        String emoji = getEmojiForRecipe(recipeName);
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(28));

        container.getChildren().addAll(circle, emojiLabel);
        return container;
    }

    /**
     * Gets an appropriate emoji based on recipe name
     */
    private String getEmojiForRecipe(String name) {
        String lowerName = name.toLowerCase();

        if (lowerName.contains("pasta") || lowerName.contains("spaghetti")) return "🍝";
        if (lowerName.contains("egg")) return "🍳";
        if (lowerName.contains("salad")) return "🥗";
        if (lowerName.contains("soup")) return "🍲";
        if (lowerName.contains("noodle") || lowerName.contains("ramen")) return "🍜";
        if (lowerName.contains("curry") || lowerName.contains("stew")) return "🥘";
        if (lowerName.contains("rice")) return "🍛";
        if (lowerName.contains("pizza")) return "🍕";
        if (lowerName.contains("sandwich") || lowerName.contains("toast")) return "🥪";
        if (lowerName.contains("taco") || lowerName.contains("mexican")) return "🌮";
        if (lowerName.contains("chicken")) return "🍗";
        if (lowerName.contains("fish") || lowerName.contains("seafood")) return "🐟";
        if (lowerName.contains("cake") || lowerName.contains("dessert")) return "🍰";
        if (lowerName.contains("cookie")) return "🍪";
        if (lowerName.contains("bread")) return "🍞";
        if (lowerName.contains("burger")) return "🍔";

        // Random emoji for others
        return FOOD_EMOJIS[Math.abs(name.hashCode()) % FOOD_EMOJIS.length];
    }

    /**
     * Gets a background color based on recipe name
     */
    private String getColorForRecipe(String name) {
        String[] colors = {
                "#FFE5E5", "#E5F3FF", "#E5FFE5", "#FFF5E5",
                "#F5E5FF", "#E5FFFF", "#FFE5F5", "#FFFDE5"
        };
        return colors[Math.abs(name.hashCode()) % colors.length];
    }

    /**
     * Creates ingredient tags
     */
    private FlowPane createIngredientTags(Recipe recipe) {
        FlowPane tagsPane = new FlowPane();
        tagsPane.setHgap(6);
        tagsPane.setVgap(6);

        List<String> ingredients = recipe.getIngredientsAsList();
        List<String> availableProducts = currentMode == ViewMode.SUGGESTIONS ?
                productDAO.getAllProductNames() : new ArrayList<>();

        int maxTags = 4;
        int count = 0;

        for (String ingredient : ingredients) {
            if (count >= maxTags) {
                // Show "+X more" label
                int remaining = ingredients.size() - maxTags;
                Label moreLabel = new Label("+" + remaining + " more");
                moreLabel.setStyle(
                        "-fx-background-color: #ecf0f1;" +
                                "-fx-padding: 4 8;" +
                                "-fx-background-radius: 12;" +
                                "-fx-font-size: 11px;" +
                                "-fx-text-fill: #7f8c8d;"
                );
                tagsPane.getChildren().add(moreLabel);
                break;
            }

            Label tag = new Label(ingredient);
            boolean isAvailable = isIngredientAvailable(ingredient, availableProducts);

            String tagStyle;
            if (currentMode == ViewMode.SUGGESTIONS) {
                tagStyle = isAvailable ?
                        "-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60;" :
                        "-fx-background-color: #ffeaa7; -fx-text-fill: #d68910;";
            } else {
                tagStyle = "-fx-background-color: #e8f4f8; -fx-text-fill: #2980b9;";
            }

            tag.setStyle(
                    tagStyle +
                            "-fx-padding: 4 10;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 11px;"
            );

            tagsPane.getChildren().add(tag);
            count++;
        }

        return tagsPane;
    }

    /**
     * Checks if ingredient is available in pantry
     */
    private boolean isIngredientAvailable(String ingredient, List<String> availableProducts) {
        String normalizedIngredient = ingredient.toLowerCase().trim();
        for (String product : availableProducts) {
            String normalizedProduct = product.toLowerCase().trim();
            if (normalizedProduct.contains(normalizedIngredient) ||
                    normalizedIngredient.contains(normalizedProduct)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates match percentage badge
     */
    private HBox createMatchBadge(double percentage) {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(8, 12, 8, 12));

        String color, bgColor, text;
        if (percentage == 100) {
            color = "#27ae60";
            bgColor = "#d5f4e6";
            text = "✅ Perfect Match! You have all ingredients!";
        } else if (percentage >= 75) {
            color = "#3498db";
            bgColor = "#e8f4f8";
            text = String.format("👍 %.0f%% Match - Almost there!", percentage);
        } else {
            color = "#f39c12";
            bgColor = "#fef5e7";
            text = String.format("📋 %.0f%% Match - Missing some ingredients", percentage);
        }

        badge.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 8;"
        );

        Label matchLabel = new Label(text);
        matchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        matchLabel.setTextFill(Color.web(color));

        badge.getChildren().add(matchLabel);
        return badge;
    }

    /**
     * Creates a styled button
     */
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;"
        );

        // Hover effect
        String hoverColor = adjustBrightness(color, -20);
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;"
        ));

        return btn;
    }

    /**
     * Adjusts color brightness
     */
    private String adjustBrightness(String hexColor, int amount) {
        try {
            Color color = Color.web(hexColor);
            double r = Math.max(0, Math.min(1, color.getRed() + amount/255.0));
            double g = Math.max(0, Math.min(1, color.getGreen() + amount/255.0));
            double b = Math.max(0, Math.min(1, color.getBlue() + amount/255.0));
            return String.format("#%02X%02X%02X",
                    (int)(r * 255), (int)(g * 255), (int)(b * 255));
        } catch (Exception e) {
            return hexColor;
        }
    }

    /**
     * Shows no results message
     */
    private void showNoResultsMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(60));
        messageBox.setPrefWidth(400);

        Label iconLabel = new Label("🍽️");
        iconLabel.setFont(Font.font(64));

        Label messageLabel = new Label("No recipes found");
        messageLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        messageLabel.setTextFill(Color.web("#2c3e50"));

        String hintText = currentMode == ViewMode.SUGGESTIONS ?
                "Try lowering the match percentage or adding more items to your pantry." :
                "Add your first recipe using the form above!";

        Label hintLabel = new Label(hintText);
        hintLabel.setFont(Font.font(14));
        hintLabel.setTextFill(Color.web("#7f8c8d"));
        hintLabel.setWrapText(true);
        hintLabel.setMaxWidth(350);
        hintLabel.setAlignment(Pos.CENTER);

        messageBox.getChildren().addAll(iconLabel, messageLabel, hintLabel);
        recipeCardsContainer.getChildren().add(messageBox);
    }

    private void filterRecipes(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            displayRecipeCards(recipeList);
        } else {
            ObservableList<Recipe> filtered = recipeList.filtered(recipe ->
                    recipe.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            recipe.getIngredientsList().toLowerCase().contains(searchTerm.toLowerCase())
            );
            displayRecipeCards(filtered);
        }
    }

    /**
     * Shows detailed view of a recipe
     */
    private void showRecipeDetails(Recipe recipe) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(recipe.getName());
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(550);
        content.setStyle("-fx-background-color: white;");

        // Header with emoji
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane icon = createEmojiIcon(recipe.getName());

        VBox titleBox = new VBox(5);
        Label nameLabel = new Label(recipe.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web("#2c3e50"));

        Label countLabel = new Label(recipe.getIngredientCount() + " ingredients");
        countLabel.setFont(Font.font(14));
        countLabel.setTextFill(Color.web("#7f8c8d"));

        titleBox.getChildren().addAll(nameLabel, countLabel);
        header.getChildren().addAll(icon, titleBox);

        // Match info (for suggestions mode)
        if (currentMode == ViewMode.SUGGESTIONS && recipe.getMatchPercentage() > 0) {
            HBox matchBadge = createMatchBadge(recipe.getMatchPercentage());
            content.getChildren().add(matchBadge);
        }

        // Ingredients section
        VBox ingredientsSection = new VBox(10);
        Label ingredientsTitle = new Label("🥗 Ingredients");
        ingredientsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        ingredientsTitle.setTextFill(Color.web("#2c3e50"));

        VBox ingredientsList = new VBox(8);
        ingredientsList.setPadding(new Insets(10, 0, 0, 10));

        List<String> availableProducts = productDAO.getAllProductNames();
        for (String ingredient : recipe.getIngredientsAsList()) {
            HBox ingredientRow = new HBox(10);
            ingredientRow.setAlignment(Pos.CENTER_LEFT);

            boolean available = isIngredientAvailable(ingredient, availableProducts);
            String checkmark = available ? "✅" : "⬜";
            Label checkLabel = new Label(checkmark);

            Label ingredientLabel = new Label(ingredient);
            ingredientLabel.setFont(Font.font(14));
            if (!available && currentMode == ViewMode.SUGGESTIONS) {
                ingredientLabel.setStyle("-fx-text-fill: #e74c3c;");
            }

            ingredientRow.getChildren().addAll(checkLabel, ingredientLabel);
            ingredientsList.getChildren().add(ingredientRow);
        }

        ingredientsSection.getChildren().addAll(ingredientsTitle, ingredientsList);

        // Instructions section
        VBox instructionsSection = new VBox(10);
        Label instructionsTitle = new Label("📝 Instructions");
        instructionsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        instructionsTitle.setTextFill(Color.web("#2c3e50"));

        TextArea instructionsArea = new TextArea(recipe.getInstructions());
        instructionsArea.setEditable(false);
        instructionsArea.setWrapText(true);
        instructionsArea.setPrefRowCount(8);
        instructionsArea.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        instructionsSection.getChildren().addAll(instructionsTitle, instructionsArea);

        content.getChildren().addAll(
                header,
                new Separator(),
                ingredientsSection,
                new Separator(),
                instructionsSection
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    @FXML
    private void handleAddRecipe() {
        String validationError = ValidationUtils.validateRecipeInput(
                txtRecipeName.getText(), txtIngredients.getText()
        );

        if (validationError != null) {
            AlertUtils.showWarning("Validation Error",
                    "Please correct the following:", validationError);
            return;
        }

        Recipe recipe = new Recipe(
                txtRecipeName.getText().trim(),
                txtIngredients.getText().trim(),
                txtInstructions.getText().trim()
        );

        int id = recipeDAO.addRecipe(recipe);

        if (id > 0) {
            recipeList.add(recipe);
            displayRecipeCards(recipeList);
            clearRecipeForm();
            AlertUtils.showInfo("Success", null,
                    "Recipe '" + recipe.getName() + "' added successfully!");
        } else {
            AlertUtils.showError("Error", "Failed to Add Recipe",
                    "An error occurred while saving the recipe.");
        }
    }

    private void handleEditRecipe(Recipe recipe) {
        Dialog<Recipe> dialog = new Dialog<>();
        dialog.setTitle("Edit Recipe");
        dialog.setHeaderText("Update recipe details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        TextField nameField = new TextField(recipe.getName());
        nameField.setPrefWidth(350);

        TextField ingredientsField = new TextField(recipe.getIngredientsList());
        ingredientsField.setPrefWidth(350);

        TextArea instructionsArea = new TextArea(recipe.getInstructions());
        instructionsArea.setPrefRowCount(6);
        instructionsArea.setWrapText(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ingredients:"), 0, 1);
        grid.add(ingredientsField, 1, 1);
        grid.add(new Label("Instructions:"), 0, 2);
        grid.add(instructionsArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String validationError = ValidationUtils.validateRecipeInput(
                        nameField.getText(), ingredientsField.getText()
                );

                if (validationError != null) {
                    AlertUtils.showWarning("Validation Error",
                            "Please correct the following:", validationError);
                    return null;
                }

                recipe.setName(nameField.getText().trim());
                recipe.setIngredientsList(ingredientsField.getText().trim());
                recipe.setInstructions(instructionsArea.getText().trim());
                return recipe;
            }
            return null;
        });

        Optional<Recipe> result = dialog.showAndWait();
        result.ifPresent(updatedRecipe -> {
            if (recipeDAO.updateRecipe(updatedRecipe)) {
                displayRecipeCards(recipeList);
                AlertUtils.showInfo("Success", null, "Recipe updated successfully!");
            } else {
                AlertUtils.showError("Error", "Failed to Update",
                        "An error occurred while updating the recipe.");
            }
        });
    }

    private void handleDeleteRecipe(Recipe recipe) {
        boolean confirmed = AlertUtils.showConfirmation(
                "Confirm Delete",
                "Delete recipe '" + recipe.getName() + "'?",
                "This action cannot be undone."
        );

        if (confirmed) {
            if (recipeDAO.deleteRecipe(recipe.getId())) {
                recipeList.remove(recipe);
                displayRecipeCards(recipeList);
                AlertUtils.showInfo("Deleted", null,
                        "Recipe '" + recipe.getName() + "' has been deleted.");
            } else {
                AlertUtils.showError("Error", "Failed to Delete",
                        "An error occurred while deleting the recipe.");
            }
        }
    }

    @FXML
    private void clearRecipeForm() {
        txtRecipeName.clear();
        txtIngredients.clear();
        txtInstructions.clear();
    }
}