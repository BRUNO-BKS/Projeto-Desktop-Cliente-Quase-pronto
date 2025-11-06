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
        StringBuilder sb = new StringBuilder("SELECT c.* FROM cupons c WHERE 1=1");
        if (codeLike != null && !codeLike.isBlank()) sb.append(" AND (c.codigo LIKE ? OR c.code LIKE ?)");
        if (activeOnly != null && activeOnly) sb.append(" AND (c.ativo = 1 OR c.active = 1)");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            StringBuilder dbg = new StringBuilder("[CouponDAO] SQL: ").append(sb).append(" | params: ");
            if (codeLike != null && !codeLike.isBlank()) { 
                String q = "%" + codeLike.trim() + "%";
                ps.setString(i++, q); 
                ps.setString(i++, q);
                dbg.append("codeLike=").append(codeLike.trim()).append(";"); 
            }
            System.out.println(dbg.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Coupon(
                            getInt(rs, "id"),
                            getString(rs, "codigo", "code"),
                            getBool(rs, "ativo", "active"),
                            toLocalDate(getDate(rs, "data_expiracao", "expira_em", "expires_at")),
                            (BigDecimal) getObject(rs, "desconto_percentual", "percentual", "percent"),
                            (BigDecimal) getObject(rs, "valor", "amount"),
                            (BigDecimal) getObject(rs, "minimo", "minimum")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
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
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private LocalDate toLocalDate(java.sql.Date d) { return d == null ? null : d.toLocalDate(); }

    public Coupon getByCode(String code, boolean onlyValid) {
        StringBuilder sb = new StringBuilder("SELECT c.* FROM cupons c WHERE (c.codigo = ? OR c.code = ?)");
        if (onlyValid) sb.append(" AND ativo = 1 AND (expira_em IS NULL OR expira_em >= CURRENT_DATE)");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            ps.setString(1, code);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Coupon(
                            getInt(rs, "id"),
                            getString(rs, "codigo", "code"),
                            getBool(rs, "ativo", "active"),
                            toLocalDate(getDate(rs, "data_expiracao", "expira_em", "expires_at")),
                            (BigDecimal) getObject(rs, "desconto_percentual", "percentual", "percent"),
                            (BigDecimal) getObject(rs, "valor", "amount"),
                            (BigDecimal) getObject(rs, "minimo", "minimum")
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private int getInt(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getInt(n); } catch (Exception ignore) {}
        }
        return 0;
    }
    private String getString(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getString(n); } catch (Exception ignore) {}
        }
        return null;
    }
    private boolean getBool(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getInt(n) == 1; } catch (Exception ignore) {}
            try { return rs.getBoolean(n); } catch (Exception ignore2) {}
        }
        return false;
    }
    private java.sql.Date getDate(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getDate(n); } catch (Exception ignore) {}
        }
        return null;
    }
    private Object getObject(ResultSet rs, String... names) {
        for (String n : names) {
            try { return rs.getObject(n); } catch (Exception ignore) {}
        }
        return null;
    }
}
