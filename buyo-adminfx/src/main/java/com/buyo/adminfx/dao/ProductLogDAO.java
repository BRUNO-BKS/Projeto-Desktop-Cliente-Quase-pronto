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
        } catch (Exception e) {
            return false;
        }
    }

    public List<ProductLogEntry> list(Integer productId, Integer orderId, LocalDate from, LocalDate to) {
        List<ProductLogEntry> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id, produto_id, tipo_acao, quantidade, admin_id, pedido_id, observacao, criado_em FROM log_produtos WHERE 1=1");
        if (productId != null) sb.append(" AND produto_id = ?");
        if (orderId != null) sb.append(" AND pedido_id = ?");
        if (from != null) sb.append(" AND DATE(criado_em) >= ?");
        if (to != null) sb.append(" AND DATE(criado_em) <= ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (productId != null) ps.setInt(i++, productId);
            if (orderId != null) ps.setInt(i++, orderId);
            if (from != null) ps.setDate(i++, java.sql.Date.valueOf(from));
            if (to != null) ps.setDate(i++, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ProductLogEntry(
                            rs.getInt("id"),
                            rs.getInt("produto_id"),
                            rs.getString("tipo_acao"),
                            rs.getInt("quantidade"),
                            (Integer) rs.getObject("admin_id"),
                            (Integer) rs.getObject("pedido_id"),
                            rs.getString("observacao"),
                            toLdt(rs.getTimestamp("criado_em"))
                    ));
                }
            }
        } catch (Exception e) {
            // vazio em caso de ausência de tabela
        }
        return out;
    }

    private java.time.LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}
