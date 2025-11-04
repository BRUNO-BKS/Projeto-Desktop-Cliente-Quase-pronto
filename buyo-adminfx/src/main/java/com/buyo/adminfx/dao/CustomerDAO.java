package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public List<Customer> listAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, nome AS name, email, telefone AS phone, created_at, last_active, is_online FROM usuarios ORDER BY id DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String created = null;
                String last = null;
                boolean online = false;
                try { created = String.valueOf(rs.getTimestamp("created_at")); } catch (Exception ignore) {}
                try { last = String.valueOf(rs.getTimestamp("last_active")); } catch (Exception ignore) {}
                try { online = rs.getInt("is_online") == 1; } catch (Exception ignore) {}
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        created,
                        last,
                        online
                ));
            }
        } catch (Exception e) {
            // Em caso de erro ou sem DB configurado, retorna lista vazia
        }
        return list;
    }
}
