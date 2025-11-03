package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.AddressDAO;
import com.buyo.adminfx.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AddressesController implements SearchableController {
    @FXML private TextField userIdFilter;

    @FXML private TableView<Address> table;
    @FXML private TableColumn<Address, Integer> colId;
    @FXML private TableColumn<Address, Integer> colUserId;
    @FXML private TableColumn<Address, String> colStreet;
    @FXML private TableColumn<Address, String> colNumber;
    @FXML private TableColumn<Address, String> colDistrict;
    @FXML private TableColumn<Address, String> colCity;
    @FXML private TableColumn<Address, String> colState;
    @FXML private TableColumn<Address, String> colZip;
    @FXML private TableColumn<Address, String> colComplement;
    @FXML private TableColumn<Address, String> colType;

    private final AddressDAO dao = new AddressDAO();
    private final ObservableList<Address> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (colStreet != null) colStreet.setCellValueFactory(new PropertyValueFactory<>("street"));
        if (colNumber != null) colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (colDistrict != null) colDistrict.setCellValueFactory(new PropertyValueFactory<>("district"));
        if (colCity != null) colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        if (colState != null) colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        if (colZip != null) colZip.setCellValueFactory(new PropertyValueFactory<>("zip"));
        if (colComplement != null) colComplement.setCellValueFactory(new PropertyValueFactory<>("complement"));
        if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (table != null) table.setItems(rows);
        refresh();
    }

    @FXML public void onApplyFilters(ActionEvent e) { refresh(); }
    @FXML public void onClearFilters(ActionEvent e) {
        if (userIdFilter != null) userIdFilter.clear();
        refresh();
    }
    @FXML public void onRefresh(ActionEvent e) { refresh(); }

    private void refresh() {
        Integer uid = null;
        try {
            if (userIdFilter != null && userIdFilter.getText() != null && !userIdFilter.getText().isBlank()) {
                uid = Integer.parseInt(userIdFilter.getText().trim());
            }
        } catch (Exception ignore) {}
        List<Address> list = dao.list(uid);
        rows.setAll(list);
    }

    @Override
    public void applySearch(String query) { }
}
