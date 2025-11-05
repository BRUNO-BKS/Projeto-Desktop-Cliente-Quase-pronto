package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private String lastError;

    public String getLastError() { return lastError; }

    private void resetErr() { lastError = null; }

    private boolean ensurePaymentsTable() {
        String ddl = "CREATE TABLE IF NOT EXISTS pagamentos (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "pedido_id INT NOT NULL, " +
                "valor DECIMAL(12,2) NOT NULL, " +
                "metodo VARCHAR(30) NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "transacao VARCHAR(180), " +
                "criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_pag_pedido (pedido_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(ddl)) {
            ps.execute();
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            return false;
        }
    }

    private String resolveColumn(Connection c, String table, String... candidates) {
        final String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        for (String cand : candidates) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, table);
                ps.setString(2, cand);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return cand;
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    public List<Payment> list(Integer orderId, LocalDate from, LocalDate to) {
        resetErr();
        List<Payment> out = new ArrayList<>();
        String methodCol = null, txCol = null, createdCol = null;
        try (Connection cc = Database.getConnection()) {
            methodCol = resolveColumn(cc, "pagamentos", "metodo", "forma", "forma_pagamento", "metodo_pagamento");
            txCol = resolveColumn(cc, "pagamentos", "transacao", "transaction", "tx_id");
            createdCol = resolveColumn(cc, "pagamentos", "criado_em", "created_at", "data_criacao");
        } catch (Exception ignore) {}

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id, pedido_id, valor, ");
        if (methodCol != null) sb.append(methodCol).append(" AS metodo, "); else sb.append("'' AS metodo, ");
        sb.append("status, ");
        if (txCol != null) sb.append(txCol).append(" AS transacao, "); else sb.append("'' AS transacao, ");
        if (createdCol != null) sb.append(createdCol).append(" AS criado_em "); else sb.append("NULL AS criado_em ");
        sb.append("FROM pagamentos WHERE 1=1");
        if (orderId != null) sb.append(" AND pedido_id = ?");
        if (createdCol != null) {
            if (from != null) sb.append(" AND DATE(").append(createdCol).append(") >= ?");
            if (to != null) sb.append(" AND DATE(").append(createdCol).append(") <= ?");
        }
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (orderId != null) ps.setInt(i++, orderId);
            if (createdCol != null) {
                if (from != null) ps.setDate(i++, java.sql.Date.valueOf(from));
                if (to != null) ps.setDate(i++, java.sql.Date.valueOf(to));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Payment(
                            rs.getInt("id"),
                            rs.getInt("pedido_id"),
                            rs.getBigDecimal("valor"),
                            rs.getString("metodo"),
                            rs.getString("status"),
                            rs.getString("transacao"),
                            ts(rs.getTimestamp("criado_em"))
                    ));
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        return out;
    }

    public boolean insert(int orderId, BigDecimal amount, String method, String status, String transaction) {
        resetErr();
        if (!ensurePaymentsTable()) return false;
        String methodCol = null, txCol = null; // vamos omitir se não existirem
        try (Connection cc = Database.getConnection()) {
            methodCol = resolveColumn(cc, "pagamentos", "metodo", "forma", "forma_pagamento", "metodo_pagamento");
            txCol = resolveColumn(cc, "pagamentos", "transacao", "transaction", "tx_id");
        } catch (Exception ignore) {}

        StringBuilder cols = new StringBuilder("pedido_id, valor, status");
        StringBuilder vals = new StringBuilder("?,?,?");
        if (methodCol != null) { cols.append(", ").append(methodCol); vals.append(",?"); }
        if (txCol != null) { cols.append(", ").append(txCol); vals.append(",?"); }
        String sql = "INSERT INTO pagamentos (" + cols + ") VALUES (" + vals + ")";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, orderId);
            ps.setBigDecimal(i++, amount);
            ps.setString(i++, status);
            if (methodCol != null) ps.setString(i++, method);
            if (txCol != null) ps.setString(i++, transaction);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            // Fallback: se der unknown column, tenta inserir somente colunas essenciais
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("unknown column")) {
                try (Connection c2 = Database.getConnection();
                     PreparedStatement ps2 = c2.prepareStatement("INSERT INTO pagamentos (pedido_id, valor, status) VALUES (?,?,?)")) {
                    ps2.setInt(1, orderId);
                    ps2.setBigDecimal(2, amount);
                    ps2.setString(3, status);
                    return ps2.executeUpdate() > 0;
                } catch (Exception e2) {
                    lastError = e2.getMessage();
                    return false;
                }
            }
            lastError = e.getMessage();
            return false;
        }
    }

    private LocalDateTime ts(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}
