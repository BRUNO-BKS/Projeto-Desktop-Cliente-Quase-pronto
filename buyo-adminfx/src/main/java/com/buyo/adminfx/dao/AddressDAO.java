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
        StringBuilder sb = new StringBuilder("SELECT id, usuario_id, logradouro, numero, bairro, cidade, estado, cep, complemento, tipo FROM enderecos WHERE 1=1");
        if (userId != null) sb.append(" AND usuario_id = ?");
        sb.append(" ORDER BY id DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            if (userId != null) ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Address(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            rs.getString("logradouro"),
                            rs.getString("numero"),
                            rs.getString("bairro"),
                            rs.getString("cidade"),
                            rs.getString("estado"),
                            rs.getString("cep"),
                            rs.getString("complemento"),
                            rs.getString("tipo")
                    ));
                }
            }
        } catch (Exception e) { }
        return out;
    }
}
