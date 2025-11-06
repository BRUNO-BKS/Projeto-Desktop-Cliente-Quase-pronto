package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.ProductLogDAO;
import com.buyo.adminfx.model.ProductLogEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
    @FXML private TableColumn<ProductLogEntry, String> colAction;
    @FXML private TableColumn<ProductLogEntry, Integer> colAdminId;
    @FXML private TableColumn<ProductLogEntry, java.time.LocalDateTime> colCreatedAt;

    private final ProductLogDAO dao = new ProductLogDAO();
    private final ObservableList<ProductLogEntry> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colProductId != null) colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        if (colAction != null) colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        if (colAdminId != null) colAdminId.setCellValueFactory(new PropertyValueFactory<>("adminId"));
        if (colCreatedAt != null) colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
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

    private void refresh() {
        Integer pid = parseInt(productIdFilter == null ? null : productIdFilter.getText());
        Integer oid = parseInt(orderIdFilter == null ? null : orderIdFilter.getText());
        LocalDate from = dateFrom != null ? dateFrom.getValue() : null;
        LocalDate to = dateTo != null ? dateTo.getValue() : null;
        List<ProductLogEntry> list = dao.list(pid, oid, from, to);
        rows.setAll(list);
    }

    private Integer parseInt(String s) {
        try { if (s != null && !s.isBlank()) return Integer.parseInt(s.trim()); } catch (Exception ignore) {}
        return null;
    }

    @Override
    public void applySearch(String query) { }
}
