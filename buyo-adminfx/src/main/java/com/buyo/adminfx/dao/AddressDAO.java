package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Address;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {
    public List<Address> list(Integer userId) {
        List<Address> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT a.* FROM enderecos a WHERE 1=1");
        if (userId != null) sb.append(" AND (a.usuario_id = ? OR a.user_id = ?)");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            StringBuilder dbg = new StringBuilder("[AddressDAO] SQL: ").append(sb).append(" | params: ");
            if (userId != null) { ps.setInt(1, userId); ps.setInt(2, userId); dbg.append("userId=").append(userId).append(";"); }
            System.out.println(dbg.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Address(
                            getInt(rs, "id"),
                            getInt(rs, "usuario_id", "user_id"),
                            getString(rs, "rua", "logradouro", "street", "logradouro_rua"),
                            getString(rs, "numero", "number"),
                            getString(rs, "bairro", "district"),
                            getString(rs, "cidade", "city"),
                            getString(rs, "estado", "state"),
                            getString(rs, "cep", "zipcode", "zip"),
                            getString(rs, "complemento", "complement"),
                            getString(rs, "tipo", "type")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
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
}
