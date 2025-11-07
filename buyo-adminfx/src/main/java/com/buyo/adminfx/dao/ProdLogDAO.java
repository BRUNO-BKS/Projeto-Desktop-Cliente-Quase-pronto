package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.ProdLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdLogDAO {
    public List<ProdLogEntry> list(Integer productId, Integer orderId, LocalDate from, LocalDate to) {
        List<ProdLogEntry> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT a.* FROM produto_log a WHERE 1=1");
        if (productId != null) sb.append(" AND (a.produto_id = ? OR a.product_id = ?)");
        if (orderId != null) sb.append(" AND (a.pedido_id = ? OR a.order_id = ?)");
        sb.append(" AND 1=1");
        if (from != null) sb.append(" AND DATE(COALESCE(a.criado_em, a.created_at, a.data_log)) >= ?");
        if (to != null) sb.append(" AND DATE(COALESCE(a.criado_em, a.created_at, a.data_log)) <= ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (productId != null) { ps.setInt(i++, productId); ps.setInt(i++, productId); }
            if (orderId != null) { ps.setInt(i++, orderId); ps.setInt(i++, orderId); }
            if (from != null) ps.setDate(i++, java.sql.Date.valueOf(from));
            if (to != null) ps.setDate(i++, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ProdLogEntry(
                            rs.getInt("id"),
                            getInt(rs, "produto_id", "product_id"),
                            getString(rs, "acao", "tipo_acao", "action"),
                            getNullableInt(rs, "admin_id", "usuario_admin_id", "user_admin_id"),
                            toLdt(getTimestamp(rs, "criado_em", "created_at", "data_log"))
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

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
    private java.time.LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}
