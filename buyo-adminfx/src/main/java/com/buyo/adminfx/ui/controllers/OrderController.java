package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.OrderDAO;
import com.buyo.adminfx.model.Order;
import com.buyo.adminfx.model.OrderItem;
import com.buyo.adminfx.auth.Session;
import com.buyo.adminfx.services.CouponService;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.FileChooser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 

public class OrderController implements SearchableController {
    @FXML private TableView<Order> table;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, Integer> colCustomerId;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, BigDecimal> colTotal;
    @FXML private TableColumn<Order, LocalDateTime> colCreatedAt;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    private ObservableList<Order> masterData;
    private FilteredList<Order> filtered;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        if (colCustomerName != null) colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        masterData = FXCollections.observableArrayList(new OrderDAO().listAll());
        filtered = new FilteredList<>(masterData, o -> true);
        table.setItems(filtered);

        // popula filtros
        if (statusFilter != null) {
            statusFilter.getItems().setAll("", "CRIADO", "PAGO", "ENVIADO", "CONCLUIDO", "CANCELADO");
            statusFilter.getSelectionModel().select(0);
        }
    }

    @Override
    public void applySearch(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (filtered == null) return;
        if (q.isEmpty()) {
            filtered.setPredicate(o -> true);
            return;
        }

        filtered.setPredicate(o -> {
            if (o == null) return false;
            String id = String.valueOf(o.getId());
            String customerId = String.valueOf(o.getCustomerId());
            String customerName = o.getCustomerName() == null ? "" : o.getCustomerName().toLowerCase();
            String status = o.getStatus() == null ? "" : o.getStatus().toLowerCase();
            String total = o.getTotal() == null ? "" : o.getTotal().toPlainString().toLowerCase();
            String created = o.getCreatedAt() == null ? "" : o.getCreatedAt().toString().toLowerCase();
            return id.contains(q) || customerId.contains(q) || customerName.contains(q) || status.contains(q) || total.contains(q) || created.contains(q);
        });
    }

    @FXML
    public void onApplyFilters() {
        String st = statusFilter != null && statusFilter.getValue() != null ? statusFilter.getValue().trim() : "";
        LocalDate dFrom = dateFrom != null ? dateFrom.getValue() : null;
        LocalDate dTo = dateTo != null ? dateTo.getValue() : null;
        filtered.setPredicate(o -> {
            if (o == null) return false;
            boolean ok = true;
            if (!st.isEmpty()) ok = ok && st.equalsIgnoreCase(o.getStatus());
            if (dFrom != null) ok = ok && o.getCreatedAt() != null && !o.getCreatedAt().toLocalDate().isBefore(dFrom);
            if (dTo != null) ok = ok && o.getCreatedAt() != null && !o.getCreatedAt().toLocalDate().isAfter(dTo);
            return ok;
        });
    }

    @FXML
    public void onClearFilters() {
        if (statusFilter != null) statusFilter.getSelectionModel().select(0);
        if (dateFrom != null) dateFrom.setValue(null);
        if (dateTo != null) dateTo.setValue(null);
        if (filtered != null) filtered.setPredicate(o -> true);
    }

    @FXML
    public void onRefresh() {
        refreshData();
    }

    private void refreshData() {
        masterData.setAll(new OrderDAO().listAll());
    }

    @FXML
    public void onNewOrder() {
        Integer userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getId() : null;
        if (userId == null) {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Novo Pedido");
            dlg.setHeaderText(null);
            dlg.setContentText("Informe o ID do cliente (usuario_id):");
            Optional<String> res = dlg.showAndWait();
            if (res.isEmpty()) return;
            try { userId = Integer.parseInt(res.get().trim()); } catch (Exception ex) { userId = null; }
        }
        if (userId == null) {
            alertInfo("Novo Pedido", "Cliente inválido.");
            return;
        }
        Integer id = new OrderDAO().createOrder(userId);
        if (id == null) {
            alertError("Novo Pedido", "Falha ao criar pedido.");
            return;
        }
        refreshData();
        alertInfo("Novo Pedido", "Pedido #" + id + " criado.");
    }

    @FXML
    public void onItems() {
        Order sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alertInfo("Itens do Pedido", "Selecione um pedido."); return; }
        try {
            java.net.URL fxml = getClass().getResource("/com/buyo/adminfx/ui/OrderItemsView.fxml");
            if (fxml == null) {
                String userDir = System.getProperty("user.dir");
                java.nio.file.Path p1 = java.nio.file.Paths.get(userDir, "buyo-adminfx", "src", "main", "resources", "com", "buyo", "adminfx", "ui", "OrderItemsView.fxml");
                java.nio.file.Path p2 = java.nio.file.Paths.get(userDir, "src", "main", "resources", "com", "buyo", "adminfx", "ui", "OrderItemsView.fxml");
                java.nio.file.Path existing = java.nio.file.Files.exists(p1) ? p1 : (java.nio.file.Files.exists(p2) ? p2 : null);
                if (existing != null) {
                    fxml = existing.toUri().toURL();
                }
            }
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxml);
            javafx.scene.Parent root = loader.load();
            OrderItemsController ctrl = loader.getController();
            ctrl.setOrderId(sel.getId());
            javafx.stage.Stage dlg = new javafx.stage.Stage();
            dlg.setTitle("Itens do Pedido #" + sel.getId());
            dlg.setScene(new javafx.scene.Scene(root, 720, 480));
            dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dlg.showAndWait();
            // Após fechar, recarrega lista e mantém filtros
            refreshData();
        } catch (Exception ex) {
            alertError("Itens", "Falha ao abrir itens: " + ex.getMessage());
        }
    }

    @FXML
    public void onChangeStatus() {
        Order sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alertInfo("Status do Pedido", "Selecione um pedido."); return; }
        OrderDAO dao = new OrderDAO();
        String atual = dao.getCurrentStatus(sel.getId());
        String[] proximos = nextStatuses(atual);
        if (proximos.length == 0) { alertInfo("Status", "Nenhuma transição válida a partir de " + atual + "."); return; }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(proximos[0], proximos);
        dlg.setTitle("Mudar Status");
        dlg.setHeaderText("Atual: " + atual);
        dlg.setContentText("Novo status:");
        Optional<String> choice = dlg.showAndWait();
        if (choice.isEmpty()) return;
        Integer adminId = Session.getCurrentUser() != null ? Session.getCurrentUser().getId() : null;
        boolean ok = dao.updateStatus(sel.getId(), choice.get(), adminId);
        if (!ok) alertError("Status", "Falha ao atualizar status.");
        refreshData();
    }

    @FXML
    public void onRemoveItem() {
        Order sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alertInfo("Remover Item", "Selecione um pedido."); return; }
        OrderDAO dao = new OrderDAO();
        List<OrderItem> items = dao.listItems(sel.getId());
        if (items.isEmpty()) { alertInfo("Remover Item", "Pedido não possui itens."); return; }
        java.util.LinkedHashMap<String, Integer> index = new java.util.LinkedHashMap<>();
        for (OrderItem it : items) {
            String label = "#" + it.getId() + " - prod " + it.getProductId() + ", qtd " + it.getQuantity() + ", R$ " + it.getUnitPrice();
            index.put(label, it.getId());
        }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(index.keySet().iterator().next(), index.keySet());
        dlg.setTitle("Remover Item");
        dlg.setHeaderText("Escolha o item para remover");
        dlg.setContentText("Item:");
        Optional<String> ch = dlg.showAndWait();
        if (ch.isEmpty()) return;
        Integer itemId = index.get(ch.get());
        boolean ok = dao.removeItem(itemId);
        if (!ok) { alertError("Remover Item", "Falha ao remover item."); return; }
        dao.recalcTotal(sel.getId());
        refreshData();
        alertInfo("Remover Item", "Item removido e total recalculado.");
    }

    @FXML
    public void onHistory() {
        Order sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alertInfo("Histórico", "Selecione um pedido."); return; }
        OrderDAO dao = new OrderDAO();
        List<String[]> rows = dao.listStatusLog(sel.getId());
        if (rows.isEmpty()) { alertInfo("Histórico", "Sem alterações de status registradas."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Histórico de status (mais recente primeiro):\n\n");
        for (String[] r : rows) {
            String antigo = r[0] == null ? "" : r[0];
            String novo = r[1] == null ? "" : r[1];
            String quando = r[2] == null ? "" : r[2];
            String admin = r[3] == null ? "" : r[3];
            sb.append(quando).append(" | ").append(antigo).append(" -> ").append(novo)
              .append(admin.isBlank() ? "" : (" (admin "+admin+")"))
              .append("\n");
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Histórico de Status");
        a.setHeaderText(null);
        a.setContentText(sb.toString());
        a.show();
    }

    private String[] nextStatuses(String atual) {
        if (atual == null) return new String[0];
        String a = atual.toUpperCase();
        switch (a) {
            case "CRIADO": return new String[]{"PAGO", "CANCELADO"};
            case "PAGO": return new String[]{"ENVIADO", "CANCELADO"};
            case "ENVIADO": return new String[]{"CONCLUIDO"};
            default: return new String[0];
        }
    }

    private void alertInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    private void alertError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    @FXML
    public void onExportCsv() {
        if (filtered == null || table == null) { alertError("Exportar", "Nada para exportar."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar pedidos como CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("id,cliente_id,cliente_nome,status,total,criado_em\n");
            for (Order o : filtered) {
                String line = String.format("%d,%d,\"%s\",%s,%s,%s\n",
                        o.getId(),
                        o.getCustomerId(),
                        safeCsv(o.getCustomerName()),
                        safeCsv(o.getStatus()),
                        o.getTotal() == null ? "" : o.getTotal().toPlainString(),
                        o.getCreatedAt() == null ? "" : o.getCreatedAt().toString()
                );
                sb.append(line);
            }
            java.nio.file.Files.writeString(file.toPath(), sb.toString(), java.nio.charset.StandardCharsets.UTF_8);
            alertInfo("Exportar", "Arquivo salvo: " + file.getAbsolutePath());
        } catch (Exception ex) {
            alertError("Exportar", "Falha ao salvar CSV: " + ex.getMessage());
        }
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "' ");
    }

    @FXML
    public void onExportOrder() {
        Order sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Exportar Pedido", "Selecione um pedido."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar pedido com itens");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;
        try {
            StringBuilder sb = new StringBuilder();
            // Cabeçalho do pedido
            sb.append("PEDIDO\n");
            sb.append("id,cliente_id,cliente_nome,status,total,criado_em\n");
            sb.append(String.format("%d,%d,\"%s\",%s,%s,%s\n\n",
                    sel.getId(), sel.getCustomerId(), safeCsv(sel.getCustomerName()), safeCsv(sel.getStatus()),
                    sel.getTotal() == null ? "" : sel.getTotal().toPlainString(),
                    sel.getCreatedAt() == null ? "" : sel.getCreatedAt().toString()
            ));

            // Itens do pedido
            sb.append("ITENS\n");
            sb.append("item_id,produto_id,quantidade,preco_unitario,subtotal\n");
            OrderDAO dao = new OrderDAO();
            java.util.List<OrderItem> items = dao.listItems(sel.getId());
            for (OrderItem it : items) {
                java.math.BigDecimal sub = (it.getUnitPrice() == null) ? java.math.BigDecimal.ZERO : it.getUnitPrice().multiply(new java.math.BigDecimal(it.getQuantity()));
                sb.append(String.format("%d,%d,%d,%s,%s\n",
                        it.getId(), it.getProductId(), it.getQuantity(),
                        it.getUnitPrice() == null ? "" : it.getUnitPrice().toPlainString(),
                        sub.toPlainString()
                ));
            }
            sb.append("\n");

            // Histórico de status
            sb.append("HISTORICO_STATUS\n");
            sb.append("quando,status_anterior,status_novo,admin_id\n");
            java.util.List<String[]> hist = dao.listStatusLog(sel.getId());
            for (String[] r : hist) {
                String antigo = r[0] == null ? "" : r[0];
                String novo = r[1] == null ? "" : r[1];
                String quando = r[2] == null ? "" : r[2];
                String admin = r[3] == null ? "" : r[3];
                sb.append(String.format("%s,%s,%s,%s\n", quando, antigo, novo, admin));
            }

            java.nio.file.Files.writeString(file.toPath(), sb.toString(), java.nio.charset.StandardCharsets.UTF_8);
            alertInfo("Exportar Pedido", "Arquivo salvo: " + file.getAbsolutePath());
        } catch (Exception ex) {
            alertError("Exportar Pedido", "Falha ao exportar: " + ex.getMessage());
        }
    }

    @FXML
    public void onApplyCoupon() {
        Order sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alertError("Cupom", "Selecione um pedido."); return; }
        TextInputDialog dlg = new TextInputDialog("");
        dlg.setTitle("Aplicar Cupom");
        dlg.setHeaderText(null);
        dlg.setContentText("Código do cupom:");
        java.util.Optional<String> res = dlg.showAndWait();
        if (res.isEmpty()) return;
        String code = res.get().trim();
        CouponService.Result r = new CouponService().applyCoupon(sel.getId(), code);
        if (!r.ok) { alertError("Cupom", r.message); return; }
        alertInfo("Cupom", r.message);
        refreshData();
    }
}
