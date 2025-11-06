package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.ReviewDAO;
import com.buyo.adminfx.model.Review;
import com.buyo.adminfx.model.Category;
import com.buyo.adminfx.dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReviewsController implements SearchableController {
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private TextField productIdFilter;
    @FXML private TextField userIdFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    @FXML private TableView<Review> table;
    @FXML private TableColumn<Review, Integer> colId;
    @FXML private TableColumn<Review, Integer> colProductId;
    @FXML private TableColumn<Review, Integer> colUserId;
    @FXML private TableColumn<Review, Integer> colRating;
    @FXML private TableColumn<Review, String> colComment;
    @FXML private TableColumn<Review, java.time.LocalDateTime> colCreatedAt;

    private final ReviewDAO dao = new ReviewDAO();
    private final ObservableList<Review> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        if (categoryFilter != null) {
            try {
                List<Category> cats = new CategoryDAO().listAll();
                categoryFilter.getItems().setAll(cats);
                categoryFilter.setConverter(new StringConverter<>() {
                    @Override public String toString(Category c) { return c == null ? "" : c.getName(); }
                    @Override public Category fromString(String s) { return null; }
                });
                categoryFilter.setPromptText("Todas");
                categoryFilter.getSelectionModel().clearSelection();
            } catch (Exception ignore) {}
        }

        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colProductId != null) colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (colRating != null) colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        if (colComment != null) colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
        if (colCreatedAt != null) {
            colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
            // Formata a data/hora para exibir apenas a data de criação
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            colCreatedAt.setCellFactory(column -> new TableCell<Review, LocalDateTime>() {
                @Override protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(fmt));
                    }
                }
            });
        }
        if (table != null) table.setItems(rows);
        refresh();
    }

    @FXML public void onApplyFilters(ActionEvent e) { refresh(); }
    @FXML public void onClearFilters(ActionEvent e) {
        if (categoryFilter != null) categoryFilter.getSelectionModel().clearSelection();
        if (productIdFilter != null) productIdFilter.clear();
        if (userIdFilter != null) userIdFilter.clear();
        if (dateFrom != null) dateFrom.setValue(null);
        if (dateTo != null) dateTo.setValue(null);
        refresh();
    }
    @FXML public void onRefresh(ActionEvent e) { refresh(); }

    private void refresh() {
        Integer pid = parseInt(productIdFilter == null ? null : productIdFilter.getText());
        Integer uid = parseInt(userIdFilter == null ? null : userIdFilter.getText());
        LocalDate from = dateFrom != null ? dateFrom.getValue() : null;
        LocalDate to = dateTo != null ? dateTo.getValue() : null;
        Integer catId = (categoryFilter != null && categoryFilter.getValue() != null) ? categoryFilter.getValue().getId() : null;
        List<Review> list = dao.list(null, pid, uid, from, to, catId);
        rows.setAll(list);
    }

    private Integer parseInt(String s) { try { if (s != null && !s.isBlank()) return Integer.parseInt(s.trim()); } catch (Exception ignore) {} return null; }

    @FXML public void onApprove(ActionEvent e) { updateStatus("APROVADO"); }
    @FXML public void onReject(ActionEvent e) { updateStatus("REJEITADO"); }

    private void updateStatus(String newStatus) {
        Review sel = table != null ? table.getSelectionModel().getSelectedItem() : null;
        if (sel == null) { error("Avaliações", "Selecione uma avaliação."); return; }
        boolean ok = dao.updateStatus(sel.getId(), newStatus);
        if (!ok) { error("Avaliações", "Falha ao atualizar status."); return; }
        info("Avaliações", "Status atualizado para " + newStatus + ".");
        refresh();
    }

    private void info(String title, String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null); a.setTitle(title); a.setContentText(msg); a.show(); }
    private void error(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setTitle(title); a.setContentText(msg); a.show(); }

    @Override public void applySearch(String query) { }
}
