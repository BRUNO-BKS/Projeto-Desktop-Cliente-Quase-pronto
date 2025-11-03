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

    public List<Payment> list(Integer orderId, LocalDate from, LocalDate to) {
        List<Payment> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        // Ajuste estes nomes de colunas conforme seu DDL real de `pagamentos`
        sb.append("SELECT id, pedido_id, valor, metodo, status, transacao, criado_em FROM pagamentos WHERE 1=1");
        if (orderId != null) sb.append(" AND pedido_id = ?");
        if (from != null) sb.append(" AND DATE(criado_em) >= ?");
        if (to != null) sb.append(" AND DATE(criado_em) <= ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (orderId != null) ps.setInt(i++, orderId);
            if (from != null) ps.setDate(i++, java.sql.Date.valueOf(from));
            if (to != null) ps.setDate(i++, java.sql.Date.valueOf(to));
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
            // fallback vazio quando não há tabela/config
        }
        return out;
    }

    public boolean insert(int orderId, BigDecimal amount, String method, String status, String transaction) {
        String sql = "INSERT INTO pagamentos (pedido_id, valor, metodo, status, transacao, criado_em) VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method);
            ps.setString(4, status);
            ps.setString(5, transaction);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDateTime ts(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}
