package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Review;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    public List<Review> list(String status, Integer productId, Integer userId, LocalDate from, LocalDate to, Integer categoryId) {
        List<Review> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT a.* FROM avaliacoes a");
        if (categoryId != null) sb.append(" JOIN produtos p ON p.id = a.produto_id");
        sb.append(" WHERE 1=1");
        if (status != null && !status.isBlank()) sb.append(" AND a.status = ?");
        if (productId != null) sb.append(" AND (a.produto_id = ? OR a.product_id = ?)");
        if (userId != null) sb.append(" AND (a.usuario_id = ? OR a.user_id = ?)");
        if (from != null) sb.append(" AND DATE(COALESCE(a.data_avaliacao, a.criado_em, a.created_at)) >= ?");
        if (to != null) sb.append(" AND DATE(COALESCE(a.data_avaliacao, a.criado_em, a.created_at)) <= ?");
        if (categoryId != null) sb.append(" AND p.categoria_id = ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            // Debug: imprime SQL e parâmetros
            StringBuilder dbg = new StringBuilder("[ReviewDAO] SQL: ").append(sb).append(" | params: ");
            int i = 1;
            if (status != null && !status.isBlank()) { ps.setString(i++, status); dbg.append("status=").append(status).append(";"); }
            if (productId != null) { ps.setInt(i++, productId); ps.setInt(i++, productId); dbg.append("produto_id|product_id=").append(productId).append(";"); }
            if (userId != null) { ps.setInt(i++, userId); ps.setInt(i++, userId); dbg.append("usuario_id|user_id=").append(userId).append(";"); }
            if (from != null) { ps.setDate(i++, java.sql.Date.valueOf(from)); dbg.append("from=").append(from).append(";"); }
            if (to != null) { ps.setDate(i++, java.sql.Date.valueOf(to)); dbg.append("to=").append(to).append(";"); }
            if (categoryId != null) { ps.setInt(i++, categoryId); dbg.append("categoria_id=").append(categoryId).append(";"); }
            System.out.println(dbg.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = getInt(rs, "id");
                    int pid = getInt(rs, "produto_id", "product_id");
                    int uid = getInt(rs, "usuario_id", "user_id");
                    int rating = getInt(rs, "nota", "rating");
                    String comment = getString(rs, "comentario", "comment");
                    String st = getString(rs, "status");
                    Timestamp ts = getTimestamp(rs, "data_avaliacao", "criado_em", "created_at");
                    out.add(new Review(id, pid, uid, rating, comment, st, toLdt(ts)));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }

    private int getInt(ResultSet rs, String... names) throws SQLException {
        for (String n : names) {
            try { return rs.getInt(n); } catch (SQLException ignore) {}
        }
        return 0;
    }

    private String getString(ResultSet rs, String... names) throws SQLException {
        for (String n : names) {
            try { return rs.getString(n); } catch (SQLException ignore) {}
        }
        return null;
    }

    private Timestamp getTimestamp(ResultSet rs, String... names) throws SQLException {
        for (String n : names) {
            try { return rs.getTimestamp(n); } catch (SQLException ignore) {}
        }
        return null;
    }

    public boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE avaliacoes SET status = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private java.time.LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}
