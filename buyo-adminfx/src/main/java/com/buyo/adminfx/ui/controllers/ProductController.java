package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.ProductDAO;
import com.buyo.adminfx.model.Product;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProductController implements SearchableController {
    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colImage;
    @FXML private ComboBox<String> categoryFilter;

    private ObservableList<Product> masterData;
    private FilteredList<Product> filtered;
    private String currentQuery = "";
    private String currentCategory = null; // null or "Todas"

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colCategory != null) {
            colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        }
        if (colImage != null) {
            colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
            colImage.setCellFactory(col -> new TableCell<>() {
                private final ImageView imageView = new ImageView();
                {
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(60);
                    imageView.setPreserveRatio(true);
                }
                @Override
                protected void updateItem(String url, boolean empty) {
                    super.updateItem(url, empty);
                    if (empty || url == null || url.isBlank()) {
                        setGraphic(null);
                    } else {
                        try {
                            imageView.setImage(new Image(url, true));
                            setGraphic(imageView);
                        } catch (Exception ex) {
                            setGraphic(null);
                        }
                    }
                }
            });
        }

        masterData = FXCollections.observableArrayList(new ProductDAO().listAll());
        filtered = new FilteredList<>(masterData, p -> true);
        table.setItems(filtered);

        // Populate category filter
        if (categoryFilter != null) {
            ObservableList<String> categories = FXCollections.observableArrayList();
            categories.add("Todas");
            masterData.stream()
                .map(Product::getCategoryName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .forEach(categories::add);
            categoryFilter.setItems(categories);
            categoryFilter.getSelectionModel().selectFirst();
            categoryFilter.valueProperty().addListener((obs, old, val) -> {
                currentCategory = (val == null || val.equals("Todas")) ? null : val;
                refilter();
            });
        }
    }

    @Override
    public void applySearch(String query) {
        currentQuery = query == null ? "" : query.trim().toLowerCase();
        refilter();
    }

    private void refilter() {
        if (filtered == null) return;
        final String q = currentQuery;
        final String cat = currentCategory;
        filtered.setPredicate(p -> {
            if (p == null) return false;
            // Category filter
            if (cat != null) {
                String pn = p.getCategoryName();
                if (pn == null || !pn.equalsIgnoreCase(cat)) return false;
            }
            // Text query filter
            if (q == null || q.isBlank()) return true;
            String id = String.valueOf(p.getId());
            String name = p.getName() == null ? "" : p.getName().toLowerCase();
            String price = p.getPrice() == null ? "" : p.getPrice().toPlainString().toLowerCase();
            String stock = String.valueOf(p.getStock());
            String category = p.getCategoryName() == null ? "" : p.getCategoryName().toLowerCase();
            return id.contains(q) || name.contains(q) || price.contains(q) || stock.contains(q) || category.contains(q);
        });
    }

    private void refreshData() {
        var dao = new ProductDAO();
        masterData.setAll(dao.listAll());
        // reconstroi filtro de categorias
        if (categoryFilter != null) {
            ObservableList<String> categories = FXCollections.observableArrayList();
            categories.add("Todas");
            masterData.stream()
                .map(Product::getCategoryName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .forEach(categories::add);
            categoryFilter.setItems(categories);
            categoryFilter.getSelectionModel().selectFirst();
        }
        refilter();
    }

    @FXML
    public void onAdd(ActionEvent e) {
        openForm(null);
    }

    @FXML
    public void onEdit(ActionEvent e) {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setTitle("Editar produto");
            a.setContentText("Selecione um produto na lista.");
            a.show();
            return;
        }
        openForm(sel);
    }

    @FXML
    public void onDelete(ActionEvent e) {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setTitle("Remover produto");
            a.setContentText("Selecione um produto na lista.");
            a.show();
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setHeaderText(null);
        conf.setTitle("Remover produto");
        conf.setContentText("Tem certeza que deseja remover '" + sel.getName() + "'? ");
        conf.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                boolean ok = new ProductDAO().deleteProduct(sel.getId());
                if (!ok) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setHeaderText(null);
                    err.setTitle("Remover produto");
                    err.setContentText("Falha ao remover.");
                    err.show();
                } else {
                    refreshData();
                }
            }
        });
    }

    private void openForm(Product product) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/ProductForm.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ProductForm.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "ProductForm.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            ProductFormController ctrl = loader.getController();
            if (product != null) ctrl.setProduct(product);
            Stage dlg = new Stage();
            dlg.setTitle(product == null ? "Adicionar Produto" : "Editar Produto");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setScene(new Scene(root, 520, 380));
            dlg.showAndWait();
            if (ctrl.isSaved()) {
                refreshData();
            }
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Produto");
            a.setContentText("Falha ao abrir formulário: " + ex.getMessage());
            a.show();
        }
    }
}

