package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.CategoryDAO;
import com.buyo.adminfx.model.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CategoryController implements SearchableController {
    @FXML private TableView<Category> table;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;

    private ObservableList<Category> masterData;
    private FilteredList<Category> filtered;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        masterData = FXCollections.observableArrayList(new CategoryDAO().listAll());
        filtered = new FilteredList<>(masterData, c -> true);
        table.setItems(filtered);
    }

    @Override
    public void applySearch(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        if (filtered == null) return;
        if (q.isEmpty()) { filtered.setPredicate(c -> true); return; }
        filtered.setPredicate(c -> {
            if (c == null) return false;
            String id = String.valueOf(c.getId());
            String name = c.getName() == null ? "" : c.getName().toLowerCase();
            String desc = c.getDescription() == null ? "" : c.getDescription().toLowerCase();
            return id.contains(q) || name.contains(q) || desc.contains(q);
        });
    }

    private void refreshData() {
        masterData.setAll(new CategoryDAO().listAll());
    }

    @FXML
    public void onAdd(ActionEvent e) {
        openForm(null);
    }

    @FXML
    public void onEdit(ActionEvent e) {
        Category sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setTitle("Editar categoria");
            a.setContentText("Selecione uma categoria na lista.");
            a.show();
            return;
        }
        openForm(sel);
    }

    @FXML
    public void onDelete(ActionEvent e) {
        Category sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setTitle("Remover categoria");
            a.setContentText("Selecione uma categoria na lista.");
            a.show();
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setHeaderText(null);
        conf.setTitle("Remover categoria");
        conf.setContentText("Tem certeza que deseja remover '" + sel.getName() + "'? Produtos vinculados ficarão sem categoria.");
        conf.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                boolean ok = new CategoryDAO().deleteCategory(sel.getId());
                if (!ok) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setHeaderText(null);
                    err.setTitle("Remover categoria");
                    err.setContentText("Falha ao remover.");
                    err.show();
                } else {
                    refreshData();
                }
            }
        });
    }

    private void openForm(Category category) {
        try {
            URL fxml = getClass().getResource("/com/buyo/adminfx/ui/CategoryForm.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                Path p1 = Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "CategoryForm.fxml");
                Path p2 = Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "CategoryForm.fxml");
                Path existing = Files.exists(p1) ? p1 : (Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            CategoryFormController ctrl = loader.getController();
            if (category != null) ctrl.setCategory(category);
            Stage dlg = new Stage();
            dlg.setTitle(category == null ? "Adicionar Categoria" : "Editar Categoria");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setScene(new Scene(root, 480, 260));
            dlg.showAndWait();
            if (ctrl.isSaved()) refreshData();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Categoria");
            a.setContentText("Falha ao abrir formulário: " + ex.getMessage());
            a.show();
        }
    }
}

