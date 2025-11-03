package com.buyo.adminfx.dao;

import com.buyo.adminfx.db.Database;
import com.buyo.adminfx.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public List<Category> listAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, nome AS name, descricao AS description FROM categorias ORDER BY nome ASC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")
                ));
            }
        } catch (Exception e) {
            // retorna vazio em caso de falha
        }
        return list;
    }

    public Integer createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) return null;
        String sql = "INSERT INTO categorias (nome, descricao) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, description == null ? null : description.trim());
            int r = ps.executeUpdate();
            if (r == 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public boolean updateCategory(int id, String name, String description) {
        String sql = "UPDATE categorias SET nome = ?, descricao = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
