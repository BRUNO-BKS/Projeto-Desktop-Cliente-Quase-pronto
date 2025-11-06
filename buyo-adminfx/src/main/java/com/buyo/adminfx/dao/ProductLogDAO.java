package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.ProductLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductLogDAO {
    // Ajuste os nomes das colunas conforme seu DDL real de `log_produtos`
    public boolean log(int productId, String action, int quantity, Integer adminId, Integer orderId, String note) {
        String sql = "INSERT INTO log_produtos (produto_id, tipo_acao, quantidade, admin_id, pedido_id, observacao, criado_em) VALUES (?,?,?,?,?, ?, CURRENT_TIMESTAMP)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, action);
            ps.setInt(3, quantity);
            if (adminId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, adminId);
            if (orderId == null) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, orderId);
            ps.setString(6, note == null ? "" : note);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<ProductLogEntry> list(Integer productId, Integer orderId, LocalDate from, LocalDate to) {
        // Tenta primeiro 'produto_log'; se falhar, usa 'log_produtos'
        try {
            return listFromTable("produto_log", productId, orderId, from, to);
        } catch (Exception first) {
            System.out.println("[ProductLogDAO] Falha ao consultar 'produto_log', tentando 'log_produtos'. Causa: " + first.getMessage());
            try {
                return listFromTable("log_produtos", productId, orderId, from, to);
            } catch (Exception second) {
                second.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    private List<ProductLogEntry> listFromTable(String table, Integer productId, Integer orderId, LocalDate from, LocalDate to) throws Exception {
        List<ProductLogEntry> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT a.* FROM ").append(table).append(" a WHERE 1=1");
        if (productId != null) sb.append(" AND (a.produto_id = ? OR a.product_id = ?)");
        if (orderId != null) sb.append(" AND (a.pedido_id = ? OR a.order_id = ?)");
        if (from != null) sb.append(" AND DATE(COALESCE(a.criado_em, a.created_at, a.data_log)) >= ?");
        if (to != null) sb.append(" AND DATE(COALESCE(a.criado_em, a.created_at, a.data_log)) <= ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            StringBuilder dbg = new StringBuilder("[ProductLogDAO] SQL(").append(table).append("): ").append(sb).append(" | params: ");
            if (productId != null) { ps.setInt(i++, productId); ps.setInt(i++, productId); dbg.append("produto_id|product_id=").append(productId).append(";"); }
            if (orderId != null) { ps.setInt(i++, orderId); ps.setInt(i++, orderId); dbg.append("pedido_id|order_id=").append(orderId).append(";"); }
            if (from != null) { ps.setDate(i++, java.sql.Date.valueOf(from)); dbg.append("from=").append(from).append(";"); }
            if (to != null) { ps.setDate(i++, java.sql.Date.valueOf(to)); dbg.append("to=").append(to).append(";"); }
            System.out.println(dbg.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ProductLogEntry(
                            getInt(rs, "id"),
                            getInt(rs, "produto_id", "product_id"),
                            getString(rs, "acao", "tipo_acao", "action"),
                            getInt(rs, "quantidade", "qtd", "quantity"),
                            getNullableInt(rs, "admin_id", "usuario_admin_id", "user_admin_id"),
                            getNullableInt(rs, "pedido_id", "order_id"),
                            getString(rs, "observacao", "obs", "note"),
                            toLdt(getTimestamp(rs, "criado_em", "created_at", "data_log"))
                    ));
                }
            }
        }
        return out;
    }

    private java.time.LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }

    private int getInt(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getInt(n); } catch (Exception ignore) {}
        }
        return 0;
    }
    private Integer getNullableInt(ResultSet rs, String... names) {
        for (String n : names) {
            try { return (Integer) rs.getObject(n); } catch (Exception ignore) {}
        }
        return null;
    }
    private String getString(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getString(n); } catch (Exception ignore) {}
        }
        return null;
    }
    private Timestamp getTimestamp(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getTimestamp(n); } catch (Exception ignore) {}
        }
        return null;
    }
}
