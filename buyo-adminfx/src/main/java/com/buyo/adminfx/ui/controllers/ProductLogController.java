package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.ProductLogDAO;
import com.buyo.adminfx.model.ProductLogEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;

public class ProductLogController implements SearchableController {
    @FXML private TextField productIdFilter;
    @FXML private TextField orderIdFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    @FXML private TableView<ProductLogEntry> table;
    @FXML private TableColumn<ProductLogEntry, Integer> colId;
    @FXML private TableColumn<ProductLogEntry, Integer> colProductId;
    @FXML private TableColumn<ProductLogEntry, String> colFieldChanged;
    @FXML private TableColumn<ProductLogEntry, String> colOldValue;
    @FXML private TableColumn<ProductLogEntry, String> colNewValue;
    @FXML private TableColumn<ProductLogEntry, java.time.LocalDateTime> colChangedAt;

    private final ProductLogDAO dao = new ProductLogDAO();
    private final ObservableList<ProductLogEntry> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colProductId != null) colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        if (colFieldChanged != null) colFieldChanged.setCellValueFactory(new PropertyValueFactory<>("fieldChanged"));
        if (colOldValue != null) colOldValue.setCellValueFactory(new PropertyValueFactory<>("oldValue"));
        if (colNewValue != null) colNewValue.setCellValueFactory(new PropertyValueFactory<>("newValue"));
        if (colChangedAt != null) colChangedAt.setCellValueFactory(new PropertyValueFactory<>("changedAt"));
        if (table != null) table.setItems(rows);
        refresh();
    }

    @FXML public void onApplyFilters(ActionEvent e) { refresh(); }
    @FXML public void onClearFilters(ActionEvent e) {
        if (productIdFilter != null) productIdFilter.clear();
        if (orderIdFilter != null) orderIdFilter.clear();
        if (dateFrom != null) dateFrom.setValue(null);
        if (dateTo != null) dateTo.setValue(null);
        refresh();
    }
    @FXML public void onRefresh(ActionEvent e) { refresh(); }

    @FXML public void onEdit(ActionEvent e) {
        ProductLogEntry sel = table == null ? null : table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecione um registro para editar.").show();
            return;
        }
        Dialog<ProductLogEntry> dlg = new Dialog<>();
        dlg.setTitle("Editar alteração de produto");
        ButtonType saveBtn = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        TextField tfField = new TextField(sel.getFieldChanged());
        TextField tfOld = new TextField(sel.getOldValue());
        TextField tfNew = new TextField(sel.getNewValue());
        TextField tfProductId = new TextField(String.valueOf(sel.getProductId()));
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("Campo alterado:"), tfField);
        gp.addRow(1, new Label("Valor antigo:"), tfOld);
        gp.addRow(2, new Label("Valor novo:"), tfNew);
        gp.addRow(3, new Label("Produto ID:"), tfProductId);
        dlg.getDialogPane().setContent(gp);
        dlg.setResultConverter(bt -> {
            if (bt == saveBtn) {
                int pid = sel.getProductId();
                try { pid = Integer.parseInt(tfProductId.getText().trim()); } catch (Exception ignore) {}
                // Mantém a data original
                return new ProductLogEntry(sel.getId(), pid, tfField.getText(), tfOld.getText(), tfNew.getText(), sel.getChangedAt());
            }
            return null;
        });
        var res = dlg.showAndWait();
        if (res.isPresent()) {
            if (dao.update(res.get())) {
                refresh();
            } else {
                new Alert(Alert.AlertType.ERROR, "Falha ao salvar alterações.").show();
            }
        }
    }

    @FXML public void onDelete(ActionEvent e) {
        ProductLogEntry sel = table == null ? null : table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecione um registro para remover.").show();
            return;
        }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Remover registro #" + sel.getId() + "?", ButtonType.YES, ButtonType.NO);
        var ans = c.showAndWait();
        if (ans.isPresent() && ans.get() == ButtonType.YES) {
            if (dao.deleteById(sel.getId())) {
                refresh();
            } else {
                new Alert(Alert.AlertType.ERROR, "Falha ao remover registro.").show();
            }
        }
    }

    private void refresh() {
        Integer pid = parseInt(productIdFilter == null ? null : productIdFilter.getText());
        LocalDate from = dateFrom != null ? dateFrom.getValue() : null;
        LocalDate to = dateTo != null ? dateTo.getValue() : null;
        List<ProductLogEntry> list = dao.list(pid, from, to);
        rows.setAll(list);
    }

    private Integer parseInt(String s) {
        try { if (s != null && !s.isBlank()) return Integer.parseInt(s.trim()); } catch (Exception ignore) {}
        return null;
    }

    @Override
    public void applySearch(String query) { }
}

