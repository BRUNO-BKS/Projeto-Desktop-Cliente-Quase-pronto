package com.buyo.adminfx.ui.controllers;

import com.buyo.adminfx.dao.PaymentDAO;
import com.buyo.adminfx.model.Payment;
import com.buyo.adminfx.services.PaymentService;
import com.buyo.adminfx.dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PaymentsController implements SearchableController {
    @FXML private TextField orderIdFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    @FXML private TableView<Payment> table;
    @FXML private TableColumn<Payment, Integer> colId;
    @FXML private TableColumn<Payment, Integer> colOrderId;
    @FXML private TableColumn<Payment, BigDecimal> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, String> colTransaction;
    @FXML private TableColumn<Payment, java.time.LocalDateTime> colCreatedAt;

    private final PaymentDAO dao = new PaymentDAO();
    private final ObservableList<Payment> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colOrderId != null) colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        if (colAmount != null) colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        if (colMethod != null) colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (colTransaction != null) colTransaction.setCellValueFactory(new PropertyValueFactory<>("transaction"));
        if (colCreatedAt != null) colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        if (table != null) table.setItems(rows);
        refresh();
    }

    @FXML
    public void onApplyFilters(ActionEvent e) { refresh(); }

    @FXML
    public void onClearFilters(ActionEvent e) {
        if (orderIdFilter != null) orderIdFilter.clear();
        if (dateFrom != null) dateFrom.setValue(null);
        if (dateTo != null) dateTo.setValue(null);
        refresh();
    }

    @FXML
    public void onRefresh(ActionEvent e) { refresh(); }

    private void refresh() {
        Integer orderId = null;
        try {
            if (orderIdFilter != null && orderIdFilter.getText() != null && !orderIdFilter.getText().isBlank()) {
                orderId = Integer.parseInt(orderIdFilter.getText().trim());
            }
        } catch (Exception ignore) {}
        LocalDate from = dateFrom != null ? dateFrom.getValue() : null;
        LocalDate to = dateTo != null ? dateTo.getValue() : null;
        List<Payment> list = dao.list(orderId, from, to);
        rows.setAll(list);
    }

    @FXML
    public void onRegisterPayment(ActionEvent e) {
        TextInputDialog orderDlg = new TextInputDialog(orderIdFilter != null ? orderIdFilter.getText() : "");
        orderDlg.setTitle("Pagamento");
        orderDlg.setHeaderText(null);
        orderDlg.setContentText("Pedido ID:");
        var orderRes = orderDlg.showAndWait();
        if (orderRes.isEmpty()) return;
        int oid;
        try { oid = Integer.parseInt(orderRes.get().trim()); } catch (Exception ex) { error("Pagamento", "Pedido ID inválido."); return; }

        TextInputDialog valDlg = new TextInputDialog("");
        valDlg.setTitle("Pagamento");
        valDlg.setHeaderText(null);
        valDlg.setContentText("Valor (ex: 99.90):");
        var valRes = valDlg.showAndWait();
        if (valRes.isEmpty()) return;
        BigDecimal amount;
        try { amount = new BigDecimal(valRes.get().trim().replace(',', '.')); } catch (Exception ex) { error("Pagamento", "Valor inválido."); return; }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) { error("Pagamento", "Valor deve ser > 0."); return; }

        ChoiceDialog<String> methodDlg = new ChoiceDialog<>("PIX", FXCollections.observableArrayList("PIX", "CARTAO", "BOLETO", "DINHEIRO"));
        methodDlg.setTitle("Pagamento");
        methodDlg.setHeaderText(null);
        methodDlg.setContentText("Método:");
        var methodRes = methodDlg.showAndWait();
        if (methodRes.isEmpty()) return;
        String method = methodRes.get();

        ChoiceDialog<String> statusDlg = new ChoiceDialog<>("CONFIRMADO", FXCollections.observableArrayList("PENDENTE", "CONFIRMADO", "FALHOU"));
        statusDlg.setTitle("Pagamento");
        statusDlg.setHeaderText(null);
        statusDlg.setContentText("Status:");
        var statusRes = statusDlg.showAndWait();
        if (statusRes.isEmpty()) return;
        String status = statusRes.get();

        TextInputDialog txDlg = new TextInputDialog("");
        txDlg.setTitle("Pagamento");
        txDlg.setHeaderText(null);
        txDlg.setContentText("Transação (opcional):");
        var txRes = txDlg.showAndWait();
        if (txRes.isEmpty()) return;
        String tx = txRes.get().trim();

        boolean ok = dao.insert(oid, amount, method, status, tx);
        if (!ok) {
            String why = (dao.getLastError() == null || dao.getLastError().isBlank()) ? "" : (" Detalhes: " + dao.getLastError());
            error("Pagamento", "Falha ao registrar pagamento." + why);
            return;
        }
        if ("CONFIRMADO".equalsIgnoreCase(status)) {
            try {
                PaymentService svc = new PaymentService();
                boolean okConf = svc.onPaymentConfirmed(oid);
                if (!okConf) {
                    String why = svc.getLastError();
                    error("Pagamento", "Falha ao confirmar (baixar estoque)." + (why == null || why.isBlank() ? "" : (" Detalhes: " + why)));
                }
            } catch (Exception ex) {
                error("Pagamento", "Erro ao confirmar pagamento: " + ex.getMessage());
            }
        }
        info("Pagamento", "Pagamento registrado.");
        refresh();
    }

    @FXML
    public void onHistory(ActionEvent e) {
        Integer oid = null;
        Payment sel = table != null ? table.getSelectionModel().getSelectedItem() : null;
        if (sel != null) oid = sel.getOrderId();
        if (oid == null) {
            try { if (orderIdFilter != null && orderIdFilter.getText() != null && !orderIdFilter.getText().isBlank()) oid = Integer.parseInt(orderIdFilter.getText().trim()); } catch (Exception ignore) {}
        }
        if (oid == null) { error("Histórico", "Selecione um pagamento ou informe o Pedido ID."); return; }
        OrderDAO dao = new OrderDAO();
        java.util.List<String[]> rows = dao.listStatusLog(oid);
        if (rows.isEmpty()) { info("Histórico", "Sem alterações de status para o pedido #" + oid + "."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Histórico de status do Pedido #").append(oid).append(" (mais recente primeiro):\n\n");
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

    @Override
    public void applySearch(String query) { }
}
