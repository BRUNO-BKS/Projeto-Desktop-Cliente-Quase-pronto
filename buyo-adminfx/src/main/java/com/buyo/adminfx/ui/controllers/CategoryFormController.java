package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.CategoryDAO;
import com.buyo.adminfx.model.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CategoryFormController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Label statusLabel;

    private Integer editingId = null;
    private boolean saved = false;

    public boolean isSaved() { return saved; }

    public void setCategory(Category c) {
        if (c == null) return;
        editingId = c.getId();
        nameField.setText(c.getName());
        descriptionField.setText(c.getDescription());
    }

    @FXML
    public void onSave(ActionEvent e) {
        String name = trim(nameField.getText());
        String desc = trim(descriptionField.getText());
        if (name.isBlank()) {
            setStatus("Nome é obrigatório.");
            return;
        }
        var dao = new CategoryDAO();
        boolean ok;
        if (editingId == null) {
            ok = dao.createCategory(name, desc) != null;
        } else {
            ok = dao.updateCategory(editingId, name, desc);
        }
        if (!ok) { setStatus("Falha ao salvar categoria."); return; }
        saved = true;
        close();
    }

    @FXML
    public void onCancel(ActionEvent e) { close(); }

    private void close() { ((Stage) nameField.getScene().getWindow()).close(); }
    private String trim(String s) { return s == null ? "" : s.trim(); }
    private void setStatus(String s) { if (statusLabel != null) statusLabel.setText(s); }
}
