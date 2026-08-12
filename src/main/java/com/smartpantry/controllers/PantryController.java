// src/main/java/com/smartpantry/controllers/PantryController.java
package com.smartpantry.controllers;

import com.smartpantry.dao.ProductDAO;
import com.smartpantry.models.Product;
import com.smartpantry.utils.AlertUtils;
import com.smartpantry.utils.ValidationUtils;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Pantry view.
 * Handles CRUD operations for products and expiration alerts.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class PantryController implements Initializable {

    /** Table displaying products */
    @FXML private TableView<Product> productTable;

    /** Table columns */
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colQuantity;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, LocalDate> colExpiration;
    @FXML private TableColumn<Product, Void> colActions;

    /** Search field */
    @FXML private TextField txtSearch;

    /** Statistics labels */
    @FXML private Label lblTotalItems;
    @FXML private Label lblExpiringItems;
    @FXML private Label lblExpiredItems;

    /** Add product form fields */
    @FXML private TextField txtName;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<String> cmbUnit;
    @FXML private DatePicker dpExpiration;

    /** Product DAO instance */
    private ProductDAO productDAO;

    /** Observable list of products */
    private ObservableList<Product> productList;

    /** Filtered list for search functionality */
    private FilteredList<Product> filteredProducts;

    /**
     * Initializes the controller.
     *
     * @param url the location used to resolve relative paths
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productDAO = ProductDAO.getInstance();

        setupTableColumns();
        setupUnitComboBox();
        setupSearchField();
        setupExpirationDatePicker();
        loadProducts();
        updateStatistics();
    }

    /**
     * Configures the table columns with cell factories.
     */
    private void setupTableColumns() {
        // ID Column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Name Column
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Quantity Column
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    // Format to remove unnecessary decimal places
                    setText(quantity % 1 == 0 ?
                            String.valueOf(quantity.intValue()) :
                            String.format("%.2f", quantity));
                }
            }
        });
        colQuantity.setStyle("-fx-alignment: CENTER;");

        // Unit Column
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setStyle("-fx-alignment: CENTER;");

        // Expiration Date Column with color coding
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        colExpiration.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });
        colExpiration.setStyle("-fx-alignment: CENTER;");

        // Actions Column
        setupActionsColumn();

        // Row factory for expiration highlighting
        productTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                getStyleClass().removeAll("expired-row", "expiring-soon-row");

                if (product == null || empty) {
                    setStyle("");
                } else if (product.isExpired()) {
                    getStyleClass().add("expired-row");
                } else if (product.isExpiringSoon()) {
                    getStyleClass().add("expiring-soon-row");
                }
            }
        });
    }

    /**
     * Sets up the actions column with Edit and Delete buttons.
     */
    private void setupActionsColumn() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Product, Void> call(TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox container = new HBox(5, btnEdit, btnDelete);

                    {
                        btnEdit.getStyleClass().addAll("btn", "btn-secondary", "btn-small");
                        btnDelete.getStyleClass().addAll("btn", "btn-danger", "btn-small");

                        btnEdit.setOnAction(event -> {
                            Product product = getTableView().getItems().get(getIndex());
                            handleEditProduct(product);
                        });

                        btnDelete.setOnAction(event -> {
                            Product product = getTableView().getItems().get(getIndex());
                            handleDeleteProduct(product);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : container);
                    }
                };
            }
        });
    }

    /**
     * Sets up the unit combo box with common units.
     */
    private void setupUnitComboBox() {
        cmbUnit.getItems().addAll(
                "pieces", "kg", "grams", "liters", "ml",
                "cups", "tablespoons", "teaspoons", "loaf", "heads", "cans"
        );
        cmbUnit.setValue("pieces");
    }

    /**
     * Sets up the search field with filtering.
     */
    private void setupSearchField() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredProducts != null) {
                filteredProducts.setPredicate(product -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return product.getName().toLowerCase().contains(lowerCaseFilter) ||
                            product.getUnit().toLowerCase().contains(lowerCaseFilter);
                });
                updateStatistics();
            }
        });
    }

    /**
     * Sets up the expiration date picker.
     */
    private void setupExpirationDatePicker() {
        dpExpiration.setValue(LocalDate.now().plusDays(7));

        // Disable past dates
        dpExpiration.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    /**
     * Loads all products from the database.
     */
    private void loadProducts() {
        productList = productDAO.getAllProducts();
        filteredProducts = new FilteredList<>(productList, p -> true);
        productTable.setItems(filteredProducts);
    }

    /**
     * Refreshes the product list from the database.
     */
    @FXML
    private void handleRefresh() {
        loadProducts();
        updateStatistics();
        txtSearch.clear();
        AlertUtils.showInfo("Refreshed", null, "Product list has been refreshed.");
    }

    /**
     * Handles adding a new product.
     */
    @FXML
    private void handleAddProduct() {
        // Validate input
        String validationError = ValidationUtils.validateProductInput(
                txtName.getText(), txtQuantity.getText(), cmbUnit.getValue()
        );

        if (validationError != null) {
            AlertUtils.showWarning("Validation Error", "Please correct the following:",
                    validationError);
            return;
        }

        // Create and save the product
        Product product = new Product(
                txtName.getText().trim(),
                Double.parseDouble(txtQuantity.getText().trim()),
                cmbUnit.getValue(),
                dpExpiration.getValue()
        );

        int id = productDAO.addProduct(product);

        if (id > 0) {
            productList.add(product);
            clearForm();
            updateStatistics();
            AlertUtils.showInfo("Success", null,
                    "Product '" + product.getName() + "' added successfully!");
        } else {
            AlertUtils.showError("Error", "Failed to Add Product",
                    "An error occurred while saving the product.");
        }
    }

    /**
     * Opens the edit dialog for a product.
     *
     * @param product the product to edit
     */
    private void handleEditProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Update product details");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(product.getName());
        TextField quantityField = new TextField(String.valueOf(product.getQuantity()));
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll(cmbUnit.getItems());
        unitCombo.setValue(product.getUnit());
        DatePicker expPicker = new DatePicker(product.getExpirationDate());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Unit:"), 0, 2);
        grid.add(unitCombo, 1, 2);
        grid.add(new Label("Expiration:"), 0, 3);
        grid.add(expPicker, 1, 3);

        GridPane.setHgrow(nameField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/application.css").toExternalForm()
        );

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String validationError = ValidationUtils.validateProductInput(
                        nameField.getText(), quantityField.getText(), unitCombo.getValue()
                );

                if (validationError != null) {
                    AlertUtils.showWarning("Validation Error",
                            "Please correct the following:", validationError);
                    return null;
                }

                product.setName(nameField.getText().trim());
                product.setQuantity(Double.parseDouble(quantityField.getText().trim()));
                product.setUnit(unitCombo.getValue());
                product.setExpirationDate(expPicker.getValue());
                return product;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(updatedProduct -> {
            if (productDAO.updateProduct(updatedProduct)) {
                productTable.refresh();
                updateStatistics();
                AlertUtils.showInfo("Success", null, "Product updated successfully!");
            } else {
                AlertUtils.showError("Error", "Failed to Update",
                        "An error occurred while updating the product.");
            }
        });
    }

    /**
     * Handles deleting a product.
     *
     * @param product the product to delete
     */
    private void handleDeleteProduct(Product product) {
        boolean confirmed = AlertUtils.showConfirmation(
                "Confirm Delete",
                "Delete " + product.getName() + "?",
                "This action cannot be undone."
        );

        if (confirmed) {
            if (productDAO.deleteProduct(product.getId())) {
                productList.remove(product);
                updateStatistics();
                AlertUtils.showInfo("Deleted", null,
                        "Product '" + product.getName() + "' has been deleted.");
            } else {
                AlertUtils.showError("Error", "Failed to Delete",
                        "An error occurred while deleting the product.");
            }
        }
    }

    /**
     * Clears the add product form.
     */
    @FXML
    private void clearForm() {
        txtName.clear();
        txtQuantity.clear();
        cmbUnit.setValue("pieces");
        dpExpiration.setValue(LocalDate.now().plusDays(7));
    }

    /**
     * Updates the statistics labels.
     */
    private void updateStatistics() {
        int total = filteredProducts.size();
        long expiring = filteredProducts.stream()
                .filter(Product::isExpiringSoon)
                .count();
        long expired = filteredProducts.stream()
                .filter(Product::isExpired)
                .count();

        lblTotalItems.setText(String.valueOf(total));
        lblExpiringItems.setText(String.valueOf(expiring));
        lblExpiredItems.setText(String.valueOf(expired));
    }
}