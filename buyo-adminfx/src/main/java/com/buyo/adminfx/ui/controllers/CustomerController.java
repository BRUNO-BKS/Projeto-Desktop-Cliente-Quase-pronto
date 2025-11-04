package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.CustomerDAO;
import com.buyo.adminfx.model.Customer;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomerController implements SearchableController {
    @FXML private TableView<Customer> table;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colCreatedAt;
    @FXML private TableColumn<Customer, String> colLastActive;

    private ObservableList<Customer> masterData;
    private FilteredList<Customer> filtered;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (colCreatedAt != null) {
            colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        }
        if (colLastActive != null) {
            colLastActive.setCellValueFactory(new PropertyValueFactory<>("lastActive"));
        }

        masterData = FXCollections.observableArrayList(new CustomerDAO().listAll());
        filtered = new FilteredList<>(masterData, c -> true);
        table.setItems(filtered);
    }

    @Override
    public void applySearch(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (filtered == null) return;
        if (q.isEmpty()) {
            filtered.setPredicate(c -> true);
            return;
        }
        filtered.setPredicate(c -> {
            if (c == null) return false;
            String id = String.valueOf(c.getId());
            String name = c.getName() == null ? "" : c.getName().toLowerCase();
            String email = c.getEmail() == null ? "" : c.getEmail().toLowerCase();
            String phone = c.getPhone() == null ? "" : c.getPhone().toLowerCase();
            String created = c.getCreatedAt() == null ? "" : c.getCreatedAt().toLowerCase();
            String last = c.getLastActive() == null ? "" : c.getLastActive().toLowerCase();
            return id.contains(q) || name.contains(q) || email.contains(q) || phone.contains(q) || created.contains(q) || last.contains(q);
        });
    }
}
