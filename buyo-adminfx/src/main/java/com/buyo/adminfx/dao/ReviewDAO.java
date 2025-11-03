package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Review;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    public List<Review> list(String status, Integer productId, Integer userId, LocalDate from, LocalDate to) {
        List<Review> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT id, produto_id, usuario_id, nota, comentario, status, criado_em FROM avaliacoes WHERE 1=1");
        if (status != null && !status.isBlank()) sb.append(" AND status = ?");
        if (productId != null) sb.append(" AND produto_id = ?");
        if (userId != null) sb.append(" AND usuario_id = ?");
        if (from != null) sb.append(" AND DATE(criado_em) >= ?");
        if (to != null) sb.append(" AND DATE(criado_em) <= ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (status != null && !status.isBlank()) ps.setString(i++, status);
            if (productId != null) ps.setInt(i++, productId);
            if (userId != null) ps.setInt(i++, userId);
            if (from != null) ps.setDate(i++, java.sql.Date.valueOf(from));
            if (to != null) ps.setDate(i++, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("produto_id"),
                            rs.getInt("usuario_id"),
                            rs.getInt("nota"),
                            rs.getString("comentario"),
                            rs.getString("status"),
                            toLdt(rs.getTimestamp("criado_em"))
                    ));
                }
            }
        } catch (Exception e) { }
        return out;
    }

    public boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE avaliacoes SET status = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    private java.time.LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}
