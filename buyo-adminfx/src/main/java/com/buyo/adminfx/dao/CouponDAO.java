package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Coupon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {

    public List<Coupon> list(String codeLike, Boolean activeOnly) {
        List<Coupon> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT id, codigo, ativo, expira_em, percentual, valor, minimo FROM cupons WHERE 1=1");
        if (codeLike != null && !codeLike.isBlank()) sb.append(" AND codigo LIKE ?");
        if (activeOnly != null && activeOnly) sb.append(" AND ativo = 1");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (codeLike != null && !codeLike.isBlank()) ps.setString(i++, "%" + codeLike.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Coupon(
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getInt("ativo") == 1,
                            toLocalDate(rs.getDate("expira_em")),
                            (BigDecimal) rs.getObject("percentual"),
                            (BigDecimal) rs.getObject("valor"),
                            (BigDecimal) rs.getObject("minimo")
                    ));
                }
            }
        } catch (Exception e) { }
        return out;
    }

    public boolean insert(String code, boolean active, LocalDate expiresAt, BigDecimal percent, BigDecimal amount, BigDecimal minimum) {
        String sql = "INSERT INTO cupons (codigo, ativo, expira_em, percentual, valor, minimo) VALUES (?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, active ? 1 : 0);
            if (expiresAt == null) ps.setNull(3, java.sql.Types.DATE); else ps.setDate(3, java.sql.Date.valueOf(expiresAt));
            if (percent == null) ps.setNull(4, java.sql.Types.DECIMAL); else ps.setBigDecimal(4, percent);
            if (amount == null) ps.setNull(5, java.sql.Types.DECIMAL); else ps.setBigDecimal(5, amount);
            if (minimum == null) ps.setNull(6, java.sql.Types.DECIMAL); else ps.setBigDecimal(6, minimum);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    private LocalDate toLocalDate(java.sql.Date d) { return d == null ? null : d.toLocalDate(); }

    public Coupon getByCode(String code, boolean onlyValid) {
        StringBuilder sb = new StringBuilder("SELECT id, codigo, ativo, expira_em, percentual, valor, minimo FROM cupons WHERE codigo = ?");
        if (onlyValid) sb.append(" AND ativo = 1 AND (expira_em IS NULL OR expira_em >= CURRENT_DATE)");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Coupon(
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getInt("ativo") == 1,
                            toLocalDate(rs.getDate("expira_em")),
                            (BigDecimal) rs.getObject("percentual"),
                            (BigDecimal) rs.getObject("valor"),
                            (BigDecimal) rs.getObject("minimo")
                    );
                }
            }
        } catch (Exception e) { }
        return null;
    }
}
