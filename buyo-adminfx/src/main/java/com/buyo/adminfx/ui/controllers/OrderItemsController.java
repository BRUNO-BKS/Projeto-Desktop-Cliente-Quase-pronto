package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.OrderDAO;
import com.buyo.adminfx.dao.ProductDAO;
import com.buyo.adminfx.model.OrderItem;
import com.buyo.adminfx.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.TableCell;

public class OrderItemsController {
    @FXML private Label orderLabel;
    @FXML private TableView<Row> itemsTable;
    @FXML private TableColumn<Row, Integer> colId;
    @FXML private TableColumn<Row, String> colProduct;
    @FXML private TableColumn<Row, Integer> colStock;
    @FXML private TableColumn<Row, Integer> colQuantity;
    @FXML private TableColumn<Row, BigDecimal> colUnitPrice;
    @FXML private TableColumn<Row, BigDecimal> colSubtotal;
    @FXML private Label totalLabel;

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Row> rows = FXCollections.observableArrayList();
    private final Map<Integer, String> productNames = new LinkedHashMap<>();
    private final Map<Integer, Integer> productStock = new LinkedHashMap<>();
    private int orderId;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        if (orderLabel != null) orderLabel.setText("Pedido #" + orderId);
        loadProductsIndex();
        refreshItems();
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colProduct != null) colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        if (colStock != null) colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colQuantity != null) colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (colUnitPrice != null) colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        if (colSubtotal != null) colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        if (itemsTable != null) itemsTable.setItems(rows);

        // Habilita edição inline
        if (itemsTable != null) itemsTable.setEditable(true);
        if (colQuantity != null) {
            colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            colQuantity.setOnEditCommit(evt -> {
                Row row = evt.getRowValue();
                int newQty;
                try { newQty = Math.max(1, evt.getNewValue()); } catch (Exception ex) { info("Editar", "Quantidade inválida"); return; }
                // valida estoque
                int stk = productStock.getOrDefault(row.getProductId(), 0);
                if (newQty > stk) { info("Editar", "Quantidade excede o estoque disponível (" + stk + ")."); return; }
                boolean ok = orderDAO.updateItem(row.getId(), newQty, row.getUnitPrice() == null ? BigDecimal.ZERO : row.getUnitPrice());
                if (!ok) { error("Editar", "Falha ao atualizar item."); return; }
                orderDAO.recalcTotal(orderId);
                refreshItems();
            });
        }
        if (colUnitPrice != null) {
            colUnitPrice.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<BigDecimal>() {
                @Override public String toString(BigDecimal obj) { return obj == null ? "" : obj.toPlainString(); }
                @Override public BigDecimal fromString(String s) {
                    if (s == null) return null; String t = s.trim().replace(',', '.');
                    return t.isEmpty() ? null : new BigDecimal(t);
                }
            }));
            colUnitPrice.setOnEditCommit(evt -> {
                Row row = evt.getRowValue();
                BigDecimal newPrice = evt.getNewValue();
                if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) { info("Editar", "Preço inválido"); return; }
                boolean ok = orderDAO.updateItem(row.getId(), row.getQuantity(), newPrice);
                if (!ok) { error("Editar", "Falha ao atualizar item."); return; }
                orderDAO.recalcTotal(orderId);
                refreshItems();
            });
        }

        // Formatação monetária apenas no subtotal (mantém preço editável)
        if (colSubtotal != null) {
            colSubtotal.setCellFactory(column -> new TableCell<Row, BigDecimal>() {
                @Override protected void updateItem(BigDecimal value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty) { setText(null); }
                    else { setText(value == null ? "" : "R$ " + value.setScale(2, java.math.RoundingMode.HALF_UP)); }
                }
            });
        }
    }

    @FXML
    public void onAdd(ActionEvent e) {
        List<Product> produtos = productDAO.listAll();
        if (produtos == null || produtos.isEmpty()) { info("Itens", "Nenhum produto disponível."); return; }
        Map<String, Integer> index = new LinkedHashMap<>();
        Map<Integer, Product> byId = new LinkedHashMap<>();
        for (Product p : produtos) {
            String label = p.getName() + " (ID " + p.getId() + ")";
            index.put(label, p.getId());
            byId.put(p.getId(), p);
        }
        ChoiceDialog<String> prodDlg = new ChoiceDialog<>(index.keySet().iterator().next(), index.keySet());
        prodDlg.setTitle("Adicionar item");
        prodDlg.setHeaderText("Selecione o produto");
        prodDlg.setContentText("Produto:");
        var prodChoice = prodDlg.showAndWait();
        if (prodChoice.isEmpty()) return;
        Integer prodId = index.get(prodChoice.get());

        TextInputDialog qtdDlg = new TextInputDialog("1");
        qtdDlg.setTitle("Quantidade");
        qtdDlg.setHeaderText(null);
        qtdDlg.setContentText("Informe a quantidade:");
        var qtdRes = qtdDlg.showAndWait();
        if (qtdRes.isEmpty()) return;
        int qtd;
        try { qtd = Math.max(1, Integer.parseInt(qtdRes.get().trim())); } catch (Exception ex) { error("Itens", "Quantidade inválida."); return; }
        // valida estoque
        int stk = productStock.getOrDefault(prodId, 0);
        if (qtd > stk) { error("Itens", "Quantidade excede o estoque disponível (" + stk + ")."); return; }

        BigDecimal defaultPrice = byId.get(prodId) != null && byId.get(prodId).getPrice() != null
                ? byId.get(prodId).getPrice() : null;
        TextInputDialog precoDlg = new TextInputDialog(defaultPrice != null ? defaultPrice.toPlainString() : "");
        precoDlg.setTitle("Preço unitário");
        precoDlg.setHeaderText(null);
        precoDlg.setContentText("Informe o preço unitário (ex: 59.90):");
        var precoRes = precoDlg.showAndWait();
        if (precoRes.isEmpty()) return;
        BigDecimal pu;
        try { pu = new BigDecimal(precoRes.get().trim().replace(',', '.')); } catch (Exception ex) { error("Itens", "Preço inválido."); return; }
        if (pu.compareTo(BigDecimal.ZERO) < 0) { error("Itens", "Preço não pode ser negativo."); return; }

        boolean ok = orderDAO.addItem(orderId, prodId, qtd, pu);
        if (!ok) { error("Itens", "Falha ao adicionar item."); return; }
        orderDAO.recalcTotal(orderId);
        refreshItems();
    }

    @FXML
    public void onRemove(ActionEvent e) {
        Row sel = itemsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Remover Item", "Selecione um item na tabela."); return; }
        boolean ok = orderDAO.removeItem(sel.getId());
        if (!ok) { error("Remover Item", "Falha ao remover item."); return; }
        orderDAO.recalcTotal(orderId);
        refreshItems();
    }

    @FXML
    public void onEdit(ActionEvent e) {
        Row sel = itemsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Editar Item", "Selecione um item na tabela."); return; }
        TextInputDialog qtdDlg = new TextInputDialog(String.valueOf(sel.getQuantity()));
        qtdDlg.setTitle("Editar Item");
        qtdDlg.setHeaderText(null);
        qtdDlg.setContentText("Quantidade:");
        var qtdRes = qtdDlg.showAndWait();
        if (qtdRes.isEmpty()) return;
        int qtd;
        try { qtd = Math.max(1, Integer.parseInt(qtdRes.get().trim())); } catch (Exception ex) { error("Editar Item", "Quantidade inválida."); return; }

        TextInputDialog precoDlg = new TextInputDialog(sel.getUnitPrice() != null ? sel.getUnitPrice().toPlainString() : "");
        precoDlg.setTitle("Editar Item");
        precoDlg.setHeaderText(null);
        precoDlg.setContentText("Preço unitário:");
        var precoRes = precoDlg.showAndWait();
        if (precoRes.isEmpty()) return;
        BigDecimal pu;
        try { pu = new BigDecimal(precoRes.get().trim().replace(',', '.')); } catch (Exception ex) { error("Editar Item", "Preço inválido."); return; }

        boolean ok = orderDAO.updateItem(sel.getId(), qtd, pu);
        if (!ok) { error("Editar Item", "Falha ao atualizar item."); return; }
        orderDAO.recalcTotal(orderId);
        refreshItems();
    }

    @FXML
    public void onExportCsv(ActionEvent e) {
        if (rows.isEmpty()) { info("Exportar", "Sem itens para exportar."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar itens do pedido como CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = fc.showSaveDialog(itemsTable.getScene().getWindow());
        if (file == null) return;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("id,produto,quantidade,preco_unitario,subtotal\n");
            for (Row r : rows) {
                sb.append(r.getId()).append(',')
                  .append('"').append(safeCsv(r.getProduct())).append('"').append(',')
                  .append(r.getQuantity()).append(',')
                  .append(r.getUnitPrice() == null ? "" : r.getUnitPrice().toPlainString()).append(',')
                  .append(r.getSubtotal() == null ? "" : r.getSubtotal().toPlainString()).append('\n');
            }
            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
            info("Exportar", "Arquivo salvo: " + file.getAbsolutePath());
        } catch (Exception ex) {
            error("Exportar", "Falha ao salvar CSV: " + ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent e) {
        Stage st = (Stage) itemsTable.getScene().getWindow();
        st.close();
    }

    private void refreshItems() {
        rows.clear();
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> list = orderDAO.listItems(orderId);
        for (OrderItem it : list) {
            String name = productNames.getOrDefault(it.getProductId(), String.valueOf(it.getProductId()));
            int stk = productStock.getOrDefault(it.getProductId(), 0);
            BigDecimal sub = it.getUnitPrice() != null ? it.getUnitPrice().multiply(new BigDecimal(it.getQuantity())) : BigDecimal.ZERO;
            rows.add(new Row(it.getId(), it.getProductId(), name, stk, it.getQuantity(), it.getUnitPrice(), sub));
            total = total.add(sub);
        }
        if (totalLabel != null) totalLabel.setText("R$ " + total.toPlainString());
    }

    private void loadProductsIndex() {
        productNames.clear();
        productStock.clear();
        List<Product> produtos = productDAO.listAll();
        for (Product p : produtos) {
            productNames.put(p.getId(), p.getName());
            productStock.put(p.getId(), p.getStock());
        }
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    private String safeCsv(String s) { return s == null ? "" : s.replace("\"", "' "); }

    public static class Row {
        private final int id;
        private final int productId;
        private final String product;
        private final int stock;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;
        public Row(int id, int productId, String product, int stock, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.id = id; this.productId = productId; this.product = product; this.stock = stock; this.quantity = quantity; this.unitPrice = unitPrice; this.subtotal = subtotal;
        }
        public int getId() { return id; }
        public int getProductId() { return productId; }
        public String getProduct() { return product; }
        public int getStock() { return stock; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}
