package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.CartDAO;
import com.buyo.adminfx.model.Cart;
import com.buyo.adminfx.model.CartItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CartsController implements SearchableController {
    @FXML private TextField userIdFilter;

    @FXML private TableView<Cart> tableCarts;
    @FXML private TableColumn<Cart, Integer> colCartId;
    @FXML private TableColumn<Cart, Integer> colUserId;
    @FXML private TableColumn<Cart, java.time.LocalDateTime> colCreatedAt;

    @FXML private TableView<CartItem> tableItems;
    @FXML private TableColumn<CartItem, Integer> colItemId;
    @FXML private TableColumn<CartItem, Integer> colProductId;
    @FXML private TableColumn<CartItem, Integer> colQuantity;
    @FXML private TableColumn<CartItem, java.math.BigDecimal> colPrice;

    private final CartDAO dao = new CartDAO();
    private final ObservableList<Cart> carts = FXCollections.observableArrayList();
    private final ObservableList<CartItem> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colCartId != null) colCartId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (colCreatedAt != null) colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        if (tableCarts != null) tableCarts.setItems(carts);

        if (colItemId != null) colItemId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colProductId != null) colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (colPrice != null) colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (tableItems != null) tableItems.setItems(items);

        if (tableCarts != null) {
            tableCarts.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) loadItems(newSel.getId()); else items.clear();
            });
        }
        refresh();
    }

    private void loadItems(int cartId) {
        List<CartItem> list = dao.listItems(cartId);
        items.setAll(list);
    }

    @FXML public void onApplyFilters(ActionEvent e) { refresh(); }
    @FXML public void onClearFilters(ActionEvent e) { if (userIdFilter != null) userIdFilter.clear(); refresh(); }
    @FXML public void onRefresh(ActionEvent e) { refresh(); }

    private void refresh() {
        Integer uid = null;
        try { if (userIdFilter != null && userIdFilter.getText() != null && !userIdFilter.getText().isBlank()) uid = Integer.parseInt(userIdFilter.getText().trim()); } catch (Exception ignore) {}
        List<Cart> list = dao.listCarts(uid);
        carts.setAll(list);
        items.clear();
    }

    @FXML
    public void onConvertToOrder(ActionEvent e) {
        Cart sel = tableCarts != null ? tableCarts.getSelectionModel().getSelectedItem() : null;
        if (sel == null) { error("Carrinhos", "Selecione um carrinho."); return; }
        Integer orderId = dao.convertToOrder(sel.getId());
        if (orderId == null) { error("Carrinhos", "Falha ao converter carrinho em pedido."); return; }
        info("Carrinhos", "Carrinho convertido. Pedido #" + orderId);
        refresh();
    }

    private void info(String title, String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null); a.setTitle(title); a.setContentText(msg); a.show(); }
    private void error(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setTitle(title); a.setContentText(msg); a.show(); }

    @Override public void applySearch(String query) { }
}
