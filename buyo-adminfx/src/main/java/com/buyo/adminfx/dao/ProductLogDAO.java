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
    public List<ProductLogEntry> list(Integer productId, LocalDate from, LocalDate to) {
        List<ProductLogEntry> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT id, produto_id, campo_alterado, valor_antigo, valor_novo, data_alteracao FROM log_produtos WHERE 1=1");
        if (productId != null) sb.append(" AND produto_id = ?");
        if (from != null) sb.append(" AND DATE(data_alteracao) >= ?");
        if (to != null) sb.append(" AND DATE(data_alteracao) <= ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (productId != null) ps.setInt(i++, productId);
            if (from != null) ps.setDate(i++, java.sql.Date.valueOf(from));
            if (to != null) ps.setDate(i++, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ProductLogEntry(
                            rs.getInt("id"),
                            rs.getInt("produto_id"),
                            rs.getString("campo_alterado"),
                            rs.getString("valor_antigo"),
                            rs.getString("valor_novo"),
                            toLdt(rs.getTimestamp("data_alteracao"))
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public boolean update(ProductLogEntry e) {
        String sql = "UPDATE log_produtos SET campo_alterado=?, valor_antigo=?, valor_novo=?, produto_id=?, data_alteracao=? WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getFieldChanged());
            ps.setString(2, e.getOldValue());
            ps.setString(3, e.getNewValue());
            ps.setInt(4, e.getProductId());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(e.getChangedAt()));
            ps.setInt(6, e.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM log_produtos WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private java.time.LocalDateTime toLdt(Timestamp t) { return t == null ? null : t.toLocalDateTime(); }
}

